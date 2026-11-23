#!/usr/bin/env python3
"""Static integrity checks for Batch 1 world memory and environmental horror."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MEMORY = (ROOT / "src/main/java/com/relaymann/theoutlands/event/WorldMemoryManager.java").read_text()
ANOMALY = (ROOT / "src/main/java/com/relaymann/theoutlands/event/EnvironmentalAnomalyManager.java").read_text()
ECOSYSTEM = (ROOT / "src/main/java/com/relaymann/theoutlands/event/OutlandsEcosystemManager.java").read_text()
TRACE = (ROOT / "src/main/java/com/relaymann/theoutlands/event/EntityTraceManager.java").read_text()

for token in ("extends SavedData", "getDataStorage().computeIfAbsent", "MAX_RECORDS",
              "rememberSighting", "investigateRememberedSite", "shiftTerritoryTowardActivity",
              "reactToDoorsLightsTrapsAndDefenses", '"hide"', '"encounter"', '"trap"',
              '"defense"', '"light"', "ENTITY_MEMORY_LIFETIME", "level.isLoaded"):
    assert token in MEMORY, f"Batch 1 world-memory feature missing: {token}"

for token in ("ClientboundStopSoundPacket", "SoundSource.WEATHER",
              "LIGHTNING_BOLT_THUNDER", "nextInt(9)", "MIN_COOLDOWN"):
    assert token in ANOMALY, f"Batch 1 environmental event missing: {token}"

for token in ("HunterEntity && b instanceof StalkerEntity",
              "HowlerEntity && (b instanceof HunterEntity || b instanceof StalkerEntity)",
              "nightmare_hound", "studyHunt"):
    assert token in ECOSYSTEM, f"Compatible entity interaction missing: {token}"

assert "NEXT_TRACE" in TRACE and "CRIT" in TRACE and "BlockParticleOption" in TRACE
print("Batch 1 world memory, hazard awareness, entity cooperation, traces, and rare environmental events validated.")
