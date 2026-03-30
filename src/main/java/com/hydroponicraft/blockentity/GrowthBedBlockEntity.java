package com.hydroponicraft.blockentity;

import com.hydroponicraft.GrowthModifier;
import com.hydroponicraft.HydroponiCraftRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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

    public GrowthBedBlockEntity(BlockPos pos, BlockState state) {
        super(HydroponiCraftRegistry.GROWTH_BED_BE.get(), pos, state);
    }

    // ------------------------------------------------------------------
    // Ticking
    // ------------------------------------------------------------------

    public void tick() {
        if (level == null || level.isClientSide()) return;

        BlockPos cropPos = worldPosition.above();
        BlockState cropState = level.getBlockState(cropPos);

        if (!(cropState.getBlock() instanceof CropBlock crop)) return;

        FluidStack fluid = fluidTank.getFluid();
        if (fluid.isEmpty() || fluidTank.getFluidAmount() < 250) return;

        Optional<GrowthModifier> modOpt = GrowthModifier.get(fluid.getFluid());
        if (modOpt.isEmpty()) return;

        GrowthModifier modifier = modOpt.get();
        ServerLevel serverLevel = (ServerLevel) level;

        fluidTank.drain(250, IFluidHandler.FluidAction.EXECUTE);
        setChanged();

        int extraTicks = Math.round(modifier.speedMultiplier());
        for (int i = 0; i < extraTicks; i++) {
            BlockState current = level.getBlockState(cropPos);
            if (!(current.getBlock() instanceof CropBlock currentCrop)) return;

            if (currentCrop.isMaxAge(current)) {
                harvestCrop(serverLevel, cropPos, current, modifier);
                return;
            }
            // Advance age by one stage (equivalent to one randomTick without light checks)
            int nextAge = Math.min(currentCrop.getAge(current) + 1, currentCrop.getMaxAge());
            serverLevel.setBlock(cropPos, currentCrop.getStateForAge(nextAge), 2);
        }

        // One final check after all ticks in case the last one pushed to max age
        BlockState finalState = level.getBlockState(cropPos);
        if (finalState.getBlock() instanceof CropBlock finalCrop && finalCrop.isMaxAge(finalState)) {
            harvestCrop(serverLevel, cropPos, finalState, modifier);
        }
    }

    private void harvestCrop(ServerLevel level, BlockPos cropPos, BlockState cropState,
                              GrowthModifier modifier) {
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
