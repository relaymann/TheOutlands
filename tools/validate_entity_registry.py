#!/usr/bin/env python3
"""Fail the build if any registered Outlands entity is missing a survival system."""
import glob, json, os, re, sys

ROOT = "src/main"
def read(path):
    with open(os.path.join(ROOT, path), encoding="utf-8") as f: return f.read()

registry = read("java/com/relaymann/theoutlands/entity/OutlandsEntities.java")
placements = read("java/com/relaymann/theoutlands/entity/OutlandsSpawnEvents.java")
attributes = read("java/com/relaymann/theoutlands/entity/OutlandsEntityEvents.java")
renderers = read("java/com/relaymann/theoutlands/client/OutlandsClientEvents.java")
sounds_java = read("java/com/relaymann/theoutlands/sound/OutlandsSounds.java")
sound_data = json.load(open(os.path.join(ROOT,"resources/assets/theoutlands/sounds.json"),encoding="utf-8"))
audio_generator = open("tools/generate_entity_audio.py",encoding="utf-8").read()

fields = dict((field, entity_id) for field,entity_id in re.findall(
    r"([A-Z][A-Z0-9_]*)\s*=\s*ENTITY_TYPES\.register\(\"([^\"]+)\"", registry))
if len(fields) != 16:
    raise SystemExit("Expected 16 registered Outlands entities; found %d: %s" % (len(fields), fields))
spawn_fields = set(re.findall(r"registerGround\(event,\s*OutlandsEntities\.([A-Z0-9_]+)\.get\(\)", placements))
attribute_fields = set(re.findall(r"event\.put\(OutlandsEntities\.([A-Z0-9_]+)\.get\(\)", attributes))
renderer_fields = set(re.findall(r"registerEntityRenderer\(OutlandsEntities\.([A-Z0-9_]+)\.get\(\)", renderers))

biome_ids=set()
for path in glob.glob(os.path.join(ROOT,"resources/data/theoutlands/neoforge/biome_modifier/*.json")):
    data=json.load(open(path,encoding="utf-8"))
    for entry in data.get("spawners",[]):
        if entry["type"].startswith("theoutlands:"): biome_ids.add(entry["type"].split(":",1)[1])

sound_map = {
 "null":"null_presence", "fear":"fear_presence", "death":"death_presence",
 "almighty":"almighty_presence", "null_apostle":"null_apostle_whisper",
 "cave_jar":"cavejar_stalk", "stalker":"stalker_breath", "observer":"observer_click",
 "hunter":"hunter_stalk", "burrower":"burrower_rise", "mimic":"mimic_reveal",
 "stillborn_statue":"stillborn_stone", "agitated_one":"agitated_warning",
 "lurker":"lurker_breath", "howler":"howler_call", "dredge":"dredge_crawl"
}
model_map = {
 "null":"NullModel","fear":"FearModel","death":"DeathModel","almighty":"AlmightyModel",
 "null_apostle":"NullApostleModel","cave_jar":"CaveJarModel","stalker":"StalkerModel",
 "observer":"ObserverModel","hunter":"HunterModel","burrower":"BurrowerModel","mimic":"MimicModel",
 "stillborn_statue":"StillbornStatueModel","agitated_one":"AgitatedOneModel",
 "lurker":"LurkerModel","howler":"HowlerModel","dredge":"DredgeModel"
}
texture_map = {}
for field,entity_id in fields.items():
    renderer_match=re.search(r"registerEntityRenderer\(OutlandsEntities\."+field+r"\.get\(\),\s*([A-Za-z0-9_]+)::new\)",renderers)
    if not renderer_match: raise SystemExit("Missing renderer registration: "+entity_id)
    renderer_src=read("java/com/relaymann/theoutlands/client/"+renderer_match.group(1)+".java")
    tex=re.search(r'textures/entity/([^\"]+\.png)',renderer_src)
    if not tex: raise SystemExit("Missing texture path: "+entity_id)
    texture_map[entity_id]=tex.group(1)

errors=[]
for field,entity_id in fields.items():
    if field not in spawn_fields: errors.append(entity_id+" missing spawn placement")
    if field not in attribute_fields: errors.append(entity_id+" missing attributes")
    if field not in renderer_fields: errors.append(entity_id+" missing renderer registration")
    if entity_id not in biome_ids: errors.append(entity_id+" missing natural biome spawn entry")
    model=model_map[entity_id]
    model_path="java/com/relaymann/theoutlands/client/"+model+".java"
    if not os.path.exists(os.path.join(ROOT,model_path)): errors.append(entity_id+" missing model "+model)
    elif "setupAnim" not in read(model_path): errors.append(entity_id+" missing custom animation")
    texture=texture_map.get(entity_id)
    if texture and not os.path.exists(os.path.join(ROOT,"resources/assets/theoutlands/textures/entity",texture)):
        errors.append(entity_id+" missing texture "+texture)
    event=sound_map[entity_id]
    if event not in sounds_java: errors.append(entity_id+" missing registered sound event")
    if event not in sound_data: errors.append(entity_id+" missing sounds.json event")
    sound_file=event+".ogg"
    generated=('make("%s"' % event) in audio_generator
    committed=os.path.exists(os.path.join(ROOT,"resources/assets/theoutlands/sounds",sound_file))
    if not generated and not committed: errors.append(entity_id+" missing generated/committed audio "+sound_file)

if len(set(texture_map.values())) != len(fields):
    errors.append("Entity renderers do not all use unique texture files")
if errors:
    print("\n".join("ERROR: "+e for e in errors),file=sys.stderr)
    raise SystemExit(1)
print("Entity audit passed: %d/%d entities have natural biome spawns, spawn placements, attributes, renderers, animated models, unique textures and dedicated sound events." % (len(fields),len(fields)))
