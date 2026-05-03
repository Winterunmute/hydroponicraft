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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public record ChemicalSynthesizerRecipe(
        Ingredient ingredient,
        ResourceLocation fluidId,
        int fluidAmountMb,
        ResourceLocation resultItemId,
        int resultCount,
        int baseTicks
) implements Recipe<ChemicalSynthesizerRecipe.Input> {

    // ------------------------------------------------------------------
    // Recipe input — item in slot 0 + current fluid in tank
    // ------------------------------------------------------------------

    public record Input(ItemStack item, FluidStack fluid) implements RecipeInput {
        @Override
        public ItemStack getItem(int slot) { return slot == 0 ? item : ItemStack.EMPTY; }
        @Override
        public int size() { return 1; }
    }

    // ------------------------------------------------------------------
    // Result helper
    // ------------------------------------------------------------------

    public ItemStack buildResult() {
        return BuiltInRegistries.ITEM.getOptional(resultItemId)
                .map(item -> new ItemStack(item, resultCount))
                .orElse(ItemStack.EMPTY);
    }

    // ------------------------------------------------------------------
    // Codecs
    // ------------------------------------------------------------------

    public static final MapCodec<ChemicalSynthesizerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(ChemicalSynthesizerRecipe::ingredient),
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(ChemicalSynthesizerRecipe::fluidId),
            Codec.INT.fieldOf("fluid_amount_mb").forGetter(ChemicalSynthesizerRecipe::fluidAmountMb),
            ResourceLocation.CODEC.fieldOf("result_item").forGetter(ChemicalSynthesizerRecipe::resultItemId),
            Codec.INT.optionalFieldOf("result_count", 1).forGetter(ChemicalSynthesizerRecipe::resultCount),
            ExtraCodecs.POSITIVE_INT.fieldOf("base_ticks").forGetter(ChemicalSynthesizerRecipe::baseTicks)
    ).apply(inst, ChemicalSynthesizerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalSynthesizerRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC,   ChemicalSynthesizerRecipe::ingredient,
                    ResourceLocation.STREAM_CODEC,      ChemicalSynthesizerRecipe::fluidId,
                    ByteBufCodecs.VAR_INT,              ChemicalSynthesizerRecipe::fluidAmountMb,
                    ResourceLocation.STREAM_CODEC,      ChemicalSynthesizerRecipe::resultItemId,
                    ByteBufCodecs.VAR_INT,              ChemicalSynthesizerRecipe::resultCount,
                    ByteBufCodecs.VAR_INT,              ChemicalSynthesizerRecipe::baseTicks,
                    ChemicalSynthesizerRecipe::new);

    // ------------------------------------------------------------------
    // Recipe contract
    // ------------------------------------------------------------------

    @Override
    public boolean matches(Input input, Level level) {
        if (!ingredient.test(input.item())) return false;
        return BuiltInRegistries.FLUID.getOptional(fluidId)
                .map(f -> input.fluid().getFluid() == f && input.fluid().getAmount() >= fluidAmountMb)
                .orElse(false);
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return buildResult();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return buildResult();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return HydroponiCraftRegistry.SYNTHESIZING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HydroponiCraftRegistry.SYNTHESIZING_RECIPE_TYPE.get();
    }
}
