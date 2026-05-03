package com.hydroponicraft.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ChemicalSynthesizerRecipeSerializer implements RecipeSerializer<ChemicalSynthesizerRecipe> {

    @Override
    public MapCodec<ChemicalSynthesizerRecipe> codec() {
        return ChemicalSynthesizerRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ChemicalSynthesizerRecipe> streamCodec() {
        return ChemicalSynthesizerRecipe.STREAM_CODEC;
    }
}
