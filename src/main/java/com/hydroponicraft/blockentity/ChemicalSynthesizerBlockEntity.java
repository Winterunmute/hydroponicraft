package com.hydroponicraft.blockentity;

import com.hydroponicraft.HydroponiCraftRegistry;
import com.hydroponicraft.recipe.ChemicalSynthesizerRecipe;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

import java.util.Optional;

public class ChemicalSynthesizerBlockEntity extends KineticBlockEntity {

    /**
     * Slot 0: item input — fed by hopper from above.
     * Slot 1: item output — drained by hopper from below.
     * Slot 1 rejects external insertion via isItemValid.
     */
    public final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0;
        }
    };

    /** Input slot exposed on Direction.UP — hoppers insert ingredients here. */
    public final RangedWrapper inputSlotWrapper  = new RangedWrapper(itemHandler, 0, 1);

    /** Output slot exposed on Direction.DOWN — hoppers extract results here. */
    public final RangedWrapper outputSlotWrapper = new RangedWrapper(itemHandler, 1, 2);

    /** Fluid input tank — 4 000 mB, exposed on horizontal sides (except shaft face). */
    public final FluidTank fluidTank = new FluidTank(4_000);

    private int processingTicks = 0;
    private int maxTicks        = 0;

    public ChemicalSynthesizerBlockEntity(BlockPos pos, BlockState state) {
        super(HydroponiCraftRegistry.CHEMICAL_SYNTHESIZER_BE.get(), pos, state);
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
    }

    private void processRecipe() {
        ItemStack inputStack = itemHandler.getStackInSlot(0);
        if (inputStack.isEmpty() || fluidTank.isEmpty()) {
            reset();
            return;
        }

        ChemicalSynthesizerRecipe.Input recipeInput =
                new ChemicalSynthesizerRecipe.Input(inputStack, fluidTank.getFluid());

        Optional<RecipeHolder<ChemicalSynthesizerRecipe>> match = level.getRecipeManager()
                .getRecipeFor(HydroponiCraftRegistry.SYNTHESIZING_RECIPE_TYPE.get(), recipeInput, level);

        if (match.isEmpty()) {
            reset();
            return;
        }

        ChemicalSynthesizerRecipe recipe = match.get().value();
        ItemStack result = recipe.buildResult();
        if (result.isEmpty()) {
            reset();
            return;
        }

        // Check the output slot can accept the result
        ItemStack outputStack = itemHandler.getStackInSlot(1);
        if (!outputStack.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(outputStack, result)) {
                reset();
                return;
            }
            if (outputStack.getCount() + result.getCount() > outputStack.getMaxStackSize()) {
                reset();
                return;
            }
        }

        // Speed scaling: base ticks assume 16 RPM; higher RPM → fewer ticks.
        maxTicks = Math.max(1, (int) (recipe.baseTicks() * 16.0 / Math.abs(getSpeed())));
        processingTicks++;

        if (processingTicks >= maxTicks) {
            processingTicks = 0;
            itemHandler.extractItem(0, 1, false);
            fluidTank.drain(recipe.fluidAmountMb(), IFluidHandler.FluidAction.EXECUTE);
            itemHandler.insertItem(1, result, false);
            setChanged();
        }
    }

    private void reset() {
        processingTicks = 0;
        maxTicks = 0;
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
