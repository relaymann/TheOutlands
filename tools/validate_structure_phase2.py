#!/usr/bin/env python3
"""Integrity audit for all Outlands structure content and generation safeguards."""
from __future__ import annotations
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src/main/resources/data/theoutlands/loot_tables/structures"
FEATURE = ROOT / "src/main/java/com/relaymann/theoutlands/worldgen/OutlandsStructureFeature.java"
PIECE = ROOT / "src/main/java/com/relaymann/theoutlands/worldgen/OutlandsStructurePiece.java"
POLICY = ROOT / "src/main/java/com/relaymann/theoutlands/worldgen/StructurePlacementPolicy.java"
AMBIENCE = ROOT / "src/main/java/com/relaymann/theoutlands/event/StructureAmbienceManager.java"
DISCOVERY = ROOT / "src/main/java/com/relaymann/theoutlands/event/StructureDiscoveryEvents.java"
ENCOUNTERS = ROOT / "src/main/java/com/relaymann/theoutlands/worldgen/StructureEncounterManager.java"
STRUCTURE = ROOT / "src/main/java/com/relaymann/theoutlands/worldgen/OutlandsStructure.java"
STRUCTURES = {"cabin","tower","bunker","shrine","observatory","archive","research_facility",
"apostle_cathedral","forsaken_settlement","last_outpost","forgotten_station","null_chamber",
"hollow_crypt","boundary_monument"}

def walk(value):
    if isinstance(value, dict):
        yield value
        for child in value.values(): yield from walk(child)
    elif isinstance(value, list):
        for child in value: yield from walk(child)

def main():
    feature, piece, policy = (p.read_text(encoding="utf-8") for p in (FEATURE, PIECE, POLICY))
    ambience, discovery, encounters, structure = (p.read_text(encoding="utf-8") for p in (AMBIENCE, DISCOVERY, ENCOUNTERS, STRUCTURE))
    refs=set(re.findall(r'theoutlands:structures/([a-z_]+)',feature))
    expected=STRUCTURES|{"hidden_cache"}
    assert refs==expected, f"loot references mismatch: missing={expected-refs}, extra={refs-expected}"
    titles=set()
    for name in expected:
        path=DATA/f"{name}.json"
        assert path.is_file(), f"missing loot table: {path}"
        table=json.loads(path.read_text(encoding="utf-8"))
        assert table.get("type")=="minecraft:chest"
        if name!="hidden_cache":
            books=[n for n in walk(table) if "minecraft:written_book_content" in n.get("components",{})]
            assert books, f"{name}: missing record"
            content=books[0]["components"]["minecraft:written_book_content"]
            assert content.get("title") and len(content.get("pages",[]))>=3, f"{name}: missing cross-reference page"
            assert content["title"] not in titles, f"{name}: duplicate record title"
            titles.add(content["title"])
    tables=[json.loads(p.read_text(encoding="utf-8")) for p in DATA.glob("*.json")]
    serialized=json.dumps(tables)
    forbidden={"theoutlands:death_jar","theoutlands:death_manifest","theoutlands:the_last_page","theoutlands:almighty_fragment"}
    assert not any(item in serialized for item in forbidden), "progression artifact leaked into structure loot"
    assert "hiddenCache(id,level,x,surfaceY,z,random)" in feature and "private static void hiddenCache(String id" in feature
    assert "three-stroke mark" in feature
    assert "new BoundingBox(x,y-40,z" in piece and "chunkPos.x==centerChunkX&&chunkPos.z==centerChunkZ" in piece
    assert "getStructureWithPieceAt" in ambience and "holder.value() instanceof" in ambience
    assert "HORROR_AUDIO_ENABLED" in ambience and "ClientboundSoundPacket" in ambience and "NEXT_CUE" in ambience
    sound_registry=(ROOT/"src/main/java/com/relaymann/theoutlands/sound/OutlandsSounds.java").read_text(encoding="utf-8")
    sound_json=json.loads((ROOT/"src/main/resources/assets/theoutlands/sounds.json").read_text(encoding="utf-8"))
    audio_script=(ROOT/"tools/generate_entity_audio.py").read_text(encoding="utf-8")
    for sound_id in ("cabin","watchtower","bunker","shrine","observatory","death_archive","research_facility","apostle_cathedral","forsaken_settlement","last_outpost","forgotten_station","null_chamber","hollow_crypt","boundary_monument"):
        key="structure_"+sound_id
        assert key in sound_registry and key in sound_json and f'structure_tone("{key}"' in audio_script, f"missing custom ambience asset: {key}"

    assert "getStructureWithPieceAt" in discovery and 'recordDiscovery(player, "structure_" + layout)' in discovery
    assert 'recordDiscovery(player, "shared_structure_mark")' in discovery
    assert "ENTITY_SPAWN_BALANCE_ENABLED" in encounters and "ENTITY_ECOSYSTEM_ENABLED" in encounters
    assert "addFreshEntity" in encounters and "NULL" in encounters and "DEATH" in encounters
    layout_ids=set(re.findall(r'case "([a-z_]+)" ->',feature))
    assert STRUCTURES<=layout_ids, f"missing layouts: {STRUCTURES-layout_ids}"
    assert "applyVariations(id,level,x,surfaceY,z,random)" in feature
    assert "StructurePlacementPolicy.containsBlock" in feature and "public static boolean containsBlock" in policy
    assert "StructurePlacementPolicy.isSpacedAnchor" in structure and "max-min>5" in structure
    selection_test=(ROOT/"src/test/java/com/relaymann/theoutlands/worldgen/OutlandsStructureSelectionTest.java").read_text(encoding="utf-8")
    assert "seededLayoutSelectionIsRepeatable" in selection_test and "forestOnlyCabinRequiresForest" in selection_test
    sj=json.loads((ROOT/"src/main/resources/data/theoutlands/worldgen/structure/outlands_sites.json").read_text())
    ss=json.loads((ROOT/"src/main/resources/data/theoutlands/worldgen/structure_set/outlands_sites.json").read_text())
    bt=json.loads((ROOT/"src/main/resources/data/theoutlands/tags/worldgen/biome/structure_land.json").read_text())
    assert sj["size"]==48 and sj["terrain_adaptation"]=="none"
    assert ss["placement"]["spacing"]==1 and ss["placement"]["separation"]==0
    assert ss["placement"]["exclusion_zone"]=={"other_set":"minecraft:villages","chunk_count":8}
    assert "startsForStructure(chunkPos, candidate -> !(candidate instanceof OutlandsStructure))" in piece
    assert "hasSuitableSurface(level,box,surfaceY)" in piece
    assert "Heightmap.Types.MOTION_BLOCKING_NO_LEAVES" in piece and "BlockTags.LOGS" in piece
    assert "Heightmap.Types.MOTION_BLOCKING_NO_LEAVES" in structure
    assert not any("ocean" in b or b.endswith(":river") or b.endswith(":beach") for b in bt["values"])
    excluded=(":stony_shore",":dripstone_caves",":lush_caves",":deep_dark",":swamp",":mangrove_swamp")
    assert not any(b.endswith(s) for b in bt["values"] for s in excluded)
    print(f"Validated {len(STRUCTURES)} layouts, {len(expected)} loot tables, linked records, shared markings, chunk clipping, placement, ambience, and encounter safeguards.")

if __name__=="__main__": main()
