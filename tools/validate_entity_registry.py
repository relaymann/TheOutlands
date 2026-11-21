#!/usr/bin/env python3
"""Run fifteen independent integrity passes over all registered Outlands entities."""
import glob, json, os, re, struct, sys

ROOT = "src/main"
EXPECTED = 16
PASSES = 15

def read(path):
    with open(os.path.join(ROOT, path), encoding="utf-8") as f:
        return f.read()

registry = read("java/com/relaymann/theoutlands/entity/OutlandsEntities.java")
placements = read("java/com/relaymann/theoutlands/entity/OutlandsSpawnEvents.java")
attributes = read("java/com/relaymann/theoutlands/entity/OutlandsEntityEvents.java")
renderers = read("java/com/relaymann/theoutlands/client/OutlandsClientEvents.java")
sounds_java = read("java/com/relaymann/theoutlands/sound/OutlandsSounds.java")
intelligence = read("java/com/relaymann/theoutlands/event/OutlandsIntelligenceManager.java")
almighty_encounter = read("java/com/relaymann/theoutlands/event/AlmightyEncounterManager.java")
sound_data = json.load(open(os.path.join(ROOT, "resources/assets/theoutlands/sounds.json"), encoding="utf-8"))
audio_generator = open("tools/generate_entity_audio.py", encoding="utf-8").read()

fields = dict((field, entity_id) for field, entity_id in re.findall(
    r'([A-Z][A-Z0-9_]*)\s*=\s*ENTITY_TYPES\.register\("([^"]+)"', registry))
if len(fields) != EXPECTED:
    raise SystemExit("Expected %d registered entities; found %d: %s" % (EXPECTED, len(fields), fields))

spawn_fields = set(re.findall(r'registerGround\(event,\s*OutlandsEntities\.([A-Z0-9_]+)\.get\(\)', placements))
attribute_fields = set(re.findall(r'event\.put\(OutlandsEntities\.([A-Z0-9_]+)\.get\(\)', attributes))
renderer_fields = set(re.findall(r'registerEntityRenderer\(OutlandsEntities\.([A-Z0-9_]+)\.get\(\)', renderers))
registered_sounds = set(re.findall(r'register\("([^"]+)"\)', sounds_java))

spawn_entries = {}
for path in glob.glob(os.path.join(ROOT, "resources/data/theoutlands/neoforge/biome_modifier/*.json")):
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    for entry in data.get("spawners", []):
        if entry.get("type", "").startswith("theoutlands:"):
            entity_id = entry["type"].split(":", 1)[1]
            spawn_entries.setdefault(entity_id, []).append((path, entry, data.get("biomes")))

sound_map = {
 "null":"null_presence", "fear":"fear_presence", "death":"death_presence",
 "almighty":"almighty_presence", "null_apostle":"null_apostle_whisper",
 "cave_jar":"cavejar_stalk", "stalker":"stalker_breath", "observer":"observer_click",
 "hunter":"hunter_stalk", "burrower":"burrower_rise", "mimic":"mimic_reveal",
 "stillborn_statue":"stillborn_stone", "agitated_one":"agitated_warning",
 "lurker":"lurker_breath", "howler":"howler_call", "dredge":"dredge_crawl"
}
model_map = {
 "null":"NullModel", "fear":"FearModel", "death":"DeathModel", "almighty":"AlmightyModel",
 "null_apostle":"NullApostleModel", "cave_jar":"CaveJarModel", "stalker":"StalkerModel",
 "observer":"ObserverModel", "hunter":"HunterModel", "burrower":"BurrowerModel",
 "mimic":"MimicModel", "stillborn_statue":"StillbornStatueModel",
 "agitated_one":"AgitatedOneModel", "lurker":"LurkerModel",
 "howler":"HowlerModel", "dredge":"DredgeModel"
}

