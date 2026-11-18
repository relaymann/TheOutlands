# Development Plan

## Phase 0 — Foundation
- [x] NeoForge 1.21.1 / Java 21 / Gradle project and metadata.
- [x] Unbounded distance progression from original shared Overworld spawn.
- [x] Server-side progression evaluation once per second.

## Phase 1 — Horror director and first encounters
- [x] Per-player pressure, quiet periods, cooldowns, weighted selection, and recent-event memory.
- [x] Context inputs: biome, day/night, weather, nearby players, and health.
- [x] Executable server-side encounters for the six original event families.
- [x] Additional private unexplained-message, false-death, and returning-observer encounters.
- [x] Player-targeted sound, particles, and messages.
- [x] Operator debug command.
- [x] Configurable horror intensity, event frequency, and event enablement.
- [x] Persistent player encounter counts, recent history, and discovery storage API.
- [x] Player history influences event selection.
- [x] Expand the director catalog to 21 event families (including three Fear-specific post-introduction families) with unique gates, cooldowns, context weighting, and bounded player-local effects.
- [x] All 18 director IDs have explicit server-side handlers, and tests assert the catalog and handler sets match.
- [ ] Finish true client-side audio muting, terrain afterimages, fake-player rendering, and world-generation event variants (current cues are deliberately non-destructive approximations).

## Phase 2 — Null
- [x] Dedicated registered Null entity with a player-proportioned humanoid renderer.
- [x] Temporary observer sightings with bounded lifetime and eye-contact disappearance.
- [x] Persistent first-sighting discovery flag shared by all Null watcher event variants.\n- [x] Persistent first-sighting discovery flag shared by all Null watcher event variants.
- [x] Safe hiding-position selection in already-loaded chunks.
- [x] Close-approach disappearance and periodic relocation while unobserved.
- [x] Bounded stalking adaptation based on previous Null sightings.
- [x] Long-range observer placements (34–46 blocks initially), cover-aware placement in already-loaded terrain, and bounded closer/more persistent adaptation.
- [ ] False sightings, building-aware placement, and coordinated multiplayer encounters.
- [ ] Custom animations, persistent story state, and Null's apostles.

## Phase 3 — Territory and apostles
- [x] Pure geometry for the Almighty's final 5,000-block region using the live configured border center and size.
- [x] Unit tests for shifted centers, non-default sizes, sides, corners, and boundaries.
- [x] Server-side entity exclusion from the Almighty territory (players and the tagged Almighty are exempt).
- [x] Fear first-meeting gate, persistent one-time discovery, registered manifestation entity, dedicated renderer/texture, and timed cleanup.
- [x] Fear-specific post-introduction encounter gates and three dedicated event families (whisper, presence, call) with player-local effects.\n- [ ] Fear forest biome, structures, ambience, full territory behavior, and fully developed exclusive events.
- [ ] Death hunter AI, territory, structures, and death.jar clue chain.
- [ ] Almighty encounter and endgame.

## Phase 4 — World and ARG
- [ ] Spawn enclosure and three progressive mazes.
- [ ] Abandoned settlements, underground facilities, apostle structures, and landmarks.
- [ ] World corruption and reality anomalies.
- [ ] Persistent ARG discoveries, encrypted records, coordinate puzzles, and lore fragments.

## Phase 5 — Advanced presentation and multiplayer
- [ ] Custom models, textures, rigs, and smooth encounter animations.
- [ ] Dynamic original audio and territory ambience.
- [ ] Player-specific perceptions, shared events, coordinated encounters, and persistence.
- [x] Configurable intensity, event rates, and progression milestones.
- [x] Configurable Null sightings, horror audio, and Almighty territory depth.
- [ ] Performance, compatibility, and release audits.

## Release gates
- [ ] Client and dedicated-server startup smoke tests.
- [ ] Multiplayer synchronization and player-specific event tests.
- [ ] Performance audit: no unbounded scans, forced chunk loads, or uncontrolled world writes.
- [ ] Non-default world-border tests and full regression audit.
- [ ] Verified release artifact and installation instructions.


## Current milestone — The World Remembers

- [x] Persist bounded coarse location history in each player's saved data.
- [x] Track look-backs, pauses, and sprinting without recording an exact movement trail.
- [x] Trigger rare, player-only familiar-place anomalies on revisits without changing blocks.
- [x] Adapt Null's distance and relocation cadence to previous sightings and player behavior.
- [x] Allow Null to reuse remembered locations only when their chunks are already loaded and a safe standing position exists.
- [x] Add sparse environmental cues without ordinary chat/actionbar narration.
- [x] Add rare Almighty influence gated by the configured border territory, including subtle nearby-mob interference.
- [x] Add regression tests for behavior adaptation and Almighty territory gating.

## Remaining presentation work

- [ ] Client-side localized ambient audio attenuation (the current hush is a restrained visual cue, not a true audio mute).
- [ ] Client-only terrain afterimages and silhouette misdirection.
- [ ] Directional positional audio for player-specific distant sounds.
- [ ] Fear forest biome, structures, ambience, and full territory behavior.
- [ ] Death hunter AI, territory, structures, and death.jar clue chain.
- [ ] The Almighty's full encounter and endgame.
