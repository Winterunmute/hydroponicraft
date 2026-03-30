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

import java.util.Optional;

public class MixerBlockEntity extends KineticBlockEntity {

    /** Tank 1: first required fluid input (e.g. Nutrient Fluid), max 4 000 mB. */
    public final FluidTank inputTank1 = new FluidTank(4_000);

    /** Tank 2: second required fluid input (e.g. Water), max 4 000 mB. */
    public final FluidTank inputTank2 = new FluidTank(4_000);

    /** Tank 3: optional additive fluid input, max 4 000 mB. */
    public final FluidTank inputTank3 = new FluidTank(4_000);

    /** Output tank: target solution, max 8 000 mB. */
    public final FluidTank outputTank = new FluidTank(8_000);

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
        MixerRecipe.Input input = new MixerRecipe.Input(
                inputTank1.getFluid(), inputTank2.getFluid(), inputTank3.getFluid());

        Optional<RecipeHolder<MixerRecipe>> match = level.getRecipeManager()
                .getRecipeFor(HydroponiCraftRegistry.MIXING_RECIPE_TYPE.get(), input, level);
        if (match.isEmpty()) return;

        MixerRecipe recipe = match.get().value();
        FluidStack output = recipe.output().toFluidStack();

        if (output.isEmpty()) return;
        if (outputTank.fill(output, IFluidHandler.FluidAction.SIMULATE) < output.getAmount()) return;

        // Consume inputs
        inputTank1.drain(recipe.input1().amount(), IFluidHandler.FluidAction.EXECUTE);
        inputTank2.drain(recipe.input2().amount(), IFluidHandler.FluidAction.EXECUTE);
        recipe.input3().ifPresent(in3 -> inputTank3.drain(in3.amount(), IFluidHandler.FluidAction.EXECUTE));

        // Produce output
        outputTank.fill(output, IFluidHandler.FluidAction.EXECUTE);
        setChanged();

        // Scale cooldown by RPM — base 20 ticks at 16 RPM
        cooldown = Math.max(1, (int) (20.0 * 16.0 / Math.abs(getSpeed())));
    }

    // ------------------------------------------------------------------
    // NBT persistence
    // ------------------------------------------------------------------

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("InputTank1", inputTank1.writeToNBT(registries, new CompoundTag()));
        tag.put("InputTank2", inputTank2.writeToNBT(registries, new CompoundTag()));
        tag.put("InputTank3", inputTank3.writeToNBT(registries, new CompoundTag()));
        tag.put("OutputTank", outputTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Cooldown", cooldown);
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        inputTank1.readFromNBT(registries, tag.getCompound("InputTank1"));
        inputTank2.readFromNBT(registries, tag.getCompound("InputTank2"));
        inputTank3.readFromNBT(registries, tag.getCompound("InputTank3"));
        outputTank.readFromNBT(registries, tag.getCompound("OutputTank"));
        cooldown = tag.getInt("Cooldown");
    }
}
