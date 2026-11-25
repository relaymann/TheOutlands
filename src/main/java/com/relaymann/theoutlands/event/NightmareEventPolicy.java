package com.relaymann.theoutlands.event;

/** Pure rules for Nightmare Mode event progression and permitted player modes. */
public final class NightmareEventPolicy {
    private NightmareEventPolicy() {}

    /** Adventure remains a valid challenge mode; only creative and spectator bypass survival. */
    public static boolean shouldBlockGameModeChange(boolean nightmareEnabled, boolean playerAlive,
                                                     boolean targetCreative, boolean targetSpectator) {
        return nightmareEnabled && playerAlive && (targetCreative || targetSpectator);
    }

    /** Advances one Blood Moon escalation step when its elapsed-time threshold has been reached. */
    public static int nextBloodMoonStage(int currentStage, long elapsedTicks) {
        int current = Math.max(0, Math.min(3, currentStage));
        if (current >= 3 || elapsedTicks < (long) (current + 1) * 600L) return current;
        return current + 1;
    }
}
