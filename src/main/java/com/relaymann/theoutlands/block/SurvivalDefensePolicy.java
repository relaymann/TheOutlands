package com.relaymann.theoutlands.block;

/** Pure per-entity trap cooldown policy, independent of global entity age. */
public final class SurvivalDefensePolicy {
    private SurvivalDefensePolicy() {}

    public static boolean isReady(boolean hasPreviousTrigger, long now,
                                  long previousTrigger, long cooldownTicks) {
        return cooldownTicks <= 0L || !hasPreviousTrigger
                || now < previousTrigger || now - previousTrigger >= cooldownTicks;
    }
}
