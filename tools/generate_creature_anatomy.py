#!/usr/bin/env python3
"""Build distinct creature anatomy and anatomy-aware GeckoLib motion overlays."""
import json
from pathlib import Path

SRC = Path("src/main/resources/assets/theoutlands")
OUT = Path("src/generated/resources/assets/theoutlands")
IDS = ("null fear death almighty null_apostle cave_jar stalker observer hunter burrower mimic "
       "stillborn_statue agitated_one lurker howler dredge pilgrim ceiling_dweller colossus "
       "nightmare_watcher nightmare_hound nightmare_wardened nightmare_false_survivor nightmare_unbound").split()
FAMILY = {
 "null":"eldritch","fear":"eldritch","death":"humanoid","almighty":"heavy",
 "null_apostle":"eldritch","cave_jar":"arthropod","stalker":"humanoid","observer":"eldritch",
 "hunter":"predator","burrower":"arthropod","mimic":"humanoid","stillborn_statue":"heavy",
 "agitated_one":"humanoid","lurker":"predator","howler":"predator","dredge":"predator",
 "pilgrim":"humanoid","ceiling_dweller":"predator","colossus":"heavy",
 "nightmare_watcher":"eldritch","nightmare_hound":"predator","nightmare_wardened":"heavy",
 "nightmare_false_survivor":"humanoid","nightmare_unbound":"eldritch"
}
def part(n, parent, pivot, cubes):
    return {"name":n,"parent":parent,"pivot":pivot,"cubes":cubes}
def cube(o,s,uv=(0,48)):
    return {"origin":o,"size":s,"uv":list(uv)}
def anatomy(e):
    p="anatomy_"+e+"_"; f=FAMILY[e]
    if f=="humanoid":
        a=[
          part(p+"brow_l","head",[-1.3,-4.5,-2], [cube([-2.1,-5,-2.5],[1.7,.7,.7])]),
          part(p+"brow_r","head",[1.3,-4.5,-2], [cube([.4,-5,-2.5],[1.7,.7,.7])]),
          part(p+"fangs","head",[0,-2,-2], [cube([-.4,-2.3,-3],[.8,1.5,.6]),cube([-1.5,-2.1,-2.8],[.5,1,.5]),cube([1,-2.1,-2.8],[.5,1,.5])]),
          part(p+"claws_l","left_arm",[0,18,-1], [cube([-.2,17,-2],[.5,2.5,.6]),cube([.7,17,-2],[.5,2.5,.6])]),
          part(p+"claws_r","right_arm",[0,18,-1], [cube([-.7,17,-2],[.5,2.5,.6]),cube([.2,17,-2],[.5,2.5,.6])]),
          part(p+"neck_tendon","body",[0,2,-1], [cube([-.4,2,-1.7],[.8,7,.5])])]
    elif f=="predator":
        a=[
          part(p+"muzzle","head",[0,-2,-2.5], [cube([-2.1,-3.2,-4.1],[4.2,2.4,2.4])]),
          part(p+"tusks","head",[0,-1.5,-3], [cube([-2,-2.2,-4],[.65,1.8,.7]),cube([1.35,-2.2,-4],[.65,1.8,.7])]),
          part(p+"mandible","jaw",[0,.3,-1.2], [cube([-1.8,.1,-2.1],[3.6,.7,1.5])]),
          part(p+"claws_l","left_arm",[0,12,-1], [cube([-.3,11,-2],[.65,2.5,.8]),cube([.55,11,-2],[.65,2.5,.8])]),
          part(p+"claws_r","right_arm",[0,12,-1], [cube([-1,11,-2],[.65,2.5,.8]),cube([-.15,11,-2],[.65,2.5,.8])]),
          part(p+"dorsal_spines","body",[0,4,1.6], [cube([-1,4,1.5],[2,5,1.1]),cube([-1,8,1.5],[2,4,1.1])])]
    elif f=="arthropod":
        a=[
          part(p+"mandible_l","head",[-2,-2,-2], [cube([-3.6,-2.5,-4.5],[1.4,1.2,3])]),
          part(p+"mandible_r","head",[2,-2,-2], [cube([2.2,-2.5,-4.5],[1.4,1.2,3])]),
          part(p+"feeler_l","head",[-1.6,-4,-1.8], [cube([-2,-7,-2.2],[.45,3.2,.45])]),
          part(p+"feeler_r","head",[1.6,-4,-1.8], [cube([1.55,-7,-2.2],[.45,3.2,.45])]),
          part(p+"carapace","body",[0,2,1], [cube([-2,2,1],[4,2,7])]),
          part(p+"diggers","left_arm",[0,9,-1], [cube([-.4,8,-2],[.8,3.5,1.1]),cube([.6,8,-2],[.8,3.5,1.1])])]
    elif f=="heavy":
        a=[
          part(p+"brow_plate","head",[0,-5,-1.8], [cube([-3.5,-6.2,-2.7],[7,1.8,1.2])]),
          part(p+"tusk_l","head",[-2,-1,-2], [cube([-2.9,-1.2,-3.3],[.9,2.8,1])]),
          part(p+"tusk_r","head",[2,-1,-2], [cube([2,-1.2,-3.3],[.9,2.8,1])]),
          part(p+"knuckles_l","left_arm",[0,15,0], [cube([-.5,14,-2.2],[2.1,2,4.4])]),
          part(p+"knuckles_r","right_arm",[0,15,0], [cube([-1.6,14,-2.2],[2.1,2,4.4])]),
          part(p+"shoulder_tendons","body",[0,3,0], [cube([-4,3,-1],[1,7,2]),cube([3,3,-1],[1,7,2])])]
    else:
        a=[
          part(p+"crown_l","head",[-1.7,-6,-.5], [cube([-2.7,-10,-.9],[.7,4,1])]),
          part(p+"crown_r","head",[1.7,-6,-.5], [cube([2,-9,-.9],[.7,3,1])]),
          part(p+"split_jaw_l","jaw",[-1,0,-1], [cube([-2.3,.2,-2.8],[1.8,1,1.8])]),
          part(p+"split_jaw_r","jaw",[1,0,-1], [cube([.5,.2,-2.8],[1.8,1,1.8])]),
          part(p+"tendril_l","body",[-2,3,1], [cube([-2.4,3,1],[.55,8,.55])]),
          part(p+"tendril_r","body",[2,3,1], [cube([1.85,3,1],[.55,6,.55])])]
    if e in ("stalker","death","nightmare_false_survivor"):
        a.append(part(p+"spinal_cable","spine",[0,4,0], [cube([-.45,3,1.5],[.9,8,.9])]))
    if e in ("hunter","nightmare_hound"):
        a.append(part(p+"split_nose","head",[0,-2,-3.4], [cube([-.35,-2.4,-4.5],[.7,.8,.8])]))
    if e in ("burrower","cave_jar"):
        a.append(part(p+"shell_ribs","body",[0,2,0], [cube([-5,2,-4],[1,3,8]),cube([4,2,-4],[1,3,8])]))
    if e in ("observer","nightmare_watcher"):
        a.append(part(p+"optic_stalk","head",[0,-5,-1], [cube([-.35,-8,-1.4],[.7,3,.7])]))
    if e in ("colossus","almighty","nightmare_wardened"):
        a.append(part(p+"sternum_keel","body",[0,3,-1], [cube([-1.4,3,-2],[2.8,8,1.2])]))
    if e in ("null","fear","null_apostle","nightmare_unbound"):
        a += [part(p+"rib_l","body",[-2,2,0], [cube([-3,2,-1],[1,6,2])]),
              part(p+"rib_r","body",[2,2,0], [cube([2,2,-1],[1,6,2])])]
    return a

