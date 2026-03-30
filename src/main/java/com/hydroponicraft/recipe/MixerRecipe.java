package com.hydroponicraft.recipe;

import com.hydroponicraft.HydroponiCraftRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Optional;

public record MixerRecipe(
        FluidIngredient input1,
        FluidIngredient input2,
        Optional<FluidIngredient> input3,
        FluidIngredient output
) implements Recipe<MixerRecipe.Input> {

    // ------------------------------------------------------------------
    // Fluid input wrapper — holds the current tank contents for matching
    // ------------------------------------------------------------------

    public record Input(FluidStack tank1, FluidStack tank2, FluidStack tank3) implements RecipeInput {
        @Override
        public ItemStack getItem(int slot) { return ItemStack.EMPTY; }
        @Override
        public int size() { return 0; }
    }

    // ------------------------------------------------------------------
    // FluidIngredient — a fluid + minimum amount required
    // ------------------------------------------------------------------

    public record FluidIngredient(ResourceLocation fluidId, int amount) {

        static final Codec<FluidIngredient> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("fluid").forGetter(FluidIngredient::fluidId),
                Codec.INT.fieldOf("amount").forGetter(FluidIngredient::amount)
        ).apply(inst, FluidIngredient::new));

        static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC, FluidIngredient::fluidId,
                        ByteBufCodecs.VAR_INT, FluidIngredient::amount,
                        FluidIngredient::new);

        public FluidStack toFluidStack() {
            return BuiltInRegistries.FLUID.getOptional(fluidId)
                    .map(f -> new FluidStack(f, amount))
                    .orElse(FluidStack.EMPTY);
        }

        public boolean matches(FluidStack stack) {
            return BuiltInRegistries.FLUID.getOptional(fluidId)
                    .map(f -> stack.getFluid() == f && stack.getAmount() >= amount)
                    .orElse(false);
        }
    }

    // ------------------------------------------------------------------
    // Codecs
    // ------------------------------------------------------------------

    public static final MapCodec<MixerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            FluidIngredient.CODEC.fieldOf("input1").forGetter(MixerRecipe::input1),
            FluidIngredient.CODEC.fieldOf("input2").forGetter(MixerRecipe::input2),
            FluidIngredient.CODEC.optionalFieldOf("input3").forGetter(MixerRecipe::input3),
            FluidIngredient.CODEC.fieldOf("output").forGetter(MixerRecipe::output)
    ).apply(inst, MixerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MixerRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    FluidIngredient.STREAM_CODEC, MixerRecipe::input1,
                    FluidIngredient.STREAM_CODEC, MixerRecipe::input2,
                    ByteBufCodecs.optional(FluidIngredient.STREAM_CODEC), MixerRecipe::input3,
                    FluidIngredient.STREAM_CODEC, MixerRecipe::output,
                    MixerRecipe::new);

    // ------------------------------------------------------------------
    // Recipe contract
    // ------------------------------------------------------------------

    @Override
    public boolean matches(Input input, Level level) {
        if (!input1.matches(input.tank1())) return false;
        if (!input2.matches(input.tank2())) return false;
        if (input3.isPresent() && !input3.get().matches(input.tank3())) return false;
        return true;
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return HydroponiCraftRegistry.MIXING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HydroponiCraftRegistry.MIXING_RECIPE_TYPE.get();
    }
}
