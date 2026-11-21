# Outlands entity catalogue and natural-spawn audit

All 16 registered Outlands entity types have survival spawn entries. They are not dependent on commands, spawn eggs, scripted cutscenes, or debug tools.

The data-driven biome modifiers add the entities to the normal Minecraft natural-spawn lists. `OutlandsSpawnEvents` registers a matching ground-spawn predicate for every entity, and `OutlandsEntityEvents` registers attributes for every entity. The client renderer registry includes an entity-specific animated model and a unique texture path for each. The sound registry and audio generator provide at least one dedicated sound event for every entity.

| Entity | Natural habitat and spawn condition | Role / behavior |
|---|---|---|
| Null | Overworld forests; normal monster spawn checks | Silent watcher that fixes its gaze on nearby players |
| Fear | Overworld surface; normal monster spawn checks; also listed in Fear Forest | Motionless hovering manifestation with a drifting veil |
| Death | Overworld caves, Y 24 or lower, block light 4 or lower | Persistent player-bound hunter that pauses and resumes its pursuit |
| The Almighty | End biomes; normal monster checks plus a very rare additional roll | Endgame presence with three escalating combat phases |
| Null Apostle | Overworld forests; normal monster spawn checks | Null's subordinate; slowly shadows nearby players and watches them |
| Cave.jar | Dark Overworld caves, Y 48 or lower, block light 7 or lower | Fast cave ambush predator with a lunging attack |
| Stalker | Overworld surface; normal monster spawn checks | Persistent hunter that follows, repositions, and pounces |
| Observer | Overworld surface; normal monster spawn checks | Patient watcher that advances when unseen and studies active predators |
| Hunter | Overworld surface; normal monster spawn checks | Pursuit predator with memory and interception behavior |
| Burrower | Dark Overworld caves, Y 48 or lower, block light 7 or lower | Subterranean predator; movement does not dig or alter blocks |
| Mimic | Overworld surface; normal monster spawn checks | Disguises itself as a block and reveals itself to attack |
| Stillborn Statue | Overworld caves, Y 64 or lower, block light 5 or lower | Freezes under direct observation and advances when unseen |
| Agitated One | Overworld surface; normal monster spawn checks | Territorial creature with a readable windup and charge |
| Lurker | Forest biomes, block light 9 or lower | Ambush predator that watches, lunges, and yields to dominant threats |
| Howler | Overworld surface; normal monster spawn checks | Territorial caller that can rally nearby Hunters and Stalkers |
| Dredge | Overworld, Y 80 or lower, block light 8 or lower | Low-profile tracker that follows last-known player positions and lunges |

## Asset and build pipeline

- `tools/generate_entity_textures.py` deterministically creates 16 separate 64x64 pixel-art skins using only the Python standard library.
- `tools/generate_entity_audio.py` creates the original entity sound effects as OGG files with FFmpeg.
- `tools/validate_entity_registry.py` fails CI if a registered entity is missing a natural biome spawn entry, spawn placement, attributes, renderer, animated model, unique texture, or dedicated sound event.
- GitHub Actions generates the texture/audio resources and runs the audit before Gradle tests and packaging.

Natural spawn weights remain deliberately different: common threats such as the Lurker, Howler, and Dredge have higher weights; story presences and major threats remain rare. Spawn habitats and the normal monster light/placement checks remain in effect.

No terrain blocks are placed, removed, or modified by the intelligence, relationship, or movement systems.
