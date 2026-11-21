#!/usr/bin/env python3
"""Static regression checks for the Phase 3 feature batch and its exclusions."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")

def main() -> None:
    behavior = read("src/main/java/com/relaymann/theoutlands/event/EntityBehaviorDirector.java")
    ambience = read("src/main/java/com/relaymann/theoutlands/event/EnvironmentalPresenceManager.java")
    dressing = read("src/main/java/com/relaymann/theoutlands/worldgen/StructureInteriorDressing.java")
    feature = read("src/main/java/com/relaymann/theoutlands/worldgen/OutlandsStructureFeature.java")
    intelligence = read("src/main/java/com/relaymann/theoutlands/event/OutlandsIntelligenceManager.java")
    ecosystem = read("src/main/java/com/relaymann/theoutlands/event/OutlandsEcosystemManager.java")
    assert "@EventBusSubscriber" in behavior and "stalkFromCover" in behavior and "LurkerEntity" in behavior
    assert "LURKER_BREATH" in ambience and "STALKER_BREATH" in ambience and "CAVEJAR_STALK" in ambience
    assert "NEXT_CUE.remove" in ambience and "NEXT_CUE.clear" in ambience
    assert "StructureInteriorDressing.apply(id,level,x,surfaceY,z,random,clip)" in feature
    assert "clip.isInside(pos)" in dressing and "level.setBlock(pos, detail, 2)" in dressing
    assert "adaptPursuit" in intelligence and "investigateDisturbances" in intelligence
    assert "shareTarget" in ecosystem and "investigateMimic" in ecosystem
    entities = read("src/main/java/com/relaymann/theoutlands/entity/OutlandsEntities.java").lower()
    assert "mimicker" not in entities
    assert "journal" not in entities and "evidence" not in entities
    print("Phase 3 feature and exclusion audit passed.")

if __name__ == "__main__":
    main()