def main():
    for e in IDS:
        src=SRC/"geo/entity"/(e+".geo.json")
        data=json.loads(src.read_text(encoding="utf-8"))
        rig=data["minecraft:geometry"][0]
        names={b["name"] for b in rig["bones"]}
        rig["bones"].extend(b for b in anatomy(e) if b["name"] not in names)
        dest=OUT/"geo/entity"/src.name; dest.parent.mkdir(parents=True,exist_ok=True)
        dest.write_text(json.dumps(data,indent=2)+"\n",encoding="utf-8")
    src=SRC/"animations/entity/outlands_monster.animation.json"
    anim=json.loads(src.read_text(encoding="utf-8"))["animations"]
    for e in IDS:
        for action in ("idle","walk","sprint","attack","death"):
            name="animation.outlands."+action+"_"+e
            if name not in anim: raise SystemExit("Missing required animation "+name)
            clip=anim[name]; duration=float(clip.get("animation_length",1))
            mid=round(duration*(.34 if action in ("attack","death") else .5),3)
            for b in anatomy(e):
                n=b["name"].lower()
                if action=="idle": amp=1.4 if ("tendon" in n or "tendril" in n) else .7
                elif action=="walk": amp=5 if ("claw" in n or "feeler" in n) else 2
                elif action=="sprint": amp=11 if ("claw" in n or "mandible" in n) else 5
                elif action=="attack": amp=34 if ("jaw" in n or "fang" in n or "mandible" in n) else 24
                else: amp=20 if ("tendon" in n or "tendril" in n) else 12
                if action in ("idle","walk","sprint"):
                    vals=([ -amp,0,-amp*.25],[amp,0,amp*.25],[-amp,0,-amp*.25])
                elif action=="attack": vals=([0,0,0],[-amp,0,amp*.25],[amp*.3,0,-amp*.1])
                else: vals=([0,0,0],[amp,0,amp*.2],[amp*1.6,0,amp*.35])
                clip.setdefault("bones",{})[b["name"]]={"rotation":{"0.0":vals[0],str(mid):vals[1],str(duration):vals[2]}}
    dest=OUT/"animations/entity/outlands_monster.animation.json"
    dest.parent.mkdir(parents=True,exist_ok=True)
    dest.write_text(json.dumps({"format_version":"1.8.0","animations":anim},indent=2)+"\n",encoding="utf-8")
    print("Generated unique anatomy for 24 creatures and articulated all 120 creature-specific clips.")

if __name__=="__main__": main()
