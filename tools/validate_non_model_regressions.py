#!/usr/bin/env python3
"""Regression checks for non-model gameplay, encounter, and survival systems."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")

def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit("NON-MODEL REGRESSION AUDIT FAILED: " + message)

def main() -> None:
    trap = read("src/main/java/com/relaymann/theoutlands/block/SurvivalDefenseBlock.java")
    cooldown = read("src/main/java/com/relaymann/theoutlands/block/SurvivalDefensePolicy.java")
    cooldown_test = read("src/test/java/com/relaymann/theoutlands/block/SurvivalDefensePolicyTest.java")
    encounters = read("src/main/java/com/relaymann/theoutlands/worldgen/StructureEncounterManager.java")
    saved = read("src/main/java/com/relaymann/theoutlands/worldgen/StructureEncounterSavedData.java")
    nightmare_policy = read("src/main/java/com/relaymann/theoutlands/event/NightmareEventPolicy.java")
    nightmare_manager = read("src/main/java/com/relaymann/theoutlands/event/NightmareModeManager.java")
    nightmare_test = read("src/test/java/com/relaymann/theoutlands/event/NightmareEventPolicyTest.java")

    require("monster.tickCount % 20" not in trap,
            "trap activation must not depend on global entity tick phase")
    require("SurvivalDefensePolicy.isReady" in trap and "pos.asLong()" in trap,
            "trap cooldown must be per entity and per block position")
    require("persistent.putLong(cooldownKey, now)" in trap,
            "trap contact timestamp must be persisted on the entity")
    require("now < previousTrigger" in cooldown,
            "cooldown must recover safely if world time moves backwards")
    require("firstContactAlwaysTriggersRegardlessOfEntityAge" in cooldown_test
            and "repeatContactWaitsForTheConfiguredCooldown" in cooldown_test,
            "trap cooldown regression tests are missing")
    require('case "nightmare_signal_hollow"' in encounters
            and "NIGHTMARE_WATCHER" in encounters,
            "Nightmare Signal Hollow must resolve its intended encounter")
    require("if (resolve(level, layout, center, distance, seed))" in encounters,
            "blocked encounter placement must remain retryable")
    require("resolvedSites.markResolved(key)" in encounters,
            "successful or intentionally skipped encounter rolls must remain one-shot")
    require("extends SavedData" in saved and "setDirty()" in saved,
            "encounter resolution must remain persistent across restarts")
    require("shouldBlockGameModeChange" in nightmare_manager
            and "GameType.CREATIVE" in nightmare_manager
            and "GameType.SPECTATOR" in nightmare_manager,
            "Nightmare Mode must block only creative and spectator mode changes")
    require("targetCreative || targetSpectator" in nightmare_policy
            and "nightmareEnabled && playerAlive" in nightmare_policy,
            "Nightmare mode restrictions must be scoped to living players entering bypass modes")
    require("Math.max(0, Math.min(3, currentStage))" in nightmare_policy
            and "(current + 1) * 600L" in nightmare_policy,
            "Blood Moon stage policy must be bounded and advance at 600-tick thresholds")
    require("nextBloodMoonStage" in nightmare_manager
            and nightmare_manager.index("if (player == null) return;") < nightmare_manager.index("BLOOD_MOON_STAGE.put(level, stage)"),
            "Blood Moon stages must not be consumed without an eligible player")
    require("private static boolean spawnThreat" in nightmare_manager
            and "if (spawned) BLOOD_MOON_STAGE.put(level, stage)" in nightmare_manager,
            "Blood Moon stages must retry when every safe spawn attempt fails")
    require("adventureModeRemainsAllowed" in nightmare_test
            and "missedBloodMoonStagesCatchUpOneAtATime" in nightmare_test,
            "Nightmare event policy regression tests are missing")
    print("Non-model gameplay regression checks passed.")

if __name__ == "__main__":
    main()
