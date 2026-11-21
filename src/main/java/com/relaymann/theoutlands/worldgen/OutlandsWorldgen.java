package com.relaymann.theoutlands.worldgen;

import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registry for additive Outlands world-generation features. */
public final class OutlandsWorldgen {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, TheOutlands.MOD_ID);
    static { FEATURES.register("outlands_structures", OutlandsStructureFeature::new); }
    private OutlandsWorldgen() {}
}
