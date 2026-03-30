package com.hydroponicraft.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class MixerRecipeSerializer implements RecipeSerializer<MixerRecipe> {

    @Override
    public MapCodec<MixerRecipe> codec() {
        return MixerRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MixerRecipe> streamCodec() {
        return MixerRecipe.STREAM_CODEC;
    }
}
