package com.relaymann.theoutlands;

import net.minecraft.world.level.GameRules;

/** Persistent world flags owned by The Outlands. */
public final class OutlandsGameRules {
    public static final GameRules.Key<GameRules.BooleanValue> NIGHTMARE_MODE =
            GameRules.register("outlandsNightmareMode", GameRules.Category.MISC,
                    GameRules.BooleanValue.create(false));

    private OutlandsGameRules() {}

    /** Forces class initialization before world creation and level loading. */
    public static void register() {}
}
