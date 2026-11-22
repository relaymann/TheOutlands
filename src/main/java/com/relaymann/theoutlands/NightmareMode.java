package com.relaymann.theoutlands;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Shared, server-authoritative NIGHTMARE world-mode checks. */
public final class NightmareMode {
    private static final Set<Long> NIGHTMARE_SEEDS = ConcurrentHashMap.newKeySet();

    private NightmareMode() {}

    public static boolean isEnabled(Level level) {
        return level != null && level.getGameRules().getBoolean(OutlandsGameRules.NIGHTMARE_MODE);
    }

    /** Structure placement runs from a seed-only generation context. */
    public static boolean isSeedEnabled(long seed) {
        return NIGHTMARE_SEEDS.contains(seed);
    }

    public static void refresh(ServerLevel level) {
        if (!level.dimension().equals(Level.OVERWORLD)) return;
        if (isEnabled(level)) NIGHTMARE_SEEDS.add(level.getSeed());
        else NIGHTMARE_SEEDS.remove(level.getSeed());
    }

    public static void clear() {
        NIGHTMARE_SEEDS.clear();
    }
}
