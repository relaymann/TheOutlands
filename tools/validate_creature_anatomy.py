#!/usr/bin/env python3
"""Validate generated Outlands rigs, cube UV bounds, hierarchy, and animation tracks."""
import json
import math
from pathlib import Path

ROOT = Path("src/generated/resources/assets/theoutlands")
IDS = ("null fear death almighty null_apostle cave_jar stalker observer hunter burrower mimic "
       "stillborn_statue agitated_one lurker howler dredge pilgrim ceiling_dweller colossus "
       "nightmare_watcher nightmare_hound nightmare_wardened nightmare_false_survivor nightmare_unbound").split()
ACTIONS = ("idle", "walk", "sprint", "attack", "death")


def validate_rig(entity, path):
    if not path.is_file():
        raise SystemExit("Missing generated geometry: " + entity)
    data = json.loads(path.read_text(encoding="utf-8"))
    geometry = data["minecraft:geometry"][0]
    desc = geometry["description"]
    width, height = desc["texture_width"], desc["texture_height"]
    bones = geometry["bones"]
    by_name = {}
    for bone in bones:
        name = bone["name"]
        if name in by_name:
            raise SystemExit("%s has duplicate bone %s" % (entity, name))
        by_name[name] = bone
    for bone in bones:
        parent = bone.get("parent")
        if parent and parent not in by_name:
            raise SystemExit("%s bone %s has missing parent %s" % (entity, bone["name"], parent))
        for cube in bone.get("cubes", []):
            origin, size = cube.get("origin"), cube.get("size")
            if not isinstance(origin, list) or not isinstance(size, list) or len(origin) != 3 or len(size) != 3:
                raise SystemExit("%s bone %s has malformed cube geometry" % (entity, bone["name"]))
            if not all(math.isfinite(float(v)) for v in origin + size) or any(float(v) <= 0 for v in size):
                raise SystemExit("%s bone %s has non-finite or non-positive cube dimensions" % (entity, bone["name"]))
            uv = cube.get("uv")
            if isinstance(uv, list):
                if len(uv) != 2:
                    raise SystemExit("%s bone %s has malformed cube UV" % (entity, bone["name"]))
                u, v = map(float, uv)
                sx, sy, sz = map(float, size)
                # Bedrock box UVs unfold to 2*(x+z)+2 by y+2*z+2 pixels.
                uv_width = 2 * (sx + sz) + 2
                uv_height = sy + 2 * sz + 2
                if u < 0 or v < 0 or u + uv_width > width + 1e-6 or v + uv_height > height + 1e-6:
                    raise SystemExit("%s bone %s cube UV [%s,%s] exceeds %sx%s atlas (needs %.2fx%.2f)" %
                                     (entity, bone["name"], u, v, width, height, uv_width, uv_height))
    return set(by_name), {name for name in by_name if name.startswith("anatomy_" + entity + "_")}


animations = json.loads((ROOT / "animations/entity/outlands_monster.animation.json").read_text(encoding="utf-8"))["animations"]
sets = {}
total = 0
for entity in IDS:
    names, anatomy = validate_rig(entity, ROOT / "geo/entity" / (entity + ".geo.json"))
    if len(anatomy) < 6:
        raise SystemExit("%s has too few anatomical structures (%d)" % (entity, len(anatomy)))
    sets[entity] = anatomy
    total += len(anatomy)
    for action in ACTIONS:
        clip = animations.get("animation.outlands." + action + "_" + entity)
        if not clip:
            raise SystemExit("Missing " + action + " clip for " + entity)
        missing = anatomy - set(clip.get("bones", {}))
        if missing:
            raise SystemExit("%s %s lacks animation for %s" % (entity, action, sorted(missing)))
        unknown = set(clip.get("bones", {})) - names
        if unknown:
            raise SystemExit("%s %s animates missing rig bones: %s" % (entity, action, sorted(unknown)))
if len({tuple(sorted(v)) for v in sets.values()}) != len(IDS):
    raise SystemExit("Creature anatomy signatures are not individually distinct")
print("Model audit passed: 24 unique rigs, %d anatomy bones, valid hierarchies/cube dimensions/UV bounds, and 120 articulated clips." % total)
