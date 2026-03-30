package com.hydroponicraft;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Consumer;

public class HydroponiCraftFluids {

    // -------------------------------------------------------------------------
    // Properties — declared first as non-final, assigned in the static block
    // below after all DeferredHolders exist (avoids illegal forward references).
    // -------------------------------------------------------------------------

    public static BaseFlowingFluid.Properties NUTRIENT_FLUID_PROPS;
    public static BaseFlowingFluid.Properties NUTRIENT_SOLUTION_PROPS;
    public static BaseFlowingFluid.Properties ENRICHED_SOLUTION_PROPS;
    public static BaseFlowingFluid.Properties YIELD_TONIC_PROPS;
    public static BaseFlowingFluid.Properties ACCELERANT_SOLUTION_PROPS;
    public static BaseFlowingFluid.Properties BALANCED_BLEND_PROPS;

    // -------------------------------------------------------------------------
    // Nutrient Fluid  (raw Digester output — earthy green)
    // -------------------------------------------------------------------------

    public static final DeferredHolder<FluidType, FluidType> NUTRIENT_FLUID_TYPE =
            HydroponiCraftRegistry.FLUID_TYPES.register("nutrient_fluid",
                    () -> makeType(0xFF5D8A3C));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> NUTRIENT_FLUID =
            HydroponiCraftRegistry.FLUIDS.register("nutrient_fluid",
                    () -> new BaseFlowingFluid.Source(NUTRIENT_FLUID_PROPS));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> NUTRIENT_FLUID_FLOWING =
            HydroponiCraftRegistry.FLUIDS.register("nutrient_fluid_flowing",
                    () -> new BaseFlowingFluid.Flowing(NUTRIENT_FLUID_PROPS));

    public static final DeferredHolder<net.minecraft.world.level.block.Block, LiquidBlock> NUTRIENT_FLUID_BLOCK =
            HydroponiCraftRegistry.BLOCKS.register("nutrient_fluid",
                    () -> new LiquidBlock(NUTRIENT_FLUID.get(), fluidBlockProps()));

    public static final DeferredHolder<Item, BucketItem> NUTRIENT_FLUID_BUCKET =
            HydroponiCraftRegistry.ITEMS.register("nutrient_fluid_bucket",
                    () -> new BucketItem(NUTRIENT_FLUID.get(), bucketProps()));

    // -------------------------------------------------------------------------
    // Nutrient Solution  (Nutrient + Water → 1.5x growth — light green)
    // -------------------------------------------------------------------------

    public static final DeferredHolder<FluidType, FluidType> NUTRIENT_SOLUTION_TYPE =
            HydroponiCraftRegistry.FLUID_TYPES.register("nutrient_solution",
                    () -> makeType(0xFF7CB342));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> NUTRIENT_SOLUTION =
            HydroponiCraftRegistry.FLUIDS.register("nutrient_solution",
                    () -> new BaseFlowingFluid.Source(NUTRIENT_SOLUTION_PROPS));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> NUTRIENT_SOLUTION_FLOWING =
            HydroponiCraftRegistry.FLUIDS.register("nutrient_solution_flowing",
                    () -> new BaseFlowingFluid.Flowing(NUTRIENT_SOLUTION_PROPS));

    public static final DeferredHolder<net.minecraft.world.level.block.Block, LiquidBlock> NUTRIENT_SOLUTION_BLOCK =
            HydroponiCraftRegistry.BLOCKS.register("nutrient_solution",
                    () -> new LiquidBlock(NUTRIENT_SOLUTION.get(), fluidBlockProps()));

    public static final DeferredHolder<Item, BucketItem> NUTRIENT_SOLUTION_BUCKET =
            HydroponiCraftRegistry.ITEMS.register("nutrient_solution_bucket",
                    () -> new BucketItem(NUTRIENT_SOLUTION.get(), bucketProps()));

    // -------------------------------------------------------------------------
    // Enriched Solution  (Nutrient + Nether Wart Fluid → 2.5x growth — purple)
    // -------------------------------------------------------------------------

    public static final DeferredHolder<FluidType, FluidType> ENRICHED_SOLUTION_TYPE =
            HydroponiCraftRegistry.FLUID_TYPES.register("enriched_solution",
                    () -> makeType(0xFF8E24AA));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> ENRICHED_SOLUTION =
            HydroponiCraftRegistry.FLUIDS.register("enriched_solution",
                    () -> new BaseFlowingFluid.Source(ENRICHED_SOLUTION_PROPS));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> ENRICHED_SOLUTION_FLOWING =
            HydroponiCraftRegistry.FLUIDS.register("enriched_solution_flowing",
                    () -> new BaseFlowingFluid.Flowing(ENRICHED_SOLUTION_PROPS));

