#!/usr/bin/env python3
"""Run fifteen independent integrity passes over all registered Outlands entities."""
import glob, json, os, re, struct, sys

ROOT = "src/main"
EXPECTED = 19
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
 "lurker":"lurker_breath", "howler":"howler_call", "dredge":"dredge_crawl", "pilgrim":"pilgrim_breath", "ceiling_dweller":"ceiling_crawl", "colossus":"colossus_step"
}
model_map = {
 "null":"NullModel", "fear":"FearModel", "death":"DeathModel", "almighty":"AlmightyModel",
 "null_apostle":"NullApostleModel", "cave_jar":"CaveJarModel", "stalker":"StalkerModel",
 "observer":"ObserverModel", "hunter":"HunterModel", "burrower":"BurrowerModel",
 "mimic":"MimicModel", "stillborn_statue":"StillbornStatueModel",
 "agitated_one":"AgitatedOneModel", "lurker":"LurkerModel",
 "howler":"HowlerModel", "dredge":"DredgeModel", "pilgrim":"PilgrimModel", "ceiling_dweller":"CeilingDwellerModel", "colossus":"ColossusModel"
}

renderer_models = {}
textures = {}
layer_fields = set(re.findall(r'event\.registerLayerDefinition\(([A-Za-z0-9_]+)Renderer\.LAYER', renderers))
for field, entity_id in fields.items():
    match = re.search(r'registerEntityRenderer\(OutlandsEntities\.' + field + r'\.get\(\),\s*([A-Za-z0-9_]+)::new\)', renderers)
    if not match:
        continue
    renderer_name = match.group(1)
    renderer_path = "java/com/relaymann/theoutlands/client/" + renderer_name + ".java"
    if not os.path.isfile(os.path.join(ROOT, renderer_path)):
        renderer_path = "java/com/relaymann/theoutlands/client/NewEntityRenderers.java"
    src = read(renderer_path)
    renderer_block_match = re.search(
        r'class\s+' + re.escape(renderer_name) + r'\b.*?(?=\nfinal class|\Z)',
        src, re.S)
    renderer_block = renderer_block_match.group(0) if renderer_block_match else src
    model_match = re.search(r'new\s+([A-Za-z0-9_]+Model)\s*\(', renderer_block)
    texture_match = re.search(r'textures/entity/([^"]+\.png)', renderer_block)
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
        if not model:
            errors.append(entity_id + ": missing model")
        else:
            model_source = read(model_path) if os.path.isfile(os.path.join(ROOT, model_path)) else read("java/com/relaymann/theoutlands/client/NewEntityRenderers.java")
            if ("class " + str(model)) not in model_source:
                errors.append(entity_id + ": missing model class")
            elif "setupAnim" not in model_source:
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
            elif struct.unpack(">II", header[16:24]) != ((128, 128) if entity_id == "colossus" else (64, 128) if entity_id == "pilgrim" else (64, 64)):
                errors.append(entity_id + ": texture dimensions do not match model UV atlas")

        sound = sound_map.get(entity_id)
        if not sound or sound not in registered_sounds: errors.append(entity_id + ": unregistered sound")
        if sound and sound not in sound_data: errors.append(entity_id + ": missing sounds.json entry")
        if sound:
            generated = ('make("%s"' % sound) in audio_generator
            committed = os.path.isfile(os.path.join(ROOT, "resources/assets/theoutlands/sounds", sound + ".ogg"))
            if not generated and not committed: errors.append(entity_id + ": no generated/committed OGG")

    expected_weights = {
        "stalker": 70, "lurker": 75, "dredge": 70, "hunter": 48,
        "observer": 38, "mimic": 32, "howler": 40, "pilgrim": 24,
        "cave_jar": 36, "burrower": 42, "ceiling_dweller": 28,
        "stillborn_statue": 5, "agitated_one": 5, "null": 6, "null_apostle": 6
    }
    for entity_id, expected_weight in expected_weights.items():
        entries = spawn_entries.get(entity_id, [])
        if not any(entry.get("weight") == expected_weight for _, entry, _ in entries):
            errors.append(entity_id + ": natural spawn weight does not match balanced pacing target")

    if len(set(textures.values())) != EXPECTED: errors.append("entity texture paths are not unique")
    new_models = read("java/com/relaymann/theoutlands/client/NewEntityRenderers.java")
    if "LayerDefinition.create(m,64,128)" not in new_models or "LayerDefinition.create(m,128,128)" not in new_models:
        errors.append("Pilgrim/Colossus UV atlas dimensions are not registered")
    for renderer in ("PilgrimRenderer", "CeilingDwellerRenderer", "ColossusRenderer"):
        if renderer not in renderers:
            errors.append(renderer + ": missing renderer registration")
    if "COLOSSUS_WARNING" not in sounds_java or "COLOSSUS_STOMP" not in sounds_java:
        errors.append("Colossus telegraph/impact sounds are not registered")
    if "CEILING_DROP" not in sounds_java or "PILGRIM_STEP" not in sounds_java:
        errors.append("new entity action sounds are not registered")
    if len(set(model_map.values())) != EXPECTED: errors.append("entity model mapping is not unique")
    if len(layer_fields) != EXPECTED: errors.append("not all entity model layers are registered")
    if "SoundEvents.WARDEN_ROAR" in almighty_encounter: errors.append("Almighty uses a vanilla placeholder sound")
    mimic_src = read("java/com/relaymann/theoutlands/entity/MimicEntity.java")
    stalker_src = read("java/com/relaymann/theoutlands/entity/StalkerEntity.java")
    if "if (isPassenger()) stopRiding();" not in stalker_src:
        errors.append("Stalker can be neutralized by vehicle trapping")
    if mimic_src.count("!p.isCreative()") < 2: errors.append("Mimic watcher checks do not ignore creative players")
    if intelligence.count("event.isCanceled()") < 4:
        errors.append("intelligence handlers do not consistently ignore canceled actions")

    config = read("java/com/relaymann/theoutlands/OutlandsConfig.java")
    required_spawn_rules = [
        "ENTITY_SPAWN_BALANCE_ENABLED", "new SpawnProfile(250.0D, 1, 1)",
        "new SpawnProfile(500.0D, 2, 1)", "new SpawnProfile(600.0D, 2, 1)",
        "new SpawnProfile(750.0D, 3, 1)", "new SpawnProfile(900.0D, 3, 2)",
        "new SpawnProfile(3_000.0D, 6, 5)", "new SpawnProfile(2_000.0D, 4, 3)",
        "new SpawnProfile(OutlandsConfig.FEAR_FIRST_APPEARANCE_DISTANCE.get(), 8, 6)",
        "new SpawnProfile(OutlandsConfig.DEATH_FIRST_APPEARANCE_DISTANCE.get(), 8, 10)",
        "int denominator = profile.rarityMultiplier()",
        "threatPoints + profile.threatCost() <= threatBudget",
        "private static final double ENCOUNTER_RADIUS = 72.0D",
        "distance < 1_000.0D ? 4", "distance < 2_000.0D ? 5",
        "distance < 4_000.0D ? 7", "distance < 7_000.0D ? 8 : 9"
    ]
    for rule in required_spawn_rules:
        if rule not in placements + config:
            errors.append("missing distance/threat spawn rule: " + rule)
    if "MobSpawnType.NATURAL" not in placements or "MobSpawnType.CHUNK_GENERATION" not in placements:
        errors.append("distance progression does not cover both natural spawn reasons")
    if "type == OutlandsEntities.ALMIGHTY.get()" not in placements:
        errors.append("Almighty's separate End spawn rule is not exempted")
    profile_section = placements.split("private static SpawnProfile profile", 1)[1].split("private record SpawnProfile", 1)[0]
    for field in fields:
        if field != "ALMIGHTY" and "OutlandsEntities." + field + ".get()" not in profile_section:
            errors.append(field.lower() + ": missing distance/threat profile")
    return errors

for pass_number in range(1, PASSES + 1):
    errors = audit_once()
    if errors:
        print("AUDIT PASS %d/%d FAILED" % (pass_number, PASSES), file=sys.stderr)
        print("\n".join("ERROR: " + e for e in errors), file=sys.stderr)
        raise SystemExit(1)
    print("Audit pass %d/%d passed (%d entities)." % (pass_number, PASSES, EXPECTED))

print("All 15 entity integrity audit passes passed.")
