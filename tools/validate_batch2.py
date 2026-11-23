#!/usr/bin/env python3
"""Regression audit for Batch 2 investigation equipment and exploration sites."""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")

def main() -> None:
    items = read("src/main/java/com/relaymann/theoutlands/item/OutlandsItems.java")
    tools = read("src/main/java/com/relaymann/theoutlands/item/InvestigationToolItem.java")
    feature = read("src/main/java/com/relaymann/theoutlands/worldgen/OutlandsStructureFeature.java")
    ambience = read("src/main/java/com/relaymann/theoutlands/event/StructureAmbienceManager.java")
    policy = read("src/main/java/com/relaymann/theoutlands/worldgen/StructureLayoutPolicy.java")
    names = ("motion_detector", "emf_receiver", "field_camera", "thermal_viewer",
             "shortwave_radio", "acoustic_recorder", "tripwire_alarm")
    for name in names:
        assert f'"{name}"' in items, f"Missing field tool registration: {name}"
        assert (ROOT / f"src/main/resources/data/theoutlands/recipe/{name}.json").is_file()
        model = ROOT / f"src/main/resources/assets/theoutlands/models/item/{name}.json"
        assert model.is_file()
        json.loads(model.read_text(encoding="utf-8"))
    lang = json.loads(read("src/main/resources/assets/theoutlands/lang/en_us.json"))
    for name in names:
        assert f"item.theoutlands.{name}" in lang
    assert "case Mode.MOTION" in tools and "lengthSqr() > 0.0015D" in tools
    assert "new java.util.ArrayList<>(nearby)" in tools
    assert "interference" in tools and "isRainingAt" in tools
    assert "The Outlands" not in tools and "written_book_content" not in tools
    sites = ("ranger_station", "mining_operation", "ruined_settlement",
             "observation_tower", "subterranean_complex")
    switch = feature.split("switch(id)", 1)[1].split("default", 1)[0]
    predicate = feature.split("private static boolean isNewExplorationSite", 1)[1].split("}", 1)[0]
    for site in sites:
        assert f'case "{site}"' in switch, f"Missing exploration layout: {site}"
        assert site in predicate, f"{site} must not generate hidden research/evidence caches"
        assert f'case "{site}"' in ambience, f"Missing ambience mapping: {site}"
    assert "nightmare_signal_hollow" in policy and "weights[23]" in policy
    nightmare_gate = policy.split("if (nightmare)", 1)[1]
    assert "weights[23]" in nightmare_gate
    assert 'case "nightmare_signal_hollow"' in switch
    assert 'case "nightmare_signal_hollow"' in ambience
    print("Batch 2 field equipment, exploration layouts, ambience, and mystery-preserving cache exclusions validated.")

if __name__ == "__main__":
    main()
