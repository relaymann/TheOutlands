# Development Plan

## Phase 0 — Foundation
- [x] NeoForge 1.21.1 / Java 21 / Gradle project and metadata.
- [x] Unbounded distance progression from original shared Overworld spawn.
- [x] Server-side progression evaluation once per second.

## Phase 1 — Horror director and psychological deception
- [x] Per-player pressure, quiet periods, cooldowns, weighted selection, and recent-event memory.
- [x] Context inputs: biome, day/night, weather, nearby players, health, and player behavior.
- [x] Executable player-local events for ambient displacement, false footsteps, figures, environmental distortion, and story interventions.
- [x] Client-only terrain afterimages, silhouettes, and false structure afterimages with server-state restoration.
- [x] Persistent haunted-place memory and escalating return anomalies.
- [x] Player-specific hallucinations and sounds that can differ between players sharing a location.
- [x] No routine chat/actionbar narration for ordinary scares.
- [x] Operator debug command and configurable pacing.

## Phase 2 — Null and his apostles
- [x] Dedicated Null entity, custom texture, and watcher behavior.
- [x] Safe hiding-position selection in already-loaded chunks.
- [x] Eye-contact and close-approach disappearance; relocation while unobserved.
- [x] Persistent Null observation memory that influences later stalking profiles.
- [x] Bounded memory of player travel, sleeping, and block-placement areas to bias future watcher positions.
- [x] Rare unnamed Null apostle observer entities with their own renderer and player-specific lifecycle.
- [ ] Distinct named apostles, individual roles, bespoke models, and story structures.
- [ ] Full building-aware stalking and coordinated multiplayer apostle encounters.

## Phase 3 — Fear's territory
- [x] Fear first-meeting gate, persistent discovery, and dedicated manifestation renderer.
- [x] Fear's full-territory forest overlay: strange mob attention, animal behavior, ash, positional sounds, and false structures.
- [x] Client-only forest fog tint and localized ambient/weather attenuation.
- [x] Registered `theoutlands:fear_forest` biome data, biome tag, vegetation modifier, and hostile spawn modifier.
- [ ] Integrate Fear biome into the vanilla Overworld biome source without replacing vanilla biome distribution or breaking biome mods.
- [ ] Permanent Fear-exclusive structures, custom vegetation blocks, and custom Fear creatures.
- [ ] Bespoke Fear animation set and original audio recordings.

## Phase 4 — Death and the ARG
- [x] Persistent Death hunter bound to the player whose trail he picked up.
- [x] Stalking pauses, apparent escape window, and bounded resume behavior.
- [x] Persisted target UUID and hunt state.
- [x] Three-stage clue sequence: `death.jar`, `manifest.txt`, and `author.dat`.
- [x] Final `the_last_page` artifact from The Almighty encounter.
- [x] Original 64x64 Death texture.
- [ ] Permanent abandoned settlements, laboratories, and Death structures.
- [ ] In-world file-reading interfaces, encrypted records, hidden coordinates, and multi-step puzzles.
- [ ] Bespoke Death geometry and authored animations.

## Phase 5 — The Almighty and endgame
- [x] Configured-border-relative exclusive territory and entity exclusion.
- [x] One-time Almighty encounter and server boss bar.
- [x] Three escalating combat phases with distinct area attacks and phase transitions.
- [x] Original 64x64 Almighty texture and enlarged silhouette.
- [x] Ending discovery and final artifact drop.
- [ ] Bespoke boss rig, animations, arena/environment sequence, and cinematic finale.
- [ ] Full epilogue, alternate outcomes, and multiplayer ending synchronization.

## Phase 6 — Release and compatibility
- [x] Rolling direct-download GitHub Release with checksum.
- [x] CI build and unit-test gate before publishing.
- [ ] Dedicated-server startup smoke test.
- [ ] Client rendering/audio test on Minecraft 1.21.1 NeoForge 21.1.266.
- [ ] Multiplayer test for private hallucinations, shared story beats, and disconnect/respawn cleanup.
- [ ] Performance and worldgen compatibility audit.
- [ ] Full regression pass on a copied survival world.

## Explicit exclusions
- No spawn enclosure or progressive mazes from the separate Under The Bedrock project.
- No destructive rewriting of player-built blocks.
- No ordinary scare narration in chat or actionbar.
- No arbitrary cap on horror escalation before the configured world border.
