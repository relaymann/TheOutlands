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
BASE_STRUCTURES = {"cabin","tower","bunker","shrine","observatory","archive","research_facility",
"apostle_cathedral","forsaken_settlement","last_outpost","forgotten_station","null_chamber",
"hollow_crypt","boundary_monument"}
NIGHTMARE_STRUCTURES = {"nightmare_containment","blood_moon_shrine","nightmare_outpost","black_site","nightmare_signal_hollow"}
EXPLORATION_STRUCTURES = {"ranger_station","mining_operation","ruined_settlement","observation_tower","subterranean_complex"}
STRUCTURES = BASE_STRUCTURES | NIGHTMARE_STRUCTURES | EXPLORATION_STRUCTURES

def walk(value):
    if isinstance(value, dict):
        yield value
        for child in value.values(): yield from walk(child)
    elif isinstance(value, list):
        for child in value: yield from walk(child)

def main():
    feature, piece, policy = (p.read_text(encoding="utf-8") for p in (FEATURE, PIECE, POLICY))
    ambience, discovery, encounters, structure = (p.read_text(encoding="utf-8") for p in (AMBIENCE, DISCOVERY, ENCOUNTERS, STRUCTURE))
    assert chr(92) + "n" not in ambience, "literal escaped newline found in Java ambience source"
    refs=set(re.findall(r'theoutlands:structures/([a-z_]+)',feature))
    expected=BASE_STRUCTURES|{"hidden_cache","nightmare_records"}
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
    assert "new BoundingBox(x,y-40,z" in piece
    assert "StructureEncounterManager.tick(level)" in (ROOT/"src/main/java/com/relaymann/theoutlands/event/ServerHorrorEvents.java").read_text(encoding="utf-8")
    assert "void tick(ServerLevel level)" in encounters and "StructureEncounterSavedData.get(level)" in encounters
    assert "bounds.minX() + 24" in encounters and "center.getY() - 12" in encounters and "center.getY() - 25" in encounters
    assert "bounds.minX()+24" in ambience and "bounds.minX()+24" in discovery
    assert "addFreshEntity(entity)" in encounters and "addFreshEntity" not in piece
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
    assert "bounds.minX() + 39, bounds.maxY() - 49, bounds.minZ() + 6" in discovery
    server_events=(ROOT/"src/main/java/com/relaymann/theoutlands/event/ServerHorrorEvents.java").read_text(encoding="utf-8")
    assert "ENTITY_SPAWN_BALANCE_ENABLED" in encounters and "ENTITY_ECOSYSTEM_ENABLED" in encounters
    assert "void tick(ServerLevel level)" in encounters and "StructureEncounterSavedData.get(level)" in encounters
    assert "bounds.minX() + 24" in encounters and "level.noCollision(entity)" in encounters
    assert "resolvedSites.hasResolved(key)" in encounters and "resolvedSites.markResolved(key)" in encounters
    assert "RESOLVED_SITES" not in encounters
    saved_encounters = (ROOT/"src/main/java/com/relaymann/theoutlands/worldgen/StructureEncounterSavedData.java").read_text(encoding="utf-8")
    assert "extends SavedData" in saved_encounters and "setDirty()" in saved_encounters
    assert "getDataStorage().computeIfAbsent" in saved_encounters and "resolved_sites" in saved_encounters
    assert "int[][] offsets" in encounters and "level.addFreshEntity(entity)" in encounters
    assert "level.addFreshEntity(entity)" in encounters and "NULL" in encounters and "DEATH" in encounters
    assert "StructureEncounterManager.tick(level)" in server_events
    assert "addFreshEntity" not in piece and "StructureEncounterManager" not in piece
    layout_ids=set(re.findall(r'case "([a-z_]+)" ->',feature))
    assert STRUCTURES<=layout_ids, f"missing layouts: {STRUCTURES-layout_ids}"
    assert 'case "nightmare_signal_hollow"' in ambience
    for site in EXPLORATION_STRUCTURES | {"nightmare_signal_hollow"}:
        predicate = feature.split("private static boolean isNewExplorationSite", 1)[1].split("}", 1)[0]
        assert site in predicate, f"{site}: should not receive hidden evidence caches"
        assert f'case "{site}"' in ambience, f"{site}: missing structure ambience"
    nightmare_manager=(ROOT/"src/main/java/com/relaymann/theoutlands/event/NightmareModeManager.java").read_text(encoding="utf-8")
    assert "bloodMoon(player)" in nightmare_manager and "silence(player)" in nightmare_manager
    assert "unusualActivity(player)" in nightmare_manager and "containmentFailure(player)" in nightmare_manager
    assert "thePursuit(player)" in nightmare_manager and "theoutlands_nightmare_stalker" in nightmare_manager
    assert "coordinateNightmarePack" in nightmare_manager and "setTarget(player)" in nightmare_manager
    assert "NightmareBalancePolicy.nextEventDelayTicks" in nightmare_manager
    assert "tickActiveStructure" in nightmare_manager and "BLOOD_MOON_STAGE" in nightmare_manager
    assert "migrationSurge(player)" in nightmare_manager and "ashfall(player)" in nightmare_manager
    assert "NIGHTMARE_WATCHER" in nightmare_manager and "PURSUIT_MEMORY" in nightmare_manager
    nightmare_entities = (ROOT/"src/main/java/com/relaymann/theoutlands/entity/OutlandsEntities.java").read_text(encoding="utf-8")
    for entity_id in ("nightmare_watcher", "nightmare_hound", "nightmare_wardened",
                      "nightmare_false_survivor", "nightmare_unbound"):
        assert entity_id in nightmare_entities, f"missing exclusive entity {entity_id}"
    nightmare_records = json.loads((DATA/"nightmare_records.json").read_text(encoding="utf-8"))
    record_titles = {node["components"]["minecraft:written_book_content"]["title"]
                     for node in walk(nightmare_records)
                     if "minecraft:written_book_content" in node.get("components", {})}
    for title in ("Watcher Survey", "Hound Handling Notes", "Wardened Test Log",
                  "False Survivor Interview", "Unbound Observation"):
        assert title in record_titles, f"missing Nightmare field record: {title}"
    assert "nightmare_records" in feature
    assert "applyVariations(id,level,x,surfaceY,z,random)" in feature
    assert "StructurePlacementPolicy.containsBlock" in feature and "public static boolean containsBlock" in policy
    assert "StructurePlacementPolicy.isSpacedAnchor" in structure and "max-min>8" in structure
    assert "int configuredSpacing = nightmare ? 3" in structure
    assert "minimumSpacingForFootprint(configuredSpacing, size)" in structure
    assert "minimumSpacingForFootprint" in policy
    selection_test=(ROOT/"src/test/java/com/relaymann/theoutlands/worldgen/OutlandsStructureSelectionTest.java").read_text(encoding="utf-8")
    assert "seededLayoutSelectionIsRepeatable" in selection_test and "forestOnlyCabinRequiresForest" in selection_test
    assert "nightmareStructureAnchorsKeepFootprintsFromOverlapping" in selection_test
    sj=json.loads((ROOT/"src/main/resources/data/theoutlands/worldgen/structure/outlands_sites.json").read_text())
    ss=json.loads((ROOT/"src/main/resources/data/theoutlands/worldgen/structure_set/outlands_sites.json").read_text())
    bt=json.loads((ROOT/"src/main/resources/data/theoutlands/tags/worldgen/biome/structure_land.json").read_text())
    assert sj["size"]==48 and sj["terrain_adaptation"]=="none"
    assert isinstance(sj.get("spawn_overrides"), dict), "structure codec requires spawn_overrides object"
    assert ss["placement"]["spacing"]==1 and ss["placement"]["separation"]==0
    assert ss["placement"]["exclusion_zone"]=={"other_set":"minecraft:villages","chunk_count":4}
    assert "startsForStructure(chunkPos, candidate -> !(candidate instanceof OutlandsStructure))" in piece
    assert "hasSuitableSurface(level,box,surfaceY)" in piece
    assert "Heightmap.Types.MOTION_BLOCKING_NO_LEAVES" in piece and "BlockTags.LOGS" in piece
    assert "BlockTags.LEAVES" in piece and "isWithinSurfaceTolerance" in piece
    assert "while (groundY > level.getMinBuildHeight())" in piece
    assert "Heightmap.Types.MOTION_BLOCKING_NO_LEAVES" in structure
    assert "surfaceSampleOffsets(size,4)" in structure and "surfaceSampleOffsets(int size, int step)" in policy
    assert "Math.abs((top-1)-expectedY)>5" not in piece
    assert "return false;" in encounters and "return true;" in encounters
    assert "event.isCanceled()" in (ROOT/"src/main/java/com/relaymann/theoutlands/event/StructureDiscoveryEvents.java").read_text(encoding="utf-8")
    assert not any("ocean" in b or b.endswith(":river") or b.endswith(":beach") for b in bt["values"])
    excluded=(":stony_shore",":dripstone_caves",":lush_caves",":deep_dark",":swamp",":mangrove_swamp")
    assert not any(b.endswith(s) for b in bt["values"] for s in excluded)
    print(f"Validated {len(STRUCTURES)} layouts, {len(expected)} loot tables, linked records, shared markings, chunk clipping, placement, ambience, and encounter safeguards.")

if __name__=="__main__": main()
