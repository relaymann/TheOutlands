#!/usr/bin/env python3
"""Validate generated anatomy rigs and all per-creature articulation tracks."""
import json
from pathlib import Path

ROOT=Path("src/generated/resources/assets/theoutlands")
IDS=("null fear death almighty null_apostle cave_jar stalker observer hunter burrower mimic "
     "stillborn_statue agitated_one lurker howler dredge pilgrim ceiling_dweller colossus "
     "nightmare_watcher nightmare_hound nightmare_wardened nightmare_false_survivor nightmare_unbound").split()
animations=json.loads((ROOT/"animations/entity/outlands_monster.animation.json").read_text())["animations"]
sets={}
for entity in IDS:
    path=ROOT/"geo/entity"/(entity+".geo.json")
    if not path.is_file(): raise SystemExit("Missing generated geometry: "+entity)
    rig=json.loads(path.read_text())["minecraft:geometry"][0]
    names={b["name"] for b in rig["bones"]}
    anatomy={n for n in names if n.startswith("anatomy_"+entity+"_")}
    if len(anatomy)<6: raise SystemExit("%s has too few anatomical structures (%d)"%(entity,len(anatomy)))
    sets[entity]=anatomy
    for action in ("idle","walk","sprint","attack","death"):
        clip=animations.get("animation.outlands."+action+"_"+entity)
        if not clip: raise SystemExit("Missing "+action+" clip for "+entity)
        missing=anatomy-set(clip.get("bones",{}))
        if missing: raise SystemExit("%s %s lacks animation for %s"%(entity,action,sorted(missing)))
if len({tuple(sorted(v)) for v in sets.values()})!=len(IDS):
    raise SystemExit("Creature anatomy signatures are not individually distinct")
print("Anatomy audit passed: 24 unique rigs, %d anatomical bones, 120 fully articulated creature clips."%sum(map(len,sets.values())))
