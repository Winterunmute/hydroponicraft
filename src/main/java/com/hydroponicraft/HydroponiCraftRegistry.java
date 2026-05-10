package com.hydroponicraft;

import com.hydroponicraft.block.C4Block;
import com.hydroponicraft.block.EnderC4Block;
import com.hydroponicraft.block.GatheringChestBlock;
import com.hydroponicraft.menu.GatheringChestMenu;
import com.hydroponicraft.block.RedstoneDetonatorBlock;
import com.hydroponicraft.blockentity.C4BlockEntity;
import com.hydroponicraft.blockentity.EnderC4BlockEntity;
import com.hydroponicraft.blockentity.GatheringChestBlockEntity;
import com.hydroponicraft.blockentity.RedstoneDetonatorBlockEntity;
import com.hydroponicraft.entity.EnderPearlLauncherCart;
import com.hydroponicraft.item.EnderPearlLauncherCartItem;
import com.hydroponicraft.item.EnderC4Launcher;
import com.hydroponicraft.item.FilterTemplate;
import com.hydroponicraft.item.RemoteDetonator;
import com.hydroponicraft.block.ChemicalSynthesizerBlock;
import com.hydroponicraft.block.DigesterBlock;
import com.hydroponicraft.block.GrowthBedBlock;
import com.hydroponicraft.block.MixerBlock;
import com.hydroponicraft.blockentity.ChemicalSynthesizerBlockEntity;
import com.hydroponicraft.blockentity.DigesterBlockEntity;
import com.hydroponicraft.blockentity.GrowthBedBlockEntity;
import com.hydroponicraft.blockentity.MixerBlockEntity;
import com.hydroponicraft.recipe.ChemicalSynthesizerRecipe;
import com.hydroponicraft.recipe.ChemicalSynthesizerRecipeSerializer;
import com.hydroponicraft.recipe.DigesterRecipe;
import com.hydroponicraft.recipe.DigesterRecipeSerializer;
import com.hydroponicraft.recipe.MixerRecipe;
import com.hydroponicraft.recipe.MixerRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
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

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, HydroponiCraftMod.MOD_ID);

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, HydroponiCraftMod.MOD_ID);

    // -------------------------------------------------------------------------
    // Chemical Synthesizer
    // -------------------------------------------------------------------------

    public static final DeferredHolder<Block, ChemicalSynthesizerBlock> CHEMICAL_SYNTHESIZER_BLOCK =
            BLOCKS.register("chemical_synthesizer", () -> new ChemicalSynthesizerBlock(
                    BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(3.5f)));

    public static final DeferredHolder<Item, BlockItem> CHEMICAL_SYNTHESIZER_ITEM =
            ITEMS.register("chemical_synthesizer", () -> new BlockItem(CHEMICAL_SYNTHESIZER_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalSynthesizerBlockEntity>> CHEMICAL_SYNTHESIZER_BE =
            BLOCK_ENTITIES.register("chemical_synthesizer", () -> BlockEntityType.Builder
                    .of(ChemicalSynthesizerBlockEntity::new, CHEMICAL_SYNTHESIZER_BLOCK.get())
                    .build(null));

    // -------------------------------------------------------------------------
    // Remote Detonator
    // -------------------------------------------------------------------------

    public static final DeferredHolder<Item, RemoteDetonator> REMOTE_DETONATOR =
            ITEMS.register("remote_detonator", () -> new RemoteDetonator(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, FilterTemplate> FILTER_TEMPLATE =
            ITEMS.register("filter_template", () -> new FilterTemplate(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, EnderC4Launcher> ENDER_C4_LAUNCHER =
            ITEMS.register("ender_c4_launcher", () -> new EnderC4Launcher(new Item.Properties().stacksTo(1)));

    // -------------------------------------------------------------------------
    // C4 block + item
    // -------------------------------------------------------------------------

    public static final DeferredHolder<Block, C4Block> C4_BLOCK =
            BLOCKS.register("c4", () -> new C4Block(
                    BlockBehaviour.Properties.of().strength(0.5f).noOcclusion()));

    public static final DeferredHolder<Item, BlockItem> C4_ITEM =
            ITEMS.register("c4", () -> new BlockItem(C4_BLOCK.get(), new Item.Properties()));

    // -------------------------------------------------------------------------
    // Colored C4 variants (16 dye colors — functionally identical to base C4)
    // -------------------------------------------------------------------------

    public static final Map<DyeColor, DeferredHolder<Block, C4Block>>    COLORED_C4_BLOCKS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredHolder<Item, BlockItem>>   COLORED_C4_ITEMS  = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            String id = "colored_c4_" + color.getName();
            DeferredHolder<Block, C4Block> bh = BLOCKS.register(id,
                    () -> new C4Block(BlockBehaviour.Properties.of().strength(0.5f).noOcclusion()));
            DeferredHolder<Item, BlockItem> ih = ITEMS.register(id,
                    () -> new BlockItem(bh.get(), new Item.Properties()));
            COLORED_C4_BLOCKS.put(color, bh);
            COLORED_C4_ITEMS.put(color, ih);
        }
    }

    // C4 BlockEntityType — covers base C4 and all 16 colored variants.
    // Declared after the static block above so COLORED_C4_BLOCKS is populated before the lambda runs.
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<C4BlockEntity>> C4_BE =
            BLOCK_ENTITIES.register("c4", () -> {
                C4Block[] blocks = new C4Block[1 + DyeColor.values().length];
                blocks[0] = C4_BLOCK.get();
                int i = 1;
                for (DyeColor color : DyeColor.values()) {
                    blocks[i++] = COLORED_C4_BLOCKS.get(color).get();
                }
                return BlockEntityType.Builder.of(C4BlockEntity::new, blocks).build(null);
            });

    // -------------------------------------------------------------------------
    // Ender C4 block + item
    // -------------------------------------------------------------------------

    public static final DeferredHolder<Block, EnderC4Block> ENDER_C4_BLOCK =
            BLOCKS.register("ender_c4", () -> new EnderC4Block(
                    BlockBehaviour.Properties.of().strength(0.5f).noOcclusion(), null));

    public static final DeferredHolder<Item, BlockItem> ENDER_C4_ITEM =
            ITEMS.register("ender_c4", () -> new BlockItem(ENDER_C4_BLOCK.get(), new Item.Properties()));

    // -------------------------------------------------------------------------
    // Colored Ender C4 variants (16 dye colors)
    // -------------------------------------------------------------------------

    public static final Map<DyeColor, DeferredHolder<Block, EnderC4Block>> COLORED_ENDER_C4_BLOCKS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredHolder<Item, BlockItem>>     COLORED_ENDER_C4_ITEMS  = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            String id = "colored_ender_c4_" + color.getName();
            final DyeColor finalColor = color;
            DeferredHolder<Block, EnderC4Block> bh = BLOCKS.register(id,
                    () -> new EnderC4Block(BlockBehaviour.Properties.of().strength(0.5f).noOcclusion(), finalColor));
            DeferredHolder<Item, BlockItem> ih = ITEMS.register(id,
                    () -> new BlockItem(bh.get(), new Item.Properties()));
            COLORED_ENDER_C4_BLOCKS.put(color, bh);
            COLORED_ENDER_C4_ITEMS.put(color, ih);
        }
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnderC4BlockEntity>> ENDER_C4_BE =
            BLOCK_ENTITIES.register("ender_c4", () -> {
                EnderC4Block[] blocks = new EnderC4Block[1 + DyeColor.values().length];
                blocks[0] = ENDER_C4_BLOCK.get();
                int i = 1;
                for (DyeColor color : DyeColor.values()) {
                    blocks[i++] = COLORED_ENDER_C4_BLOCKS.get(color).get();
                }
                return BlockEntityType.Builder.of(EnderC4BlockEntity::new, blocks).build(null);
            });

    // -------------------------------------------------------------------------
    // Gathering Chest
    // -------------------------------------------------------------------------

    public static final DeferredHolder<Block, GatheringChestBlock> GATHERING_CHEST_BLOCK =
            BLOCKS.register("gathering_chest", () -> new GatheringChestBlock(
                    BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(3.5f)));

    public static final DeferredHolder<Item, BlockItem> GATHERING_CHEST_ITEM =
            ITEMS.register("gathering_chest", () -> new BlockItem(GATHERING_CHEST_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GatheringChestBlockEntity>> GATHERING_CHEST_BE =
            BLOCK_ENTITIES.register("gathering_chest", () -> BlockEntityType.Builder
                    .of(GatheringChestBlockEntity::new, GATHERING_CHEST_BLOCK.get())
                    .build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<GatheringChestMenu>> GATHERING_CHEST_MENU =
            MENUS.register("gathering_chest", () -> IMenuTypeExtension.create(GatheringChestMenu::new));

    // -------------------------------------------------------------------------
    // Redstone Detonator
    // -------------------------------------------------------------------------

    public static final DeferredHolder<Block, RedstoneDetonatorBlock> REDSTONE_DETONATOR_BLOCK =
            BLOCKS.register("redstone_detonator", () -> new RedstoneDetonatorBlock(
                    BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(3.5f)));

    public static final DeferredHolder<Item, BlockItem> REDSTONE_DETONATOR_ITEM =
            ITEMS.register("redstone_detonator", () -> new BlockItem(REDSTONE_DETONATOR_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneDetonatorBlockEntity>> REDSTONE_DETONATOR_BE =
            BLOCK_ENTITIES.register("redstone_detonator", () -> BlockEntityType.Builder
                    .of(RedstoneDetonatorBlockEntity::new, REDSTONE_DETONATOR_BLOCK.get())
                    .build(null));

    // -------------------------------------------------------------------------
    // Ender Pearl Launcher Cart entity + spawner item
    // -------------------------------------------------------------------------

    public static final DeferredHolder<EntityType<?>, EntityType<EnderPearlLauncherCart>> ENDER_PEARL_LAUNCHER_CART_TYPE =
            ENTITY_TYPES.register("ender_pearl_launcher_cart", () ->
                    EntityType.Builder.<EnderPearlLauncherCart>of(EnderPearlLauncherCart::new, MobCategory.MISC)
                            .sized(0.98f, 0.7f)
                            .build("ender_pearl_launcher_cart"));

    public static final DeferredHolder<Item, EnderPearlLauncherCartItem> ENDER_PEARL_LAUNCHER_CART_ITEM =
            ITEMS.register("ender_pearl_launcher_cart", () -> new EnderPearlLauncherCartItem(new Item.Properties().stacksTo(1)));

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

    public static final DeferredHolder<RecipeType<?>, RecipeType<ChemicalSynthesizerRecipe>> SYNTHESIZING_RECIPE_TYPE =
            RECIPE_TYPES.register("synthesizing", () -> new RecipeType<ChemicalSynthesizerRecipe>() {
                @Override
                public String toString() { return HydroponiCraftMod.MOD_ID + ":synthesizing"; }
            });

    public static final DeferredHolder<RecipeSerializer<?>, ChemicalSynthesizerRecipeSerializer> SYNTHESIZING_SERIALIZER =
            RECIPE_SERIALIZERS.register("synthesizing", ChemicalSynthesizerRecipeSerializer::new);

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
        ENTITY_TYPES.register(bus);
        MENUS.register(bus);
    }
}
