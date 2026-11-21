#!/usr/bin/env python3
"""Verify that distributable JARs actually contain Outlands entity textures and audio."""
from pathlib import Path
from zipfile import ZipFile
import sys

ROOT = Path(__file__).resolve().parents[1]
JARS = sorted(p for p in (ROOT / "build/libs").glob("*.jar")
              if not any(s in p.name for s in ("-sources", "-javadoc", "-dev")))

TEXTURES = (
    "agitated_one", "almighty", "burrower", "cave_jar", "ceiling_dweller",
    "colossus", "death", "dredge", "fear", "howler", "hunter", "lurker",
    "mimic", "null", "null_apostle", "observer", "pilgrim", "stalker",
    "stillborn_statue",
)
SOUNDS = (
    "null_presence", "fear_presence", "death_presence", "almighty_presence",
    "null_apostle_whisper", "cavejar_stalk", "stalker_breath", "observer_click",
    "hunter_stalk", "burrower_rise", "mimic_reveal", "stillborn_stone",
    "agitated_warning", "lurker_breath", "howler_call", "dredge_crawl",
    "pilgrim_breath", "ceiling_crawl", "colossus_step",
)

if len(JARS) != 1:
    raise SystemExit(f"Expected one distributable JAR, found {len(JARS)}: {JARS}")

with ZipFile(JARS[0]) as jar:
    names = set(jar.namelist())
    missing_textures = [name for name in TEXTURES
                        if f"assets/theoutlands/textures/entity/{name}.png" not in names]
    missing_sounds = [name for name in SOUNDS
                      if f"assets/theoutlands/sounds/{name}.ogg" not in names]
    if "assets/theoutlands/sounds.json" not in names:
        missing_sounds.append("sounds.json")
    if missing_textures or missing_sounds:
        if missing_textures:
            print("Missing packaged textures:", ", ".join(missing_textures), file=sys.stderr)
        if missing_sounds:
            print("Missing packaged audio:", ", ".join(missing_sounds), file=sys.stderr)
        raise SystemExit(1)

print(f"Packaged asset audit passed: {len(TEXTURES)} entity textures and "
      f"{len(SOUNDS)} entity sounds are present in {JARS[0].name}.")
