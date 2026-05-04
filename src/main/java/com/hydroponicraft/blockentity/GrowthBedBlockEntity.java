package com.hydroponicraft.blockentity;

import com.hydroponicraft.GrowthModifier;
import com.hydroponicraft.HydroponiCraftRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;
import java.util.Optional;

public class GrowthBedBlockEntity extends BlockEntity {

    /** Accepts only fluids registered in GrowthModifier.REGISTRY (the 5 solutions). */
    public final FluidTank fluidTank = new FluidTank(4_000) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return GrowthModifier.REGISTRY.containsKey(stack.getFluid());
        }
    };

    /** Base interval in server ticks between growth advances at 1.0x speed multiplier. */
    private static final int BASE_INTERVAL = 100;

    /** Counts server ticks since the last growth advance. */
    private int tickCounter = 0;

    public GrowthBedBlockEntity(BlockPos pos, BlockState state) {
        super(HydroponiCraftRegistry.GROWTH_BED_BE.get(), pos, state);
    }

    // ------------------------------------------------------------------
    // Ticking
    // ------------------------------------------------------------------

    public void tick() {
        if (level == null || level.isClientSide()) return;

        tickCounter++;

        // Check fluid before computing threshold — bail early if tank is dry
        FluidStack fluid = fluidTank.getFluid();
        if (fluid.isEmpty() || fluidTank.getFluidAmount() < 25) return;

        Optional<GrowthModifier> modOpt = GrowthModifier.get(fluid.getFluid());
        if (modOpt.isEmpty()) return;

        GrowthModifier modifier = modOpt.get();

        // Higher speed multiplier → shorter interval between growth ticks
        int threshold = Math.max(1, (int) (BASE_INTERVAL / modifier.speedMultiplier()));
        if (tickCounter < threshold) return;
        tickCounter = 0;

        BlockPos cropPos = worldPosition.above();
        BlockState cropState = level.getBlockState(cropPos);
        if (!(cropState.getBlock() instanceof CropBlock crop)) return;

        // Consume fluid and advance crop by one age stage
        fluidTank.drain(25, IFluidHandler.FluidAction.EXECUTE);
        setChanged();

        ServerLevel serverLevel = (ServerLevel) level;

        if (crop.isMaxAge(cropState)) {
            harvestCrop(serverLevel, cropPos, cropState, modifier);
            return;
        }

        int nextAge = Math.min(crop.getAge(cropState) + 1, crop.getMaxAge());
        serverLevel.setBlock(cropPos, crop.getStateForAge(nextAge), 2);
        serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                cropPos.getX() + 0.5, cropPos.getY() + 0.5, cropPos.getZ() + 0.5,
                3, 0.4, 0.2, 0.4, 0.0);

        // Harvest immediately if the single advance pushed the crop to max age
        BlockState advanced = level.getBlockState(cropPos);
        if (advanced.getBlock() instanceof CropBlock advCrop && advCrop.isMaxAge(advanced)) {
            harvestCrop(serverLevel, cropPos, advanced, modifier);
        }
    }

    private void harvestCrop(ServerLevel level, BlockPos cropPos, BlockState cropState,
                              GrowthModifier modifier) {
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                cropPos.getX() + 0.5, cropPos.getY() + 0.5, cropPos.getZ() + 0.5,
                8, 0.5, 0.3, 0.5, 0.0);
        level.playSound(null, cropPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.6f, 1.0f);

        // Build a fortune-enchanted tool if the solution provides a fortune bonus
        ItemStack tool = new ItemStack(Items.DIAMOND_HOE);
        if (modifier.fortuneLevel() > 0) {
            level.registryAccess()
                    .lookup(Registries.ENCHANTMENT)
                    .flatMap(reg -> reg.get(Enchantments.FORTUNE))
                    .ifPresent(h -> tool.enchant(h, modifier.fortuneLevel()));
        }

        List<ItemStack> drops = Block.getDrops(cropState, level, cropPos, null, null, tool);

        // Remove crop — player uses a Create Deployer above to replant
        level.setBlock(cropPos, Blocks.AIR.defaultBlockState(), 3);

        // Push drops downward into whatever is below (hopper, pipe, chest, etc.)
        IItemHandler below = level.getCapability(
                Capabilities.ItemHandler.BLOCK, worldPosition.below(), Direction.UP);

        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            ItemStack remaining = drop.copy();

            if (below != null) {
                for (int slot = 0; slot < below.getSlots() && !remaining.isEmpty(); slot++) {
                    remaining = below.insertItem(slot, remaining, false);
                }
            }

            // Anything that couldn't be inserted falls out as an item entity
            if (!remaining.isEmpty()) {
                Block.popResource(level, worldPosition, remaining);
            }
        }

        setChanged();
    }

    // ------------------------------------------------------------------
    // NBT persistence
    // ------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("FluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
    }
}
