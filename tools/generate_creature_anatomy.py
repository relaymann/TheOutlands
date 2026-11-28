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
    if e == "stalker":
        a += [
          part(p+"skull_ridge","head",[0,-6,-1], [cube([-1.8,-7,-2.3],[3.6,1.2,2.8],(0,48))]),
          part(p+"finger_hooks_l","left_arm",[0,21,-1], [cube([3.5,21,-2.1],[.65,2.8,.65],(8,48)),cube([4.35,21,-2.1],[.65,2.8,.65],(8,48))]),
          part(p+"finger_hooks_r","right_arm",[0,21,-1], [cube([-5,21,-2.1],[.65,2.8,.65],(8,48)),cube([-4.15,21,-2.1],[.65,2.8,.65],(8,48))])]
    elif e == "hunter":
        a += [
          part(p+"neck_trapezius","body",[0,1,-1], [cube([-3.8,1,-2.5],[7.6,4.5,1.2],(0,48))]),
          part(p+"rib_l","body",[-2,6,-1.5], [cube([-3.8,5,-2.8],[1.1,5,.9],(12,48)),cube([-3.4,10,-2.5],[1,2,.8],(12,48))]),
          part(p+"rib_r","body",[2,6,-1.5], [cube([2.7,5,-2.8],[1.1,5,.9],(12,48)),cube([2.4,10,-2.5],[1,2,.8],(12,48))]),
          part(p+"hooked_fingers_l","left_arm",[0,18,-1], [cube([6.2,18,-3.5],[.7,3,.8],(20,48)),cube([7,18,-3.5],[.7,2.2,.8],(20,48))]),
          part(p+"hooked_fingers_r","right_arm",[0,18,-1], [cube([-7.7,18,-3.5],[.7,3,.8],(20,48)),cube([-8.5,18,-3.5],[.7,2.2,.8],(20,48))])]
    elif e == "lurker":
        a += [
          part(p+"hump","body",[0,2,1.5], [cube([-4.5,2,1.5],[9,5,3],(0,48))]),
          part(p+"rib_l","body",[-3,4,-2], [cube([-4.7,3,-3.8],[1.2,5,1],(12,48)),cube([-4.4,7,-3.8],[1.2,4,1],(12,48))]),
          part(p+"rib_r","body",[3,4,-2], [cube([3.5,3,-3.8],[1.2,5,1],(12,48)),cube([3.2,7,-3.8],[1.2,4,1],(12,48))]),
          part(p+"wrist_spurs_l","left_arm",[0,17,-1], [cube([8,16,-2.8],[1.4,3,1.2],(20,48))]),
          part(p+"wrist_spurs_r","right_arm",[0,17,-1], [cube([-9.4,16,-2.8],[1.4,3,1.2],(20,48))])]
    elif e == "observer":
        a += [
          part(p+"outer_eye_l","head",[-2.3,-4,-3.8], [cube([-3.7,-5.3,-4.9],[2.2,2.2,.65],(0,48))]),
          part(p+"outer_eye_r","head",[2.3,-4,-3.8], [cube([1.5,-5.3,-4.9],[2.2,2.2,.65],(0,48))]),
          part(p+"central_lens","head",[0,-3.5,-4], [cube([-1.5,-4.8,-5.2],[3,2.6,.7],(16,48))]),
          part(p+"neck_cables","body",[0,2,0], [cube([-.35,2,-1.9],[.7,8,.7],(32,48)),cube([-.9,3,1.4],[.55,7,.55],(32,48))]),
          part(p+"crown_spines","head",[0,-6,0], [cube([-3,-7,.8],[.7,3,.8],(40,48)),cube([2.3,-7,.8],[.7,3,.8],(40,48))])]
    elif e == "stillborn_statue":
        a += [
          part(p+"stone_collar","body",[0,1,-1], [cube([-5.8,1,-4.8],[11.6,3,1.2],(0,48))]),
          part(p+"chest_slab","body",[0,5,-1], [cube([-4.1,4,-4.8],[8.2,7,1.3],(16,48))]),
          part(p+"shoulder_pauldron_l","left_arm",[0,3,0], [cube([4.7,2,-3.2],[5,4,6.4],(32,48))]),
          part(p+"shoulder_pauldron_r","right_arm",[0,3,0], [cube([-9.7,2,-3.2],[5,4,6.4],(32,48))]),
          part(p+"face_mask","head",[0,-5,-2], [cube([-3.6,-6.5,-4.9],[7.2,3.5,1.1],(0,48))])]
    elif e == "nightmare_false_survivor":
        a += [
          part(p+"mask_cheek_l","head",[-2,-3,-2], [cube([-3.1,-4.8,-3.5],[1.8,3.5,1.1],(0,48))]),
          part(p+"mask_cheek_r","head",[2,-3,-2], [cube([1.3,-4.8,-3.5],[1.8,3.5,1.1],(0,48))]),
          part(p+"throat_tendons","body",[0,1,-1], [cube([-.55,1,-2.5],[1.1,8,.8],(16,48)),cube([-1.7,2,-2.3],[.55,6,.7],(16,48)),cube([1.15,2,-2.3],[.55,6,.7],(16,48))]),
          part(p+"sternum_ribs","body",[0,6,-1], [cube([-2.7,5,-2.8],[.7,5,.7],(32,48)),cube([2,5,-2.8],[.7,5,.7],(32,48))]),
          part(p+"split_fingers_l","left_arm",[0,13,-1], [cube([5.9,14,-2.8],[.55,3,.55],(40,48)),cube([6.7,14,-2.8],[.55,2,.55],(40,48))]),
          part(p+"split_fingers_r","right_arm",[0,13,-1], [cube([-7.1,14,-2.8],[.55,3,.55],(40,48)),cube([-7.9,14,-2.8],[.55,2,.55],(40,48))])]
    elif e == "pilgrim":
        a += [
          part(p+"hood_cowl","head",[0,-6,-1], [cube([-3.8,-7.8,-3.9],[7.6,2.3,7.1],(0,96))]),
          part(p+"mantle_l","body",[-3,1,0], [cube([-5.8,1,-3.9],[2.2,7,7.8],(16,96))]),
          part(p+"mantle_r","body",[3,1,0], [cube([3.6,1,-3.9],[2.2,7,7.8],(16,96))]),
          part(p+"prayer_cords","body",[0,3,-3], [cube([-.25,3,-4.1],[.5,10,.6],(32,96)),cube([-.9,11,-4.1],[1.8,.7,.6],(32,96))]),
          part(p+"sleeve_hands_l","left_arm",[0,19,-1], [cube([6.8,18,-2.8],[2.2,4.5,5.6],(40,96))]),
          part(p+"sleeve_hands_r","right_arm",[0,19,-1], [cube([-9,18,-2.8],[2.2,4.5,5.6],(40,96))])]
    elif e == "agitated_one":
        a += [
          part(p+"raised_collar","body",[0,1,-1], [cube([-3.7,1,-2.8],[7.4,3,1.2],(0,48))]),
          part(p+"rib_spurs_l","body",[-2,6,-1], [cube([-3.6,5,-3],[1.2,4,1.1],(12,48)),cube([-3.1,9,-3],[1.1,3,1.1],(12,48))]),
          part(p+"rib_spurs_r","body",[2,6,-1], [cube([2.4,5,-3],[1.2,4,1.1],(12,48)),cube([2,9,-3],[1.1,3,1.1],(12,48))]),
          part(p+"jaw_hooks","jaw",[0,.5,-1], [cube([-2.3,.2,-4.2],[.7,2.5,.8],(24,48)),cube([1.6,.2,-4.2],[.7,2.5,.8],(24,48))]),
          part(p+"knuckle_bone_l","left_arm",[0,15,-1], [cube([4.4,15,-2.3],[1.8,2.2,4.6],(36,48))]),
          part(p+"knuckle_bone_r","right_arm",[0,15,-1], [cube([-6.2,15,-2.3],[1.8,2.2,4.6],(36,48))])]
    elif e == "burrower":
        a += [
          part(p+"shovel_brow","head",[0,-4,-2], [cube([-4.8,-5.5,-5.3],[9.6,1.8,2.1],(0,48))]),
          part(p+"outer_mandible_l","head",[-3,-1,-3], [cube([-4.5,-1.5,-6.2],[1.2,2.4,3.2],(12,48))]),
          part(p+"outer_mandible_r","head",[3,-1,-3], [cube([3.3,-1.5,-6.2],[1.2,2.4,3.2],(12,48))]),
          part(p+"tooth_row_upper","head",[0,-1,-4], [cube([-2.8,-1.4,-6.7],[1.1,1.1,1.1],(24,48)),cube([-.55,-1.4,-6.9],[1.1,1.1,1.1],(24,48)),cube([1.7,-1.4,-6.7],[1.1,1.1,1.1],(24,48))]),
          part(p+"tooth_row_lower","jaw",[0,.4,-2], [cube([-2.4,.2,-3.4],[.9,1.2,1],(32,48)),cube([-.45,.2,-3.6],[.9,1.2,1],(32,48)),cube([1.5,.2,-3.4],[.9,1.2,1],(32,48))]),
          part(p+"digging_blade_l","left_arm",[0,10,-1], [cube([7.2,10,-3.2],[2.2,4,6.4],(40,48)),cube([7.5,12,-4.2],[1.8,4,1.4],(48,48))]),
          part(p+"digging_blade_r","right_arm",[0,10,-1], [cube([-9.4,10,-3.2],[2.2,4,6.4],(40,48)),cube([-9.3,12,-4.2],[1.8,4,1.4],(48,48))]),
          part(p+"tail_segments","tail",[0,4,0], [cube([-1.5,4,9],[3,2.5,2.5],(0,56)),cube([-1.3,4,12],[2.6,2.2,2.4],(12,56)),cube([-1,4,15],[2,1.8,2.2],(24,56))]),
          part(p+"carapace_ridges","body",[0,2,1], [cube([-4.8,5,2],[1.1,2,5],(32,56)),cube([3.7,5,2],[1.1,2,5],(32,56))])]
    elif e == "dredge":
        a += [
          part(p+"angler_jaw_l","head",[-2,-1,-3], [cube([-3.8,-.5,-6.4],[1.5,2.1,3.4],(0,48))]),
          part(p+"angler_jaw_r","head",[2,-1,-3], [cube([2.3,-.5,-6.4],[1.5,2.1,3.4],(12,48))]),
          part(p+"needle_teeth","jaw",[0,.5,-2], [cube([-2.7,.2,-3.5],[.55,1.7,.7],(24,48)),cube([-1.3,.2,-3.7],[.55,1.7,.7],(24,48)),cube([.2,.2,-3.7],[.55,1.7,.7],(24,48)),cube([1.6,.2,-3.5],[.55,1.7,.7],(24,48))]),
          part(p+"gill_frill_l","body",[-5,3,-1], [cube([-6.4,3,-1.8],[1.2,5,3.6],(32,48)),cube([-7,4,-1.4],[.7,4,2.8],(40,48))]),
          part(p+"gill_frill_r","body",[5,3,-1], [cube([5.2,3,-1.8],[1.2,5,3.6],(32,48)),cube([6.3,4,-1.4],[.7,4,2.8],(40,48))]),
          part(p+"hook_fingers_l","left_arm",[0,14,-1], [cube([10.1,13,-4],[.9,3.2,1],(48,48)),cube([10.8,13,-4],[.8,2.5,1],(48,48))]),
          part(p+"hook_fingers_r","right_arm",[0,14,-1], [cube([-11,13,-4],[.9,3.2,1],(48,48)),cube([-11.8,13,-4],[.8,2.5,1],(48,48))]),
          part(p+"barbed_tail","tail",[0,4,0], [cube([-1.2,4,8],[2.4,2,2.4],(0,56)),cube([-1,4,11],[2,1.8,2],(12,56)),cube([-.7,4,14],[1.4,1.5,2],(24,56))]),
          part(p+"sternal_ridges","body",[0,3,-2], [cube([-3.8,3,-3.1],[1,4,1],(32,56)),cube([2.8,3,-3.1],[1,4,1],(32,56))])]
    elif e == "howler":
        a += [
          part(p+"throat_sac","body",[0,3,-2], [cube([-2.7,3,-4],[5.4,7,2.1],(0,48))]),
          part(p+"jaw_hinge_l","head",[-2.5,-1,-3], [cube([-3.4,-1.5,-4.5],[1.2,2.4,2],(12,48))]),
          part(p+"jaw_hinge_r","head",[2.5,-1,-3], [cube([2.2,-1.5,-4.5],[1.2,2.4,2],(12,48))]),
          part(p+"fangs_upper","head",[0,-1,-4], [cube([-2.6,-1,-5.1],[.8,1.7,.8],(24,48)),cube([1.8,-1,-5.1],[.8,1.7,.8],(24,48))]),
          part(p+"fangs_lower","jaw",[0,.5,-2], [cube([-2,.4,-3.6],[.8,1.6,.8],(32,48)),cube([1.2,.4,-3.6],[.8,1.6,.8],(32,48))]),
          part(p+"forearm_hook_l","left_arm",[0,16,-1], [cube([6.5,15,-3.5],[1.1,3.4,1.1],(40,48)),cube([7.3,15,-3.5],[1,2.7,1],(40,48))]),
          part(p+"forearm_hook_r","right_arm",[0,16,-1], [cube([-7.6,15,-3.5],[1.1,3.4,1.1],(40,48)),cube([-8.4,15,-3.5],[1,2.7,1],(40,48))]),
          part(p+"hock_l","left_leg",[0,23,0], [cube([.1,22,-1.8],[2.8,4,3.6],(48,48))]),
          part(p+"hock_r","right_leg",[0,23,0], [cube([-2.9,22,-1.8],[2.8,4,3.6],(48,48))])]
    elif e == "nightmare_hound":
        a += [
          part(p+"upper_jaw_plate","head",[0,-4,-3], [cube([-4.9,-5,-6.5],[9.8,2.2,3.4],(0,48))]),
          part(p+"fangs_upper","head",[0,-1,-4], [cube([-3.3,-1.4,-7.2],[1,2.4,1],(12,48)),cube([2.3,-1.4,-7.2],[1,2.4,1],(12,48)),cube([-.5,-1.4,-7.5],[1,2.7,1],(12,48))]),
          part(p+"fangs_lower","jaw",[0,.6,-2], [cube([-2.7,.4,-4.1],[.9,2,1],(24,48)),cube([1.8,.4,-4.1],[.9,2,1],(24,48))]),
          part(p+"cheek_muscle_l","head",[-3,-2,-2], [cube([-4.8,-3.2,-4.5],[1.7,3.2,2.4],(32,48))]),
          part(p+"cheek_muscle_r","head",[3,-2,-2], [cube([3.1,-3.2,-4.5],[1.7,3.2,2.4],(32,48))]),
          part(p+"dewclaw_l","left_arm",[0,10,-1], [cube([10,9,-3.5],[1.4,2.5,1.4],(40,48))]),
          part(p+"dewclaw_r","right_arm",[0,10,-1], [cube([-11.4,9,-3.5],[1.4,2.5,1.4],(40,48))]),
          part(p+"haunch_l","left_leg",[0,12,0], [cube([.1,10,-2.8],[5,5,5.6],(0,56))]),
          part(p+"haunch_r","right_leg",[0,12,0], [cube([-5.1,10,-2.8],[5,5,5.6],(0,56))]),
          part(p+"tail_spines","tail",[0,5,0], [cube([-1.4,5,11],[2.8,2,2],(32,56)),cube([-1.1,5,14],[2.2,1.8,2],(40,56))])]
    elif e == "cave_jar":
        a += [
          part(p+"jar_rim","body",[0,1,0], [cube([-5.4,1,-3.9],[10.8,1.4,7.8],(0,48))]),
          part(p+"shell_bulge","body",[0,7,0], [cube([-5.8,7,-4.2],[11.6,7,8.4],(12,48))]),
          part(p+"inner_rib_l","body",[-3,5,-2], [cube([-4.2,4,-3.8],[1,8,1.2],(24,48)),cube([-3.8,10,-3.8],[1,4,1.2],(24,48))]),
          part(p+"inner_rib_r","body",[3,5,-2], [cube([3.2,4,-3.8],[1,8,1.2],(24,48)),cube([2.8,10,-3.8],[1,4,1.2],(24,48))]),
          part(p+"mandible_l","head",[-2,-1,-2], [cube([-3.3,-1.5,-5.5],[1.1,2.8,2.4],(32,48))]),
          part(p+"mandible_r","head",[2,-1,-2], [cube([2.2,-1.5,-5.5],[1.1,2.8,2.4],(32,48))]),
          part(p+"hooked_tips_l","left_arm",[0,18,-1], [cube([7.2,17,-3.5],[.9,3,1],(40,48)),cube([8,17,-3.5],[.9,2.2,1],(40,48))]),
          part(p+"hooked_tips_r","right_arm",[0,18,-1], [cube([-8.1,17,-3.5],[.9,3,1],(40,48)),cube([-9,17,-3.5],[.9,2.2,1],(40,48))])]
    elif e == "ceiling_dweller":
        a += [
          part(p+"abdomen_sac","body",[0,2,1], [cube([-4.6,2,-1],[9.2,4,8],(0,48))]),
          part(p+"mouth_hooks","jaw",[0,.5,-1], [cube([-2.8,.2,-4.2],[.8,2.4,1],(12,48)),cube([2,.2,-4.2],[.8,2.4,1],(12,48))]),
          part(p+"eye_cluster_l","head",[-2,-3,-2], [cube([-3.4,-4,-4.7],[1.4,1.5,.6],(24,48)),cube([-2.4,-5,-4.7],[1.1,1,.6],(24,48))]),
          part(p+"eye_cluster_r","head",[2,-3,-2], [cube([2,-4,-4.7],[1.4,1.5,.6],(24,48)),cube([1.3,-5,-4.7],[1.1,1,.6],(24,48))]),
          part(p+"adhesive_pad_l","left_arm",[0,15,-1], [cube([7.2,14,-3],[2.2,2.2,6],(32,48))]),
          part(p+"adhesive_pad_r","right_arm",[0,15,-1], [cube([-9.4,14,-3],[2.2,2.2,6],(32,48))]),
          part(p+"hooked_tarsus_0","extra_leg_0",[0,3,0], [cube([-19,2,-4],[2,2,2],(40,48))]),
          part(p+"hooked_tarsus_1","extra_leg_1",[0,3,0], [cube([17,2,-4],[2,2,2],(40,48))]),
          part(p+"hooked_tarsus_2","extra_leg_2",[0,3,0], [cube([-19,2,1],[2,2,2],(40,48))]),
          part(p+"hooked_tarsus_3","extra_leg_3",[0,3,0], [cube([17,2,1],[2,2,2],(40,48))])]
    elif e == "mimic":
        a += [
          part(p+"split_face_l","head",[-1,-3,-2], [cube([-2.8,-4,-3.3],[1.8,3.4,1.2],(0,48))]),
          part(p+"split_face_r","head",[1,-3,-2], [cube([1,-4,-3.3],[1.8,3.4,1.2],(12,48))]),
          part(p+"inner_teeth","jaw",[0,.4,-1], [cube([-2.1,.2,-3.6],[.6,1.5,.7],(24,48)),cube([-1,.2,-3.8],[.6,1.5,.7],(24,48)),cube([.1,.2,-3.8],[.6,1.5,.7],(24,48)),cube([1.2,.2,-3.6],[.6,1.5,.7],(24,48))]),
          part(p+"false_rib_cage","body",[0,5,-1], [cube([-3.4,4,-3.1],[1,6,1],(32,48)),cube([2.4,4,-3.1],[1,6,1],(32,48))]),
          part(p+"wrist_tendrils_l","left_arm",[0,14,-1], [cube([6.8,13,-2.4],[.6,4,.6],(40,48)),cube([7.7,13,-2.4],[.6,3,.6],(40,48))]),
          part(p+"wrist_tendrils_r","right_arm",[0,14,-1], [cube([-7.4,13,-2.4],[.6,4,.6],(40,48)),cube([-8.3,13,-2.4],[.6,3,.6],(40,48))]),
          part(p+"heel_spurs_l","left_leg",[0,24,0], [cube([.2,23,1.4],[2.5,3,1.4],(48,48))]),
          part(p+"heel_spurs_r","right_leg",[0,24,0], [cube([-2.7,23,1.4],[2.5,3,1.4],(48,48))])]
    elif e == "death":
        a += [
          part(p+"hood_shadow","head",[0,-6,0], [cube([-4.4,-7,-3.8],[8.8,2,7.6],(0,48))]),
          part(p+"nasal_cavity","head",[0,-2,-3], [cube([-.65,-3,-4.5],[1.3,2.2,.8],(12,48))]),
          part(p+"jaw_teeth","jaw",[0,.5,-1], [cube([-2.6,.3,-3.5],[.7,1.7,.7],(24,48)),cube([-1.1,.3,-3.7],[.7,1.7,.7],(24,48)),cube([.4,.3,-3.7],[.7,1.7,.7],(24,48)),cube([1.9,.3,-3.5],[.7,1.7,.7],(24,48))]),
          part(p+"rib_arch_l","body",[-2,6,-1], [cube([-4.8,5,-3.6],[1.2,8,1],(32,48)),cube([-3.8,5,-3.6],[1.2,8,1],(32,48))]),
          part(p+"rib_arch_r","body",[2,6,-1], [cube([2.6,5,-3.6],[1.2,8,1],(32,48)),cube([3.6,5,-3.6],[1.2,8,1],(32,48))]),
          part(p+"bone_sickle_l","left_arm",[0,18,-1], [cube([6.4,17,-3],[1.1,4,1.1],(40,48)),cube([7.2,19,-3],[1,1.2,1.1],(40,48))]),
          part(p+"bone_sickle_r","right_arm",[0,18,-1], [cube([-7.5,17,-3],[1.1,4,1.1],(40,48)),cube([-8.3,19,-3],[1,1.2,1.1],(40,48))]),
          part(p+"robe_tails","body",[0,13,1], [cube([-5.5,13,1],[2,7,1.5],(48,48)),cube([3.5,13,1],[2,7,1.5],(48,48))])]
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
                if e in ("stalker","hunter","lurker","observer","stillborn_statue","nightmare_false_survivor","pilgrim","agitated_one"):
                    if action=="idle": amp=8 if ("eye" in n or "lens" in n) else 2.2 if ("tendon" in n or "tendril" in n or "cable" in n or "cord" in n) else 1.8 if ("mantle" in n or "hood" in n) else .7
                    elif action=="walk": amp=8 if ("claw" in n or "feeler" in n or "finger" in n or "hook" in n or "spur" in n) else 5 if ("mantle" in n or "hood" in n or "cord" in n) else 2
                    elif action=="sprint": amp=14 if ("claw" in n or "finger" in n or "hook" in n or "spur" in n) else 8 if ("mantle" in n or "hood" in n or "cord" in n) else 5
                    elif action=="attack": amp=38 if ("jaw" in n or "fang" in n or "mandible" in n or "hook" in n or "claw" in n) else 24
                    else: amp=24 if ("tendon" in n or "tendril" in n or "cable" in n or "cord" in n) else 12
                elif action=="idle": amp=1.4 if ("tendon" in n or "tendril" in n) else .7
                elif action=="walk": amp=5 if ("claw" in n or "feeler" in n) else 2
                elif action=="sprint": amp=11 if ("claw" in n or "mandible" in n) else 5
                elif action=="attack": amp=34 if ("jaw" in n or "fang" in n or "mandible" in n) else 24
                else: amp=20 if ("tendon" in n or "tendril" in n) else 12
                if action in ("idle","walk","sprint"):
                    vals=([ -amp,0,-amp*.25],[amp,0,amp*.25],[-amp,0,-amp*.25])
                elif action=="attack": vals=([0,0,0],[-amp,0,amp*.25],[amp*.3,0,-amp*.1])
                else: vals=([0,0,0],[amp,0,amp*.2],[amp*1.6,0,amp*.35])
                if "eye" in n or "lens" in n:
                    if action=="idle": vals=([0,-14,0],[0,16,0],[0,-14,0])
                    elif action in ("walk","sprint"): vals=([0,-8,0],[0,10,0],[0,-8,0])
                    elif action=="attack": vals=([0,0,0],[0,22,0],[0,-12,0])
                clip.setdefault("bones",{})[b["name"]]={"rotation":{"0.0":vals[0],str(mid):vals[1],str(duration):vals[2]}}
    reflect_animations(anim)
    dest=OUT/"animations/entity/outlands_monster.animation.json"
    dest.parent.mkdir(parents=True,exist_ok=True)
    dest.write_text(json.dumps({"format_version":"1.8.0","animations":anim},indent=2)+"\n",encoding="utf-8")
    print("Generated unique anatomy for 24 creatures and articulated all 120 creature-specific clips.")

if __name__=="__main__": main()
