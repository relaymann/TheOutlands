#!/usr/bin/env python3
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

def sculpt(e, rig):
    prefix = "anatomy_" + e + "_"
    by_name = {b["name"]: b for b in rig["bones"]}
    def bounds(name):
        cubes = by_name[name].get("cubes", [])
        if not cubes:
            raise SystemExit("Missing geometry for " + name + " in " + e)
        return (
            min(c["origin"][0] for c in cubes),
            min(c["origin"][1] for c in cubes),
            min(c["origin"][2] for c in cubes),
            max(c["origin"][0] + c["size"][0] for c in cubes),
            max(c["origin"][1] + c["size"][1] for c in cubes),
            max(c["origin"][2] + c["size"][2] for c in cubes)
        )
    def overlay(name, parent, box, uv):
        x0, y0, z0, x1, y1, z1 = box
        size = [round(x1-x0, 4), round(y1-y0, 4), round(z1-z0, 4)]
        if min(size) <= 0:
            raise SystemExit("Invalid sculpted geometry for " + e + ": " + name)
        pivot = [round((x0+x1)/2, 4), round((y0+y1)/2, 4), round((z0+z1)/2, 4)]
        return part(prefix+name, parent, pivot, [cube([round(x0,4),round(y0,4),round(z0,4)], size, uv)])
    body = bounds("body")
    head = bounds("head")
    family = FAMILY[e]
    width = body[3]-body[0]
    height = body[4]-body[1]
    depth = body[5]-body[2]
    scale = {"humanoid":1.0,"predator":1.12,"arthropod":1.08,"heavy":1.24,"eldritch":0.92}[family]
    center = (body[0]+body[3])/2
    front = body[2]
    rear = body[5]
    additions = [
        overlay("pectoral_l","body",(center-width*.47,body[1]+height*.17,front-.52,center-width*.025,body[1]+height*.59,front+.12),(0,40)),
        overlay("pectoral_r","body",(center+width*.025,body[1]+height*.17,front-.52,center+width*.47,body[1]+height*.59,front+.12),(28,40)),
        overlay("abdominal_keel","body",(center-width*.28,body[1]+height*.48,front-.48,center+width*.28,body[1]+height*.94,front+.08),(0,40)),
        overlay("dorsal_plate","body",(center-width*.36,body[1]+height*.20,rear-.08,center+width*.36,body[1]+height*.76,rear+.58),(28,40))
    ]
    for side, arm_name in (("l","left_arm"),("r","right_arm")):
        x0,y0,z0,x1,y1,z1 = bounds(arm_name)
        w=x1-x0
        h=y1-y0
        d=z1-z0
        additions.append(overlay("upper_arm_"+side,arm_name,(x0-w*.13,y0+h*.12,z0-d*.12,x1+w*.13,y0+h*.53,z1+d*.12),(0,40)))
        additions.append(overlay("forearm_"+side,arm_name,(x0-w*.20,y0+h*.52,z0-d*.18,x1+w*.20,y0+h*.88,z1+d*.18),(28,40)))
    for side, leg_name in (("l","left_leg"),("r","right_leg")):
        x0,y0,z0,x1,y1,z1 = bounds(leg_name)
        w=x1-x0
        h=y1-y0
        d=z1-z0
        additions.append(overlay("thigh_"+side,leg_name,(x0-w*.15,y0+h*.04,z0-d*.12,x1+w*.15,y0+h*.49,z1+d*.12),(0,56)))
        additions.append(overlay("shin_"+side,leg_name,(x0-w*.10,y0+h*.49,z0-d*.10,x1+w*.10,y0+h*.88,z1+d*.10),(28,40)))
        additions.append(overlay("talon_boot_"+side,leg_name,(x0-w*.18,y0+h*.82,z0-1.0,x1+w*.18,y1,z1+.2),(44,40)))
    hx0,hy0,hz0,hx1,hy1,hz1=head
    hw=hx1-hx0
    hh=hy1-hy0
    hd=hz1-hz0
    additions.append(overlay("cheek_l","head",(hx0-hw*.04,hy0+hh*.28,hz0-.34,hx0+hw*.28,hy0+hh*.72,hz0+.22),(44,40)))
    additions.append(overlay("cheek_r","head",(hx1-hw*.28,hy0+hh*.28,hz0-.34,hx1+hw*.04,hy0+hh*.72,hz0+.22),(44,40)))
    if e == "stalker":
        additions.extend([
            overlay("hunched_mantle","body",(center-width*.70,body[1]-.4,front-.65,center+width*.70,body[1]+height*.42,front+.25),(0,40)),
            overlay("pelvic_mass","body",(center-width*.48,body[1]+height*.68,front-.35,center+width*.48,body[4]+.4,rear+.25),(28,40)),
            overlay("spinal_ridge","body",(center-.65,body[1]+height*.08,rear+.35,center+.65,body[4]-.3,rear+1.15),(0,56)),
            overlay("hook_l","left_arm",(bounds("left_arm")[0]-1.0,bounds("left_arm")[4]-.5,bounds("left_arm")[2]-.5,bounds("left_arm")[3]+.9,bounds("left_arm")[4]+3.2,bounds("left_arm")[5]+.5),(44,40)),
            overlay("hook_r","right_arm",(bounds("right_arm")[0]-1.0,bounds("right_arm")[4]-.5,bounds("right_arm")[2]-.5,bounds("right_arm")[3]+.9,bounds("right_arm")[4]+3.8,bounds("right_arm")[5]+.5),(44,48))
        ])
    return additions

