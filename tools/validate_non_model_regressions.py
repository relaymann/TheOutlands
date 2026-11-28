#!/usr/bin/env python3
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
    print("Non-model gameplay regression checks passed.")

if __name__ == "__main__":
    main()
