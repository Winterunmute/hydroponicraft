package com.hydroponicraft;

import com.hydroponicraft.block.DigesterBlock;
import com.hydroponicraft.block.GrowthBedBlock;
import com.hydroponicraft.block.MixerBlock;
import com.hydroponicraft.blockentity.DigesterBlockEntity;
import com.hydroponicraft.blockentity.GrowthBedBlockEntity;
import com.hydroponicraft.blockentity.MixerBlockEntity;
import com.hydroponicraft.recipe.DigesterRecipe;
import com.hydroponicraft.recipe.DigesterRecipeSerializer;
import com.hydroponicraft.recipe.MixerRecipe;
import com.hydroponicraft.recipe.MixerRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class HydroponiCraftRegistry {

    // -------------------------------------------------------------------------
    // DeferredRegisters
    // -------------------------------------------------------------------------

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, HydroponiCraftMod.MOD_ID);

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(HydroponiCraftMod.MOD_ID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(HydroponiCraftMod.MOD_ID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, HydroponiCraftMod.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HydroponiCraftMod.MOD_ID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, HydroponiCraftMod.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, HydroponiCraftMod.MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HydroponiCraftMod.MOD_ID);

    // -------------------------------------------------------------------------
    // Digester
    // -------------------------------------------------------------------------

    public static final DeferredHolder<Block, DigesterBlock> DIGESTER_BLOCK =
            BLOCKS.register("digester", () -> new DigesterBlock(
                    BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(3.5f)));

    public static final DeferredHolder<Item, BlockItem> DIGESTER_ITEM =
            ITEMS.register("digester", () -> new BlockItem(DIGESTER_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DigesterBlockEntity>> DIGESTER_BE =
            BLOCK_ENTITIES.register("digester", () -> BlockEntityType.Builder
                    .of(DigesterBlockEntity::new, DIGESTER_BLOCK.get())
                    .build(null));

    // -------------------------------------------------------------------------
    // Growth Bed
    // -------------------------------------------------------------------------

    public static final DeferredHolder<Block, GrowthBedBlock> GROWTH_BED_BLOCK =
            BLOCKS.register("growth_bed", () -> new GrowthBedBlock(
                    BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(3.5f)));

    public static final DeferredHolder<Item, BlockItem> GROWTH_BED_ITEM =
            ITEMS.register("growth_bed", () -> new BlockItem(GROWTH_BED_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrowthBedBlockEntity>> GROWTH_BED_BE =
            BLOCK_ENTITIES.register("growth_bed", () -> BlockEntityType.Builder
                    .of(GrowthBedBlockEntity::new, GROWTH_BED_BLOCK.get())
                    .build(null));

    // -------------------------------------------------------------------------
    // Mixer
    // -------------------------------------------------------------------------

    public static final DeferredHolder<Block, MixerBlock> MIXER_BLOCK =
            BLOCKS.register("mixer", () -> new MixerBlock(
                    BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(3.5f)));

    public static final DeferredHolder<Item, BlockItem> MIXER_ITEM =
            ITEMS.register("mixer", () -> new BlockItem(MIXER_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MixerBlockEntity>> MIXER_BE =
            BLOCK_ENTITIES.register("mixer", () -> BlockEntityType.Builder
                    .of(MixerBlockEntity::new, MIXER_BLOCK.get())
                    .build(null));

    // -------------------------------------------------------------------------
    // Recipes
    // -------------------------------------------------------------------------

    public static final DeferredHolder<RecipeType<?>, RecipeType<DigesterRecipe>> DIGESTING_RECIPE_TYPE =
            RECIPE_TYPES.register("digesting", () -> new RecipeType<DigesterRecipe>() {
                @Override
                public String toString() { return HydroponiCraftMod.MOD_ID + ":digesting"; }
            });

    public static final DeferredHolder<RecipeSerializer<?>, DigesterRecipeSerializer> DIGESTING_SERIALIZER =
            RECIPE_SERIALIZERS.register("digesting", DigesterRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<MixerRecipe>> MIXING_RECIPE_TYPE =
            RECIPE_TYPES.register("mixing", () -> new RecipeType<MixerRecipe>() {
                @Override
                public String toString() { return HydroponiCraftMod.MOD_ID + ":mixing"; }
            });

    public static final DeferredHolder<RecipeSerializer<?>, MixerRecipeSerializer> MIXING_SERIALIZER =
            RECIPE_SERIALIZERS.register("mixing", MixerRecipeSerializer::new);

    // -------------------------------------------------------------------------
    // Creative tab
    // -------------------------------------------------------------------------

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB =
            CREATIVE_TABS.register("hydroponicraft", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hydroponicraft"))
                    .icon(() -> new ItemStack(GROWTH_BED_ITEM.get()))
                    .build());

    // -------------------------------------------------------------------------
    // Bus registration
    // -------------------------------------------------------------------------

    public static void register(IEventBus bus) {
        FLUID_TYPES.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        FLUIDS.register(bus);
        BLOCK_ENTITIES.register(bus);
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        CREATIVE_TABS.register(bus);
    }
}
