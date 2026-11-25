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
                # Bedrock box UV net dimensions are 2*(x+z) by y+2*z; no padding is added.
                uv_width = 2 * (sx + sz)
                uv_height = sy + 2 * sz
                if u < 0 or v < 0 or u + uv_width > width + 1e-6 or v + uv_height > height + 1e-6:
                    raise SystemExit("%s bone %s cube UV [%s,%s] exceeds %sx%s atlas (needs %.2fx%.2f)" %
                                     (entity, bone["name"], u, v, width, height, uv_width, uv_height))
    return set(by_name), {name for name in by_name if name.startswith("anatomy_" + entity + "_")}


animations = json.loads((ROOT / "animations/entity/outlands_monster.animation.json").read_text(encoding="utf-8"))["animations"]


def check(condition, message):
    if not condition:
        raise SystemExit(message)


source_root = Path("src/main/resources/assets/theoutlands")
source_animations = json.loads((source_root / "animations/entity/outlands_monster.animation.json").read_text(encoding="utf-8"))["animations"]
rig_data = {}
rig_sets = {}
for entity in IDS:
    path = ROOT / "geo/entity" / (entity + ".geo.json")
    names, anatomy = validate_rig(entity, path)
    rig_data[entity] = json.loads(path.read_text(encoding="utf-8"))["minecraft:geometry"][0]
    rig_sets[entity] = anatomy


def a01():
    check(len(rig_data) == 24, "Expected 24 generated rigs")
    check(all((ROOT / "geo/entity" / (e + ".geo.json")).is_file() for e in IDS), "Missing creature rig")


def a02():
    for e, g in rig_data.items():
        d = g["description"]
        check(d.get("identifier") == "geometry.theoutlands." + e, e + " geometry identifier mismatch")
        check(d.get("texture_width", 0) > 0 and d.get("texture_height", 0) > 0, e + " invalid atlas dimensions")


def a03():
    for e, g in rig_data.items():
        n = [b["name"] for b in g["bones"]]
        check(len(n) == len(set(n)), e + " duplicate bone names")


def a04():
    for e, g in rig_data.items():
        n = {b["name"] for b in g["bones"]}
        for b in g["bones"]:
            check(not b.get("parent") or b["parent"] in n, e + "/" + b["name"] + " missing parent")


def a05():
    for e, g in rig_data.items():
        parents = {b["name"]: b.get("parent") for b in g["bones"]}
        for name in parents:
            seen, cur = set(), name
            while cur:
                check(cur not in seen, e + " hierarchy cycle at " + name)
                seen.add(cur)
                cur = parents.get(cur)


def a06():
    for e, g in rig_data.items():
        for b in g["bones"]:
            if "pivot" in b:
                check(len(b["pivot"]) == 3 and all(math.isfinite(float(v)) for v in b["pivot"]), e + "/" + b["name"] + " invalid pivot")


def a07():
    for e, g in rig_data.items():
        for b in g["bones"]:
            for c in b.get("cubes", []):
                check(len(c["size"]) == 3 and all(math.isfinite(float(v)) and float(v) > 0 for v in c["size"]), e + "/" + b["name"] + " invalid cube size")


def a08():
    for e, g in rig_data.items():
        for b in g["bones"]:
            for c in b.get("cubes", []):
                check(len(c["origin"]) == 3 and all(math.isfinite(float(v)) for v in c["origin"]), e + "/" + b["name"] + " invalid cube origin")


def a09():
    for e in IDS:
        validate_rig(e, ROOT / "geo/entity" / (e + ".geo.json"))


def a10():
    for e, anatomy in rig_sets.items():
        check(len(anatomy) >= 6, e + " anatomy incomplete")


def a11():
    check(len({tuple(sorted(v)) for v in rig_sets.values()}) == 24, "Anatomy signatures are not unique")


def a12():
    for e in IDS:
        for action in ACTIONS:
            check("animation.outlands." + action + "_" + e in animations, "Missing " + action + " clip for " + e)


