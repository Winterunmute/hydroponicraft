package com.hydroponicraft.recipe;

import com.hydroponicraft.HydroponiCraftFluids;
import com.hydroponicraft.HydroponiCraftRegistry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import java.util.Optional;

public record DigesterRecipe(
        Ingredient ingredient,
        int fluidOutputMb,
        int baseTicks,
        Optional<ResourceLocation> outputFluid
) implements Recipe<SingleRecipeInput> {

    /**
     * Resolves the output fluid from the optional field.
     * Falls back to nutrient_fluid if not specified (all existing recipes).
     */
    public Fluid resolveOutputFluid() {
        return outputFluid
                .flatMap(id -> BuiltInRegistries.FLUID.getOptional(id))
                .orElse(HydroponiCraftFluids.NUTRIENT_FLUID.get());
    }

    public static final MapCodec<DigesterRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(DigesterRecipe::ingredient),
            net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("fluid_output_mb").forGetter(DigesterRecipe::fluidOutputMb),
            net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("base_ticks").forGetter(DigesterRecipe::baseTicks),
            ResourceLocation.CODEC.optionalFieldOf("output_fluid").forGetter(DigesterRecipe::outputFluid)
    ).apply(inst, DigesterRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DigesterRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, DigesterRecipe::ingredient,
                    ByteBufCodecs.VAR_INT, DigesterRecipe::fluidOutputMb,
                    ByteBufCodecs.VAR_INT, DigesterRecipe::baseTicks,
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), DigesterRecipe::outputFluid,
                    DigesterRecipe::new
            );

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
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
        return HydroponiCraftRegistry.DIGESTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HydroponiCraftRegistry.DIGESTING_RECIPE_TYPE.get();
    }
}