    public static final DeferredHolder<net.minecraft.world.level.block.Block, LiquidBlock> ENRICHED_SOLUTION_BLOCK =
            HydroponiCraftRegistry.BLOCKS.register("enriched_solution",
                    () -> new LiquidBlock(ENRICHED_SOLUTION.get(), fluidBlockProps()));

    public static final DeferredHolder<Item, BucketItem> ENRICHED_SOLUTION_BUCKET =
            HydroponiCraftRegistry.ITEMS.register("enriched_solution_bucket",
                    () -> new BucketItem(ENRICHED_SOLUTION.get(), bucketProps()));

    // -------------------------------------------------------------------------
    // Yield Tonic  (Nutrient + Chorus Fluid → Fortune 2 — bright magenta)
    // -------------------------------------------------------------------------

    public static final DeferredHolder<FluidType, FluidType> YIELD_TONIC_TYPE =
            HydroponiCraftRegistry.FLUID_TYPES.register("yield_tonic",
                    () -> makeType(0xFFE040FB));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> YIELD_TONIC =
            HydroponiCraftRegistry.FLUIDS.register("yield_tonic",
                    () -> new BaseFlowingFluid.Source(YIELD_TONIC_PROPS));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> YIELD_TONIC_FLOWING =
            HydroponiCraftRegistry.FLUIDS.register("yield_tonic_flowing",
                    () -> new BaseFlowingFluid.Flowing(YIELD_TONIC_PROPS));

    public static final DeferredHolder<net.minecraft.world.level.block.Block, LiquidBlock> YIELD_TONIC_BLOCK =
            HydroponiCraftRegistry.BLOCKS.register("yield_tonic",
                    () -> new LiquidBlock(YIELD_TONIC.get(), fluidBlockProps()));

    public static final DeferredHolder<Item, BucketItem> YIELD_TONIC_BUCKET =
            HydroponiCraftRegistry.ITEMS.register("yield_tonic_bucket",
                    () -> new BucketItem(YIELD_TONIC.get(), bucketProps()));

    // -------------------------------------------------------------------------
    // Accelerant Solution  (1.5x Nutrient + Enriched → 4x growth — deep amber)
    // -------------------------------------------------------------------------

    public static final DeferredHolder<FluidType, FluidType> ACCELERANT_SOLUTION_TYPE =
            HydroponiCraftRegistry.FLUID_TYPES.register("accelerant_solution",
                    () -> makeType(0xFFFF6F00));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> ACCELERANT_SOLUTION =
            HydroponiCraftRegistry.FLUIDS.register("accelerant_solution",
                    () -> new BaseFlowingFluid.Source(ACCELERANT_SOLUTION_PROPS));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> ACCELERANT_SOLUTION_FLOWING =
            HydroponiCraftRegistry.FLUIDS.register("accelerant_solution_flowing",
                    () -> new BaseFlowingFluid.Flowing(ACCELERANT_SOLUTION_PROPS));

    public static final DeferredHolder<net.minecraft.world.level.block.Block, LiquidBlock> ACCELERANT_SOLUTION_BLOCK =
            HydroponiCraftRegistry.BLOCKS.register("accelerant_solution",
                    () -> new LiquidBlock(ACCELERANT_SOLUTION.get(), fluidBlockProps()));

    public static final DeferredHolder<Item, BucketItem> ACCELERANT_SOLUTION_BUCKET =
            HydroponiCraftRegistry.ITEMS.register("accelerant_solution_bucket",
                    () -> new BucketItem(ACCELERANT_SOLUTION.get(), bucketProps()));

    // -------------------------------------------------------------------------
    // Balanced Blend  (Nutrient + Yield Tonic → 2x growth + Fortune 1 — teal)
    // -------------------------------------------------------------------------

