package com.hydroponicraft.blockentity;

import com.hydroponicraft.HydroponiCraftRegistry;
import com.hydroponicraft.recipe.MixerRecipe;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class MixerBlockEntity extends KineticBlockEntity {

    /** Tank 1: first fluid input, max 4 000 mB. */
    public final FluidTank inputTank1 = new FluidTank(4_000);

    /** Tank 2: second fluid input, max 4 000 mB. */
    public final FluidTank inputTank2 = new FluidTank(4_000);

    /** Output tank: target solution, max 8 000 mB. */
    public final FluidTank outputTank = new FluidTank(8_000);

    /**
     * Combined handler for both input tanks — exposed on all horizontal faces.
     * Routes each fluid to the tank already holding that type; falls back to tank1 first.
     */
    public final IFluidHandler inputHandler = new IFluidHandler() {
        @Override public int getTanks() { return 2; }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? inputTank1.getFluid() : inputTank2.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? inputTank1.getCapacity() : inputTank2.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) { return true; }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            // Prefer the tank already holding this fluid type
            if (!inputTank1.isEmpty() && inputTank1.getFluid().getFluid() == resource.getFluid()) {
                return inputTank1.fill(resource, action);
            }
            if (!inputTank2.isEmpty() && inputTank2.getFluid().getFluid() == resource.getFluid()) {
                return inputTank2.fill(resource, action);
            }
            // Both empty or neither matches — fill tank1, overflow to tank2
            int filled = inputTank1.fill(resource, action);
            if (filled < resource.getAmount()) {
                FluidStack remaining = new FluidStack(resource.getFluid(), resource.getAmount() - filled);
                filled += inputTank2.fill(remaining, action);
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    };

    /** Ticks remaining before the next processing attempt. */
    private int cooldown = 0;

    public MixerBlockEntity(BlockPos pos, BlockState state) {
        super(HydroponiCraftRegistry.MIXER_BE.get(), pos, state);
    }

    // ------------------------------------------------------------------
    // Ticking
    // ------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) return;
        if (!isSpeedRequirementFulfilled() || Math.abs(getSpeed()) < 1f) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        tryProcess();
    }

    private void tryProcess() {
        for (RecipeHolder<MixerRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(HydroponiCraftRegistry.MIXING_RECIPE_TYPE.get())) {
            MixerRecipe recipe = holder.value();

            FluidStack output = recipe.output().toFluidStack();
            if (output.isEmpty()) continue;
            if (outputTank.fill(output, IFluidHandler.FluidAction.SIMULATE) < output.getAmount()) continue;

            // Check both tank orderings so pipe connection direction doesn't matter
            boolean normalOrder  = recipe.input1().matches(inputTank1.getFluid())
                                && recipe.input2().matches(inputTank2.getFluid());
            boolean swappedOrder = !normalOrder
                                && recipe.input1().matches(inputTank2.getFluid())
                                && recipe.input2().matches(inputTank1.getFluid());

            if (!normalOrder && !swappedOrder) continue;

            if (normalOrder) {
                inputTank1.drain(recipe.input1().amount(), IFluidHandler.FluidAction.EXECUTE);
                inputTank2.drain(recipe.input2().amount(), IFluidHandler.FluidAction.EXECUTE);
            } else {
                inputTank2.drain(recipe.input1().amount(), IFluidHandler.FluidAction.EXECUTE);
                inputTank1.drain(recipe.input2().amount(), IFluidHandler.FluidAction.EXECUTE);
            }

            outputTank.fill(output, IFluidHandler.FluidAction.EXECUTE);
            setChanged();
            cooldown = Math.max(1, (int) (20.0 * 16.0 / Math.abs(getSpeed())));
            return;
        }
    }

    // ------------------------------------------------------------------
    // NBT persistence
    // ------------------------------------------------------------------

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("InputTank1", inputTank1.writeToNBT(registries, new CompoundTag()));
        tag.put("InputTank2", inputTank2.writeToNBT(registries, new CompoundTag()));
        tag.put("OutputTank", outputTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Cooldown", cooldown);
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        inputTank1.readFromNBT(registries, tag.getCompound("InputTank1"));
        inputTank2.readFromNBT(registries, tag.getCompound("InputTank2"));
        outputTank.readFromNBT(registries, tag.getCompound("OutputTank"));
        cooldown = tag.getInt("Cooldown");
    }
}
