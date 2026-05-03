package com.hydroponicraft;

import com.hydroponicraft.block.ChemicalSynthesizerBlock;
import com.hydroponicraft.block.DigesterBlock;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(HydroponiCraftMod.MOD_ID)
public class HydroponiCraftMod {

    public static final String MOD_ID = "hydroponicraft";

    public HydroponiCraftMod(IEventBus modEventBus) {
        HydroponiCraftFluids.register();
        HydroponiCraftRegistry.register(modEventBus);
        modEventBus.addListener(HydroponiCraftMod::registerCapabilities);
        modEventBus.addListener(HydroponiCraftMod::buildCreativeTab);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Digester: item handler exposed on top face (hopper/funnel feeds from above)
        // Also exposed on null side so internal/automation queries work
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                HydroponiCraftRegistry.DIGESTER_BE.get(),
                (be, side) -> (side == null || side == Direction.UP) ? be.itemHandler : null);

        // Digester: fluid tank exposed on all sides except top (hopper input)
        // and model-east / shaft face (facing.getClockWise())
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                HydroponiCraftRegistry.DIGESTER_BE.get(),
                (be, side) -> {
                    if (side == Direction.UP) return null;
                    Direction facing = be.getBlockState().getValue(DigesterBlock.FACING);
                    if (side == facing.getClockWise()) return null;
                    return be.fluidTank;
                });

        // Mixer: input handler on all horizontal faces; output tank on bottom only
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                HydroponiCraftRegistry.MIXER_BE.get(),
                (be, side) -> {
                    if (side == null || side == Direction.UP) return null;
                    if (side == Direction.DOWN) return be.outputTank;
                    return be.inputHandler;
                });

        // Growth Bed: fluid input on all sides except top (crops are planted on top)
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                HydroponiCraftRegistry.GROWTH_BED_BE.get(),
                (be, side) -> side == Direction.UP ? null : be.fluidTank);

        // Chemical Synthesizer: item input on top (slot 0), item output on bottom (slot 1)
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                HydroponiCraftRegistry.CHEMICAL_SYNTHESIZER_BE.get(),
                (be, side) -> {
                    if (side == null) return be.itemHandler;
                    if (side == Direction.UP)   return be.inputSlotWrapper;
                    if (side == Direction.DOWN) return be.outputSlotWrapper;
                    return null;
                });

        // Chemical Synthesizer: fluid tank on all horizontal sides except shaft face
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                HydroponiCraftRegistry.CHEMICAL_SYNTHESIZER_BE.get(),
                (be, side) -> {
                    if (side == null || side == Direction.UP || side == Direction.DOWN) return null;
                    Direction facing = be.getBlockState().getValue(ChemicalSynthesizerBlock.FACING);
                    if (side == facing.getClockWise()) return null;
                    return be.fluidTank;
                });
    }

    private static void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(HydroponiCraftRegistry.CREATIVE_TAB.getKey())) return;

        // Machines
        event.accept(HydroponiCraftRegistry.DIGESTER_ITEM.get());
        event.accept(HydroponiCraftRegistry.MIXER_ITEM.get());
        event.accept(HydroponiCraftRegistry.GROWTH_BED_ITEM.get());
        event.accept(HydroponiCraftRegistry.CHEMICAL_SYNTHESIZER_ITEM.get());

        // Tools
        event.accept(HydroponiCraftRegistry.REMOTE_DETONATOR.get());

        // C4 and colored variants
        event.accept(HydroponiCraftRegistry.C4_ITEM.get());
        for (var holder : HydroponiCraftRegistry.COLORED_C4_ITEMS.values()) {
            event.accept(holder.get());
        }

        // Fluid buckets
        event.accept(HydroponiCraftFluids.NUTRIENT_FLUID_BUCKET.get());
        event.accept(HydroponiCraftFluids.NUTRIENT_SOLUTION_BUCKET.get());
        event.accept(HydroponiCraftFluids.ENRICHED_SOLUTION_BUCKET.get());
        event.accept(HydroponiCraftFluids.YIELD_TONIC_BUCKET.get());
        event.accept(HydroponiCraftFluids.ACCELERANT_SOLUTION_BUCKET.get());
        event.accept(HydroponiCraftFluids.BALANCED_BLEND_BUCKET.get());
        event.accept(HydroponiCraftFluids.NETHER_WART_FLUID_BUCKET.get());
        event.accept(HydroponiCraftFluids.CHORUS_FRUIT_FLUID_BUCKET.get());
    }

}
