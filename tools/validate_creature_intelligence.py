#!/usr/bin/env python3
"""Static regression audit for the advanced creature intelligence framework."""
from pathlib import Path

ROOT = Path("src/main/java/com/relaymann/theoutlands")
manager = (ROOT / "event/OutlandsIntelligenceManager.java").read_text(encoding="utf-8")
alarm = (ROOT / "block/SurvivalDefenseBlock.java").read_text(encoding="utf-8")

required = {
    "server-side footstep sampling": "PlayerTickEvent.Post",
    "sprinting noise": 'isSprinting() ? "sprint" : "footstep"',
    "block breaking": 'record(level, event.getPos(), player.getUUID(), "break")',
    "block placement": 'record(level, event.getPos(), player.getUUID(), "place")',
    "door and trapdoor hearing": "TrapDoorBlock",
    "perimeter alarm stimulus": 'record(level, pos, null, "alarm")',
    "alarm processing without nearby players": '!"alarm".equals(disturbance.kind())',
    "per-species hearing profiles": "hearingRange(Mob mob)",
    "hearing scan covers the loudest profile": "inflate(56.0D)",
    "sound-specific effective ranges": "disturbanceRange(String kind)",
    "player-source pursuit": "isSoundPursuer(mob)",
    "widening search behavior": "continueSearch(ServerLevel level, Mob mob)",
    "wooden door interaction": "openNearbyWoodDoor(ServerLevel level, Mob mob)",
    "vanilla synchronized door opening": "door.setOpen(mob, level, state, doorPos, true)",
    "loaded-chunk safety": "level.hasChunkAt(candidate)",
    "adaptive pursuit": "adaptPursuit(Mob mob)",
    "deterministic temperament": "temperament(Mob mob)",
    "virtual territory": "maintainTerritory(ServerLevel level, Mob mob)",
    "config gate": "ENTITY_INTELLIGENCE_ENABLED.get()",
    "creative/spectator exclusion": "!player.isCreative() && !player.isSpectator()",
    "bounded disturbance history": "MAX_DISTURBANCES_PER_DIMENSION",
    "search cleanup": "SEARCHES.keySet().removeIf",
}
for label, token in required.items():
    if token not in manager:
        raise SystemExit("Missing intelligence regression: " + label)

for entity in (
    "NullEntity", "FearEntity", "DeathEntity", "AlmightyEntity", "NullApostleEntity",
    "CaveJarEntity", "StalkerEntity", "ObserverEntity", "HunterEntity", "BurrowerEntity",
    "MimicEntity", "StillbornStatueEntity", "AgitatedOneEntity", "LurkerEntity",
    "HowlerEntity", "DredgeEntity", "PilgrimEntity", "CeilingDwellerEntity",
    "ColossusEntity", "NightmareCreature",
):
    if entity not in manager:
        raise SystemExit("Creature roster missing from intelligence manager: " + entity)

if "OutlandsIntelligenceManager.recordAlarm(server, pos)" not in alarm:
    raise SystemExit("Perimeter alarm does not publish an audible stimulus")

if manager.count("@SubscribeEvent") < 6:
    raise SystemExit("Expected event coverage for movement, blocks, doors, deaths, and server ticks")

print("Creature intelligence audit passed: hearing, memory, investigation, pursuit, search, alarms, and roster coverage.")
