#!/usr/bin/env python3
"""Static Batch 3 survival, defense, Nightmare exclusivity, and asset audit."""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")

def main() -> None:
    blocks = read("src/main/java/com/relaymann/theoutlands/block/OutlandsBlocks.java")
    defense = read("src/main/java/com/relaymann/theoutlands/block/SurvivalDefenseBlock.java")
    items = read("src/main/java/com/relaymann/theoutlands/item/OutlandsItems.java")
    ward = read("src/main/java/com/relaymann/theoutlands/item/NightmareWardItem.java")
    main_class = read("src/main/java/com/relaymann/theoutlands/TheOutlands.java")
    nightmare = read("src/main/java/com/relaymann/theoutlands/event/NightmareModeManager.java")
    creature = read("src/main/java/com/relaymann/theoutlands/entity/NightmareCreature.java")
    policy = read("src/main/java/com/relaymann/theoutlands/worldgen/StructureLayoutPolicy.java")
    feature = read("src/main/java/com/relaymann/theoutlands/worldgen/OutlandsStructureFeature.java")
    assert "REINFORCED_BARRICADE" in blocks and "REINFORCED_DOOR" in blocks
    assert "SPIKE_TRAP" in blocks and "PERIMETER_ALARM" in blocks
    assert "strength(14.0F, 1200.0F)" in blocks and "strength(18.0F, 1600.0F)" in blocks
    assert "DoorBlock" in blocks and "stepOn" in defense and "damageSources().cactus()" in defense
    assert "TRIPWIRE_CLICK_ON" in defense and "ServerLevel" in defense
    for name in ("REINFORCED_BARRICADE", "REINFORCED_DOOR", "SPIKE_TRAP", "PERIMETER_ALARM",
                 "EMERGENCY_FLARE", "REINFORCED_SHIELD", "NIGHTMARE_WARD"):
        assert name in items, f"Missing item registration: {name}"
    assert "OutlandsBlocks.BLOCKS.register(modEventBus)" in main_class
    assert "NightmareMode.isEnabled(level)" in ward and "InteractionResultHolder.fail(stack)" in ward
    assert "hollowEncounter" in nightmare and "NIGHTMARE_FALSE_SURVIVOR" in nightmare
    assert "coordinateNearbyPredators" in creature
    assert '"nightmare_signal_hollow"' in policy and "weights[23]" in policy
    assert 'case "nightmare_signal_hollow"' in feature and '"nightmare_signal_hollow"' in feature
    assert "nightmare_signal_hollow" in feature.split("private static boolean isNewExplorationSite",1)[1].split("}",1)[0]
    assets = [
        "reinforced_barricade", "reinforced_door", "spike_trap", "perimeter_alarm",
        "emergency_flare", "reinforced_shield", "nightmare_ward"
    ]
    for name in assets:
        recipe = ROOT / f"src/main/resources/data/theoutlands/recipe/{name}.json"
        model = ROOT / f"src/main/resources/assets/theoutlands/models/item/{name}.json"
        assert recipe.exists(), f"Missing recipe: {name}"
        assert model.exists(), f"Missing item model: {name}"
        json.loads(recipe.read_text(encoding="utf-8"))
        json.loads(model.read_text(encoding="utf-8"))
    for name in ("reinforced_barricade", "reinforced_door", "spike_trap", "perimeter_alarm"):
        state = ROOT / f"src/main/resources/assets/theoutlands/blockstates/{name}.json"
        assert state.exists(), f"Missing blockstate: {name}"
        state_data = json.loads(state.read_text(encoding="utf-8"))
        if name == "reinforced_door":
            models = {entry["model"] for entry in state_data["variants"].values()}
            assert models == {"minecraft:block/iron_door_bottom", "minecraft:block/iron_door_top"}
    for shield in ("reinforced_shield", "nightmare_ward"):
        shield_model = json.loads(read(f"src/main/resources/assets/theoutlands/models/item/{shield}.json"))
        assert shield_model.get("parent") == "builtin/entity", f"{shield}: shield needs the built-in entity model"
        assert shield_model.get("textures", {}).get("particle") == "minecraft:entity/shield_base_nopattern"
    lang = json.loads(read("src/main/resources/assets/theoutlands/lang/en_us.json"))
    for name in assets:
        assert f"item.theoutlands.{name}" in lang, f"Missing localization: {name}"
    print("Batch 3 survival defenses, equipment, Nightmare-only gear/events/structure, and assets validated.")

if __name__ == "__main__":
    main()
