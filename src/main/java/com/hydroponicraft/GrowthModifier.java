package com.hydroponicraft;

import net.minecraft.world.level.material.Fluid;

import java.util.Map;
import java.util.Optional;

/**
 * Maps each Hydroponicraft solution fluid to its growth effect.
 * speedMultiplier — how many extra randomTick calls per real tick
 * fortuneLevel    — passed to the loot context on harvest
 */
public record GrowthModifier(float speedMultiplier, int fortuneLevel) {

    public static final Map<Fluid, GrowthModifier> REGISTRY;

    static {
        REGISTRY = Map.of(
                HydroponiCraftFluids.NUTRIENT_FLUID.get(),      new GrowthModifier(1.0f, 0),
                HydroponiCraftFluids.NUTRIENT_SOLUTION.get(),   new GrowthModifier(1.5f, 0),
                HydroponiCraftFluids.ENRICHED_SOLUTION.get(),   new GrowthModifier(2.5f, 0),
                HydroponiCraftFluids.YIELD_TONIC.get(),         new GrowthModifier(1.0f, 2),
                HydroponiCraftFluids.ACCELERANT_SOLUTION.get(), new GrowthModifier(4.0f, 0),
                HydroponiCraftFluids.BALANCED_BLEND.get(),      new GrowthModifier(2.0f, 1)
        );
    }

    public static Optional<GrowthModifier> get(Fluid fluid) {
        return Optional.ofNullable(REGISTRY.get(fluid));
    }
}
