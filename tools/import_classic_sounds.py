#!/usr/bin/env python3
"""Import classic-era OGG files locally and configure The Outlands sound aliases.

The supplied archive is labelled Minecraft 1.2.5 assets-sound. It is not
claimed to be an Alpha 1.2.6 audio archive. Files stay in ignored generated
resources and are never committed to the public repository.
"""
from __future__ import annotations
import argparse
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/generated/resources/assets/theoutlands/sounds/legacy"
SOUNDS_JSON = ROOT / "src/main/resources/assets/theoutlands/sounds.json"

GROUPS = {
    "alpha_step_stone": "step/stone", "alpha_step_grass": "step/grass",
    "alpha_step_gravel": "step/gravel", "alpha_step_sand": "step/sand",
    "alpha_step_snow": "step/snow", "alpha_step_wood": "step/wood",
    "alpha_step_cloth": "step/cloth", "alpha_step_ladder": "step/ladder",
    "alpha_dig_stone": "dig/stone", "alpha_dig_grass": "dig/grass",
    "alpha_dig_gravel": "dig/gravel", "alpha_dig_sand": "dig/sand",
    "alpha_dig_snow": "dig/snow", "alpha_dig_wood": "dig/wood",
    "alpha_dig_cloth": "dig/cloth", "alpha_door_open": "random/door_open",
    "alpha_door_close": "random/door_close", "alpha_player_hurt": "random/classic_hurt",
    "alpha_fall_small": "damage/fallsmall", "alpha_fall_big": "damage/fallbig",
    "alpha_water": "liquid/water", "alpha_swim": "liquid/swim",
    "alpha_lava": "liquid/lava", "alpha_cave_ambience": "ambient/cave/cave",
    "alpha_rain": "ambient/weather/rain", "alpha_thunder": "ambient/weather/thunder",
    "alpha_chicken": "mob/chicken/say", "alpha_cow": "mob/cow/say",
    "alpha_pig": "mob/pig/say", "alpha_sheep": "mob/sheep/say",
    "alpha_wolf": "mob/wolf/bark", "alpha_zombie": "mob/zombie/say",
    "alpha_skeleton": "mob/skeleton/say", "alpha_spider": "mob/spider/say",
    "alpha_creeper": "mob/creeper/say", "alpha_ghast": "mob/ghast/moan",
    "alpha_enderman": "mob/endermen/idle", "alpha_villager": "mob/villager/idle",
}
SINGLE = {
    "alpha_door_open", "alpha_door_close", "alpha_player_hurt",
    "alpha_fall_small", "alpha_fall_big", "alpha_water", "alpha_lava",
}

def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("sounds_archive", type=Path, help="The supplied classic-era sounds ZIP")
    args = parser.parse_args()
    if not args.sounds_archive.is_file() or not zipfile.is_zipfile(args.sounds_archive):
        parser.error(f"Not a valid sounds ZIP: {args.sounds_archive}")

    sound_names: set[str] = set()
    copied = 0
    with zipfile.ZipFile(args.sounds_archive) as archive:
        for member in archive.namelist():
            if not member.lower().endswith(".ogg"):
                continue
            destination = OUT / member
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_bytes(archive.read(member))
            sound_names.add(member[:-4])
            copied += 1

    definitions = json.loads(SOUNDS_JSON.read_text(encoding="utf-8"))
    configured = 0
    for event_name, prefix in GROUPS.items():
        if event_name in SINGLE:
            variants = [prefix] if prefix in sound_names else []
        else:
            variants = sorted(name for name in sound_names
                              if name.startswith(prefix)
                              and name[len(prefix):].lstrip("0123456789") == "")
        if variants:
            definitions[event_name] = {
                "sounds": [
                    {"name": "theoutlands:legacy/" + name,
                     "volume": 0.65 if event_name.startswith("alpha_step_") else 1.0}
                    for name in variants
                ]
            }
            configured += 1

    SOUNDS_JSON.write_text(json.dumps(definitions, indent=2) + "\n", encoding="utf-8")
    print(f"Imported {copied} OGG files and configured {configured} classic sound aliases.")
    print(f"Audio files remain local under {OUT}; do not commit them.")

if __name__ == "__main__":
    main()