    public static final DeferredHolder<FluidType, FluidType> BALANCED_BLEND_TYPE =
            HydroponiCraftRegistry.FLUID_TYPES.register("balanced_blend",
                    () -> makeType(0xFF00ACC1));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> BALANCED_BLEND =
            HydroponiCraftRegistry.FLUIDS.register("balanced_blend",
                    () -> new BaseFlowingFluid.Source(BALANCED_BLEND_PROPS));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> BALANCED_BLEND_FLOWING =
            HydroponiCraftRegistry.FLUIDS.register("balanced_blend_flowing",
                    () -> new BaseFlowingFluid.Flowing(BALANCED_BLEND_PROPS));

    public static final DeferredHolder<net.minecraft.world.level.block.Block, LiquidBlock> BALANCED_BLEND_BLOCK =
            HydroponiCraftRegistry.BLOCKS.register("balanced_blend",
                    () -> new LiquidBlock(BALANCED_BLEND.get(), fluidBlockProps()));

    public static final DeferredHolder<Item, BucketItem> BALANCED_BLEND_BUCKET =
            HydroponiCraftRegistry.ITEMS.register("balanced_blend_bucket",
                    () -> new BucketItem(BALANCED_BLEND.get(), bucketProps()));

    // -------------------------------------------------------------------------
    // Static initialiser — assign all Properties now that every holder exists
    // -------------------------------------------------------------------------

    static {
        NUTRIENT_FLUID_PROPS = new BaseFlowingFluid.Properties(
                NUTRIENT_FLUID_TYPE::get, NUTRIENT_FLUID::get, NUTRIENT_FLUID_FLOWING::get)
                .bucket(NUTRIENT_FLUID_BUCKET::get).block(NUTRIENT_FLUID_BLOCK::get);

        NUTRIENT_SOLUTION_PROPS = new BaseFlowingFluid.Properties(
                NUTRIENT_SOLUTION_TYPE::get, NUTRIENT_SOLUTION::get, NUTRIENT_SOLUTION_FLOWING::get)
                .bucket(NUTRIENT_SOLUTION_BUCKET::get).block(NUTRIENT_SOLUTION_BLOCK::get);

        ENRICHED_SOLUTION_PROPS = new BaseFlowingFluid.Properties(
                ENRICHED_SOLUTION_TYPE::get, ENRICHED_SOLUTION::get, ENRICHED_SOLUTION_FLOWING::get)
                .bucket(ENRICHED_SOLUTION_BUCKET::get).block(ENRICHED_SOLUTION_BLOCK::get);

        YIELD_TONIC_PROPS = new BaseFlowingFluid.Properties(
                YIELD_TONIC_TYPE::get, YIELD_TONIC::get, YIELD_TONIC_FLOWING::get)
                .bucket(YIELD_TONIC_BUCKET::get).block(YIELD_TONIC_BLOCK::get);

        ACCELERANT_SOLUTION_PROPS = new BaseFlowingFluid.Properties(
                ACCELERANT_SOLUTION_TYPE::get, ACCELERANT_SOLUTION::get, ACCELERANT_SOLUTION_FLOWING::get)
                .bucket(ACCELERANT_SOLUTION_BUCKET::get).block(ACCELERANT_SOLUTION_BLOCK::get);

        BALANCED_BLEND_PROPS = new BaseFlowingFluid.Properties(
                BALANCED_BLEND_TYPE::get, BALANCED_BLEND::get, BALANCED_BLEND_FLOWING::get)
                .bucket(BALANCED_BLEND_BUCKET::get).block(BALANCED_BLEND_BLOCK::get);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    @SuppressWarnings("deprecation")
    private static FluidType makeType(int tintColor) {
        return new FluidType(FluidType.Properties.create()) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    private static final ResourceLocation STILL =
                            ResourceLocation.withDefaultNamespace("block/water_still");
                    private static final ResourceLocation FLOW =
                            ResourceLocation.withDefaultNamespace("block/water_flow");

                    @Override
                    public ResourceLocation getStillTexture() { return STILL; }

                    @Override
                    public ResourceLocation getFlowingTexture() { return FLOW; }

                    @Override
                    public int getTintColor() { return tintColor; }
                });
            }
        };
    }

    private static BlockBehaviour.Properties fluidBlockProps() {
        return BlockBehaviour.Properties.of()
                .noCollission()
                .strength(100f)
                .noLootTable()
                .liquid()
                .replaceable();
    }

    private static Item.Properties bucketProps() {
        return new Item.Properties()
                .craftRemainder(Items.BUCKET)
                .stacksTo(1);
    }

    /** Called from the mod constructor to force static initialisation. */
    public static void register() {}
}
