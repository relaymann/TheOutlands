#!/usr/bin/env python3
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
    if len(anatomy)<14: raise SystemExit("%s has too few anatomical structures (%d)"%(entity,len(anatomy)))
    def bounds(bone_name):
        cubes=[c for b in rig["bones"] if b["name"]==bone_name for c in b.get("cubes",[])]
        if not cubes: raise SystemExit("%s is missing %s geometry"%(entity,bone_name))
        return (min(c["origin"][1] for c in cubes),max(c["origin"][1]+c["size"][1] for c in cubes))
    head=bounds("head")
    body=bounds("body")
    legs=[bounds(n) for n in ("left_leg","right_leg")]
    if min(v[0] for v in legs)<-0.01: raise SystemExit("%s feet extend below the ground plane"%entity)
    if (head[0]+head[1])/2 <= (body[0]+body[1])/2: raise SystemExit("%s head is not above its torso"%entity)
    if (body[0]+body[1])/2 <= sum((v[0]+v[1])/2 for v in legs)/2: raise SystemExit("%s torso is not above its legs"%entity)
    for bone in rig["bones"]:
        for cube_data in bone.get("cubes",[]):
            if any(value<=0 for value in cube_data["size"]): raise SystemExit("%s has a non-positive cube"%entity)
    sets[entity]=anatomy
    for action in ("idle","walk","sprint","attack","death"):
        clip=animations.get("animation.outlands."+action+"_"+entity)
        if not clip: raise SystemExit("Missing "+action+" clip for "+entity)
        missing=anatomy-set(clip.get("bones",{}))
        if missing: raise SystemExit("%s %s lacks animation for %s"%(entity,action,sorted(missing)))
if len({tuple(sorted(v)) for v in sets.values()})!=len(IDS):
    raise SystemExit("Creature anatomy signatures are not individually distinct")
print("Anatomy audit passed: 24 unique rigs, %d anatomical bones, 120 fully articulated creature clips."%sum(map(len,sets.values())))