def a13():
    for name, clip in animations.items():
        if name.startswith("animation.outlands."):
            duration = float(clip.get("animation_length", 0))
            check(math.isfinite(duration) and duration > 0, name + " invalid duration")


def a14():
    for e in IDS:
        for action in ACTIONS:
            clip = animations["animation.outlands." + action + "_" + e]
            missing = rig_sets[e] - set(clip.get("bones", {}))
            check(not missing, e + "/" + action + " missing anatomy tracks " + str(sorted(missing)))


def a15():
    for e in IDS:
        names = {b["name"] for b in rig_data[e]["bones"]}
        for action in ACTIONS:
            unknown = set(animations["animation.outlands." + action + "_" + e].get("bones", {})) - names
            check(not unknown, e + "/" + action + " animates missing bones " + str(sorted(unknown)))


def a16():
    for name, clip in animations.items():
        duration = float(clip.get("animation_length", 0))
        for bone, channels in clip.get("bones", {}).items():
            for channel, frames in channels.items():
                if isinstance(frames, dict):
                    for timestamp in frames:
                        t = float(timestamp)
                        check(math.isfinite(t) and 0 <= t <= duration + 1e-6, name + "/" + bone + " keyframe outside clip")


def a17():
    for name, clip in animations.items():
        for bone, channels in clip.get("bones", {}).items():
            frames = channels.get("rotation", {})
            if isinstance(frames, dict):
                for value in frames.values():
                    if isinstance(value, list):
                        check(len(value) == 3 and all(math.isfinite(float(v)) for v in value), name + "/" + bone + " invalid rotation")


def a18():
    for e in IDS:
        for action in ACTIONS:
            check(bool(animations["animation.outlands." + action + "_" + e].get("bones")), e + "/" + action + " has no tracks")


def a19():
    for e in IDS:
        for action in ACTIONS:
            name = "animation.outlands." + action + "_" + e
            check(name in source_animations, "Unexpected generated clip " + name)
            check(float(animations[name]["animation_length"]) == float(source_animations[name]["animation_length"]), name + " duration changed")


def a20():
    for e in IDS:
        texture = source_root / "textures/entity" / (e + ".png")
        if e.startswith("nightmare_"):
            texture = source_root / "textures/entity/nightmare" / (e.removeprefix("nightmare_") + ".png")
        check(texture.is_file(), e + " missing texture")
    check(len(rig_data) == 24 and sum(len(v) for v in rig_sets.values()) >= 144, "Anatomy overlay coverage too low")
    check(len([n for n in animations if n.startswith("animation.outlands.") and any(n.endswith("_" + e) for e in IDS)]) >= 120, "Fewer than 120 creature clips")
    check((ROOT / "animations/entity/outlands_monster.animation.json").is_file(), "Generated animation overlay missing")


AUDITS = [
    "24 generated rigs", "geometry identifiers and atlas headers", "unique bone names",
    "valid parent links", "acyclic bone hierarchies", "finite three-axis pivots",
    "positive finite cube dimensions", "finite cube origins", "Bedrock cube UV bounds",
    "anatomy on every creature", "24 distinct anatomy signatures", "five action clips per creature",
    "finite positive animation durations", "anatomy animation coverage", "valid animation bone references",
    "keyframe timestamps", "finite rotation keyframes", "nonempty animation tracks",
    "base animation timing compatibility", "textures and overlay completeness"
]
for i, (title, fn) in enumerate(zip(AUDITS, [a01,a02,a03,a04,a05,a06,a07,a08,a09,a10,a11,a12,a13,a14,a15,a16,a17,a18,a19,a20]), 1):
    fn()
    print("AUDIT %02d/20 PASS — %s" % (i, title))
print("20-PASS MODEL AUDIT COMPLETE: 24 rigs, %d anatomy bones, 120 creature action clips." % sum(len(v) for v in rig_sets.values()))
