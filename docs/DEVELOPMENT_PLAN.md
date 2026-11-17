# Development plan

## Phase 0 — Foundation
- [x] NeoForge 1.21.1 / Java 21 / Gradle project and metadata.
- [x] Unbounded distance progression from original shared Overworld spawn.
- [x] Server-side progression evaluation once per second.

## Phase 1 — Horror director and first encounters
- [x] Per-player pressure, quiet periods, cooldowns, weighted selection, and recent-event memory.
- [x] Context inputs: biome, day/night, weather, nearby players, and health.
- [x] Executable server-side encounters for all six initial event families.
- [x] Player-targeted sound, particles, and messages.
- [x] Operator debug command.
- [ ] Verify full Gradle build/tests and dedicated-server smoke test.

## Phase 2 — Null
- [x] Dedicated registered Null entity with a player-proportioned humanoid renderer.
- [x] Pure-black 64x64 skin with exactly two white eyes and no other facial detail.
- [x] Temporary observer sightings with bounded lifetime and no combat.
- [ ] Advanced watcher/stalker AI, retreat behavior, and long-range appearances.
- [ ] Custom animations, persistent story state, and Null's apostles.
- [ ] Multiplayer-aware target selection and story progression.

## Phase 3 — Fear
- [ ] First appearance milestone and adaptive multiplayer dialogue.
- [ ] Fear forest biome, ambience, exclusive structures, and environmental effects.
- [ ] Increased entity/event activity with controlled spawn budgets.
- [ ] Validate 15,000-block introduction and 100,000-block full territory.

## Phase 4 — Death / death.jar
- [ ] Hunter AI, tracking, misdirection, escape windows, and attack commitment.
- [ ] death.jar ARG clue chain and reveal; multiplayer fairness safeguards.

## Phase 5 — The Almighty
- [ ] Territory from configured world-border center and size.
- [ ] Entity exclusion for the final 5,000-block region.
- [ ] Border encounter, presence, and finale.

## Phase 6 — World and ARG
- [ ] Vanilla-compatible anomaly structures, evidence, books, signs, and coordinate puzzles.
- [ ] Safe world modification rules protecting player builds.
- [ ] Custom audio/visual content and long-term variation.

## Release gates
- [ ] Client and dedicated-server startup smoke tests.
- [ ] Multiplayer synchronization and player-specific event tests.
- [ ] Performance audit: no unbounded scans, forced chunk loads, or uncontrolled world writes.
- [ ] Non-default world-border tests and full regression audit.
- [ ] Verified release artifact and installation instructions.