def reflect_rig(rig):
    bones = rig["bones"]
    leg_cubes = [c for b in bones if b["name"] in ("left_leg","right_leg") for c in b.get("cubes",[])]
    if not leg_cubes:
        raise SystemExit("Cannot establish foot plane for " + rig["description"]["identifier"])
    floor = max(c["origin"][1]+c["size"][1] for c in leg_cubes)
    for bone in bones:
        if "pivot" in bone:
            bone["pivot"][1] = round(floor-bone["pivot"][1], 5)
        if "rotation" in bone:
            bone["rotation"][0] = -bone["rotation"][0]
            bone["rotation"][2] = -bone["rotation"][2]
        for c in bone.get("cubes",[]):
            c["origin"][1] = round(floor-(c["origin"][1]+c["size"][1]), 5)
    all_cubes = [c for b in bones for c in b.get("cubes",[])]
    low = min(c["origin"][1] for c in all_cubes)
    high = max(c["origin"][1]+c["size"][1] for c in all_cubes)
    rig["description"]["visible_bounds_offset"][1] = round((low+high)/2, 4)
    return floor

def reflect_animations(animations):
    def transform(value, channel):
        if isinstance(value, list) and len(value) >= 3 and all(isinstance(v,(int,float)) for v in value[:3]):
            result = list(value)
            if channel == "rotation":
                result[0] = -result[0]
                result[2] = -result[2]
            elif channel == "position":
                result[1] = -result[1]
            return result
        if isinstance(value, dict):
            return {k:transform(v,channel) if k in ("vector","post","pre") else v for k,v in value.items()}
        return value
    for clip in animations.values():
        for bone in clip.get("bones",{}).values():
            for channel in ("rotation","position"):
                track = bone.get(channel)
                if isinstance(track,dict):
                    for key,value in list(track.items()):
                        track[key] = transform(value,channel)

def main():
    additions_by_entity={}
    for e in IDS:
        src=SRC/"geo/entity"/(e+".geo.json")
        data=json.loads(src.read_text(encoding="utf-8"))
        rig=data["minecraft:geometry"][0]
        additions=anatomy(e)+sculpt(e,rig)
        names={b["name"] for b in rig["bones"]}
        rig["bones"].extend(b for b in additions if b["name"] not in names)
        reflect_rig(rig)
        dest=OUT/"geo/entity"/src.name; dest.parent.mkdir(parents=True,exist_ok=True)
        dest.write_text(json.dumps(data,indent=2)+"\n",encoding="utf-8")
        additions_by_entity[e]=additions
    src=SRC/"animations/entity/outlands_monster.animation.json"
    anim=json.loads(src.read_text(encoding="utf-8"))["animations"]
    for e in IDS:
        for action in ("idle","walk","sprint","attack","death"):
            name="animation.outlands."+action+"_"+e
            if name not in anim: raise SystemExit("Missing required animation "+name)
            clip=anim[name]; duration=float(clip.get("animation_length",1))
            mid=round(duration*(.34 if action in ("attack","death") else .5),3)
            for b in additions_by_entity[e]:
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
    reflect_animations(anim)
    dest=OUT/"animations/entity/outlands_monster.animation.json"
    dest.parent.mkdir(parents=True,exist_ok=True)
    dest.write_text(json.dumps({"format_version":"1.8.0","animations":anim},indent=2)+"\n",encoding="utf-8")
    print("Generated unique anatomy for 24 creatures and articulated all 120 creature-specific clips.")

if __name__=="__main__": main()
