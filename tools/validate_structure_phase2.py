#!/usr/bin/env python3
"""Static integrity checks for Phase 2A-2C structure content."""
from __future__ import annotations
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src/main/resources/data/theoutlands/loot_tables/structures"
FEATURE = ROOT / "src/main/java/com/relaymann/theoutlands/worldgen/OutlandsStructureFeature.java"
PIECE = ROOT / "src/main/java/com/relaymann/theoutlands/worldgen/OutlandsStructurePiece.java"
STRUCTURES = {
    "cabin", "tower", "bunker", "shrine", "observatory", "archive",
    "research_facility", "apostle_cathedral", "forsaken_settlement",
    "last_outpost", "forgotten_station", "null_chamber", "hollow_crypt",
    "boundary_monument",
}

def walk(value):
    if isinstance(value, dict):
        yield value
        for child in value.values():
            yield from walk(child)
    elif isinstance(value, list):
        for child in value:
            yield from walk(child)

def main() -> None:
    feature = FEATURE.read_text(encoding="utf-8")
    piece = PIECE.read_text(encoding="utf-8")
    references = set(re.findall(r'theoutlands:structures/([a-z_]+)', feature))
    expected = STRUCTURES | {"hidden_cache"}
    assert references == expected, f"loot references mismatch: missing={expected-references}, extra={references-expected}"
    for name in expected:
        path = DATA / f"{name}.json"
        assert path.is_file(), f"missing loot table: {path}"
        table = json.loads(path.read_text(encoding="utf-8"))
        assert table.get("type") == "minecraft:chest", f"{name}: not a chest loot table"
        nodes = list(walk(table))
        if name != "hidden_cache":
            assert any("minecraft:written_book_content" in n.get("components", {}) for n in nodes), f"{name}: missing field record"
    all_tables = [json.loads(p.read_text(encoding="utf-8")) for p in DATA.glob("*.json")]
    forbidden = {"theoutlands:death_jar", "theoutlands:death_manifest", "theoutlands:the_last_page", "theoutlands:almighty_fragment"}
    serialized = json.dumps(all_tables)
    assert not any(item in serialized for item in forbidden), "progression-gated story artifacts leaked into structure loot"
    assert "hiddenCache(id,level,x,surfaceY,z,random)" in feature, "hidden chamber hook missing"
    assert "private static void hiddenCache(String id" in feature, "hidden chamber builder missing"
    assert "new BoundingBox(x,y-40,z" in piece, "piece bounds do not include hidden chambers"
    ambience = ROOT / "src/main/java/com/relaymann/theoutlands/event/StructureAmbienceManager.java"
    encounters = ROOT / "src/main/java/com/relaymann/theoutlands/worldgen/StructureEncounterManager.java"
    ambience_text = ambience.read_text(encoding="utf-8")
    encounter_text = encounters.read_text(encoding="utf-8")
    assert "getStructureWithPieceAt" in ambience_text
    assert "HORROR_AUDIO_ENABLED" in ambience_text
    assert "NEXT_CUE" in ambience_text and "ClientboundSoundPacket" in ambience_text
    assert "ENTITY_SPAWN_BALANCE_ENABLED" in encounter_text and "ENTITY_ECOSYSTEM_ENABLED" in encounter_text
    assert "addFreshEntity" in encounter_text and "NULL" in encounter_text and "DEATH" in encounter_text
    assert "applyVariations(id,level,x,surfaceY,z,random)" in feature
    for keyword in ("collapsed", "alternate", "wing"):
        pass
    print(f"Validated {len(expected)} structure loot tables, {len(STRUCTURES)} site field records, hidden chambers, ambience, rare encounters, and variation hooks.")

if __name__ == "__main__":
    main()
