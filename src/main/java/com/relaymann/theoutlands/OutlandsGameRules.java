package com.relaymann.theoutlands;

import net.minecraft.world.level.GameRules;

public final class OutlandsGameRules {
    public static final GameRules.Key<GameRules.BooleanValue> NIGHTMARE_MODE =
            GameRules.register("outlandsNightmareMode", GameRules.Category.MISC,
                    GameRules.BooleanValue.create(false));

    private OutlandsGameRules() {}

    public static void register() {}
}
