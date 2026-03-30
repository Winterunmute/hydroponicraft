package com.hydroponicraft;

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
        // Digester: item handler exposed only on top face (hopper feeds from above)
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                HydroponiCraftRegistry.DIGESTER_BE.get(),
                (be, side) -> side == Direction.UP ? be.itemHandler : null);

        // Digester: fluid tank exposed only on the model-east face (facing.getClockWise())
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                HydroponiCraftRegistry.DIGESTER_BE.get(),
                (be, side) -> {
                    if (side == null) return be.fluidTank;
                    Direction facing = be.getBlockState().getValue(DigesterBlock.FACING);
                    return side == facing.getClockWise() ? be.fluidTank : null;
                });

        // Mixer: expose output tank on all sides so Create fluid pipes can connect
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                HydroponiCraftRegistry.MIXER_BE.get(),
                (be, side) -> be.outputTank);

        // Growth Bed: fluid input on all sides except top (crops are planted on top)
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                HydroponiCraftRegistry.GROWTH_BED_BE.get(),
                (be, side) -> side == Direction.UP ? null : be.fluidTank);
    }

    private static void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(HydroponiCraftRegistry.CREATIVE_TAB.getKey())) return;

        // Machines
        event.accept(HydroponiCraftRegistry.DIGESTER_ITEM.get());
        event.accept(HydroponiCraftRegistry.MIXER_ITEM.get());
        event.accept(HydroponiCraftRegistry.GROWTH_BED_ITEM.get());

        // Fluid buckets
        event.accept(HydroponiCraftFluids.NUTRIENT_FLUID_BUCKET.get());
        event.accept(HydroponiCraftFluids.NUTRIENT_SOLUTION_BUCKET.get());
        event.accept(HydroponiCraftFluids.ENRICHED_SOLUTION_BUCKET.get());
        event.accept(HydroponiCraftFluids.YIELD_TONIC_BUCKET.get());
        event.accept(HydroponiCraftFluids.ACCELERANT_SOLUTION_BUCKET.get());
        event.accept(HydroponiCraftFluids.BALANCED_BLEND_BUCKET.get());
    }

}