renderer_models = {}
textures = {}
layer_fields = set(re.findall(r'event\.registerLayerDefinition\(([A-Za-z0-9_]+)Renderer\.LAYER', renderers))
for field, entity_id in fields.items():
    match = re.search(r'registerEntityRenderer\(OutlandsEntities\.' + field + r'\.get\(\),\s*([A-Za-z0-9_]+)::new\)', renderers)
    if not match:
        continue
    renderer_name = match.group(1)
    src = read("java/com/relaymann/theoutlands/client/" + renderer_name + ".java")
    model_match = re.search(r'new\s+([A-Za-z0-9_]+)\s*\(', src)
    texture_match = re.search(r'textures/entity/([^"]+\.png)', src)
    if model_match:
        renderer_models[entity_id] = model_match.group(1)
    if texture_match:
        textures[entity_id] = texture_match.group(1)

def audit_once():
    errors = []
    for field, entity_id in fields.items():
        if field not in spawn_fields: errors.append(entity_id + ": no spawn placement")
        if field not in attribute_fields: errors.append(entity_id + ": no attributes")
        if field not in renderer_fields: errors.append(entity_id + ": no renderer")
        entries = spawn_entries.get(entity_id, [])
        if not entries: errors.append(entity_id + ": no natural biome spawn")
        for path, entry, biomes in entries:
            if not isinstance(entry.get("weight"), int) or entry["weight"] <= 0:
                errors.append(entity_id + ": invalid spawn weight in " + path)
            low, high = entry.get("minCount"), entry.get("maxCount")
            if not isinstance(low, int) or not isinstance(high, int) or low < 1 or high < low:
                errors.append(entity_id + ": invalid spawn group size in " + path)
            if not biomes: errors.append(entity_id + ": missing biome selector in " + path)

        model = model_map.get(entity_id)
        model_path = "java/com/relaymann/theoutlands/client/" + str(model) + ".java"
        if not model or not os.path.isfile(os.path.join(ROOT, model_path)):
            errors.append(entity_id + ": missing model")
        elif "setupAnim" not in read(model_path):
            errors.append(entity_id + ": missing animation method")
        if renderer_models.get(entity_id) != model:
            errors.append(entity_id + ": renderer/model mismatch")

        texture = textures.get(entity_id)
        texture_path = os.path.join(ROOT, "resources/assets/theoutlands/textures/entity", texture or "")
        if not texture or not os.path.isfile(texture_path):
            errors.append(entity_id + ": missing texture")
        else:
            with open(texture_path, "rb") as image:
                header = image.read(24)
            if len(header) != 24 or header[:8] != b"\x89PNG\r\n\x1a\n":
                errors.append(entity_id + ": invalid PNG texture")
            elif struct.unpack(">II", header[16:24]) != (64, 64):
                errors.append(entity_id + ": texture is not 64x64")

        sound = sound_map.get(entity_id)
        if not sound or sound not in registered_sounds: errors.append(entity_id + ": unregistered sound")
        if sound and sound not in sound_data: errors.append(entity_id + ": missing sounds.json entry")
        if sound:
            generated = ('make("%s"' % sound) in audio_generator
            committed = os.path.isfile(os.path.join(ROOT, "resources/assets/theoutlands/sounds", sound + ".ogg"))
            if not generated and not committed: errors.append(entity_id + ": no generated/committed OGG")

    if len(set(textures.values())) != EXPECTED: errors.append("entity texture paths are not unique")
    if len(set(model_map.values())) != EXPECTED: errors.append("entity model mapping is not unique")
    if len(layer_fields) != EXPECTED: errors.append("not all entity model layers are registered")
    if "SoundEvents.WARDEN_ROAR" in almighty_encounter: errors.append("Almighty uses a vanilla placeholder sound")
    mimic_src = read("java/com/relaymann/theoutlands/entity/MimicEntity.java")
    if mimic_src.count("!p.isCreative()") < 2: errors.append("Mimic watcher checks do not ignore creative players")
    if intelligence.count("event.isCanceled()") < 4:
        errors.append("intelligence handlers do not consistently ignore canceled actions")
    return errors

for pass_number in range(1, PASSES + 1):
    errors = audit_once()
    if errors:
        print("AUDIT PASS %d/%d FAILED" % (pass_number, PASSES), file=sys.stderr)
        print("\n".join("ERROR: " + e for e in errors), file=sys.stderr)
        raise SystemExit(1)
    print("Audit pass %d/%d passed (%d entities)." % (pass_number, PASSES, EXPECTED))

print("All 15 entity integrity audit passes passed.")
