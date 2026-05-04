package com.hydroponicraft.blockentity;

import com.hydroponicraft.HydroponiCraftRegistry;
import com.hydroponicraft.recipe.DigesterRecipe;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Optional;

public class DigesterBlockEntity extends KineticBlockEntity {

    /** Single organic input slot — fed by hopper from above. */
    public final ItemStackHandler itemHandler = new ItemStackHandler(1);

    /** Output tank — accepts any fluid (recipe determines what is produced), 8 000 mB capacity. */
    public final FluidTank fluidTank = new FluidTank(8_000);

    private int processingTicks = 0;
    private int maxTicks        = 0;

    public DigesterBlockEntity(BlockPos pos, BlockState state) {
        super(HydroponiCraftRegistry.DIGESTER_BE.get(), pos, state);
    }

    // ------------------------------------------------------------------
    // Ticking
    // ------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) return;
        if (!isSpeedRequirementFulfilled() || Math.abs(getSpeed()) < 1f) {
            processingTicks = 0;
            maxTicks = 0;
            return;
        }
        processRecipe();
        if (processingTicks > 0 && level instanceof ServerLevel sl) {
            double cx = worldPosition.getX() + 0.5;
            double cy = worldPosition.getY() + 1.0;
            double cz = worldPosition.getZ() + 0.5;
            if (processingTicks % 4 == 0)
                sl.sendParticles(ParticleTypes.BUBBLE, cx, cy, cz, 4, 0.3, 0.1, 0.3, 0.0);
            if (processingTicks % 20 == 0)
                level.playSound(null, worldPosition, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP,
                        SoundSource.BLOCKS, 0.4f, 0.9f + level.random.nextFloat() * 0.2f);
        }
    }

    private void processRecipe() {
        ItemStack stack = itemHandler.getStackInSlot(0);
        if (stack.isEmpty()) {
            processingTicks = 0;
            maxTicks = 0;
            return;
        }

        Optional<RecipeHolder<DigesterRecipe>> match = level.getRecipeManager()
                .getRecipeFor(HydroponiCraftRegistry.DIGESTING_RECIPE_TYPE.get(),
                        new SingleRecipeInput(stack), level);
        if (match.isEmpty()) {
            processingTicks = 0;
            maxTicks = 0;
            return;
        }

        DigesterRecipe recipe = match.get().value();
        net.minecraft.world.level.material.Fluid outputFluid = recipe.resolveOutputFluid();

        // If the tank already holds a different fluid, wait until it drains
        if (!fluidTank.isEmpty() && fluidTank.getFluid().getFluid() != outputFluid) {
            processingTicks = 0;
            maxTicks = 0;
            return;
        }
        if (fluidTank.getFluidAmount() + recipe.fluidOutputMb() > fluidTank.getCapacity()) {
            processingTicks = 0;
            maxTicks = 0;
            return;
        }

        // Speed scaling: base ticks assume 16 RPM; higher RPM → fewer ticks.
        maxTicks = Math.max(1, (int) (recipe.baseTicks() * 16.0 / Math.abs(getSpeed())));
        processingTicks++;

        if (processingTicks >= maxTicks) {
            processingTicks = 0;
            itemHandler.extractItem(0, 1, false);
            fluidTank.fill(new FluidStack(outputFluid, recipe.fluidOutputMb()),
                    IFluidHandler.FluidAction.EXECUTE);
            setChanged();
        }
    }

    // ------------------------------------------------------------------
    // NBT persistence
    // ------------------------------------------------------------------

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        tag.put("FluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("ProcessingTicks", processingTicks);
        tag.putInt("MaxTicks", maxTicks);
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
        processingTicks = tag.getInt("ProcessingTicks");
        maxTicks = tag.getInt("MaxTicks");
    }
}
