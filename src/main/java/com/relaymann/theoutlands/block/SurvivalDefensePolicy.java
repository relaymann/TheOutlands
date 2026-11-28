package com.relaymann.theoutlands.block;

public final class SurvivalDefensePolicy {
    private SurvivalDefensePolicy() {}

    public static boolean isReady(boolean hasPreviousTrigger, long now,
                                  long previousTrigger, long cooldownTicks) {
        return cooldownTicks <= 0L || !hasPreviousTrigger
                || now < previousTrigger || now - previousTrigger >= cooldownTicks;
    }
}
