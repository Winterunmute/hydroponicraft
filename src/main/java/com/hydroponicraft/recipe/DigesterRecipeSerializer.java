package com.hydroponicraft.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class DigesterRecipeSerializer implements RecipeSerializer<DigesterRecipe> {

    @Override
    public MapCodec<DigesterRecipe> codec() {
        return DigesterRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, DigesterRecipe> streamCodec() {
        return DigesterRecipe.STREAM_CODEC;
    }
}
