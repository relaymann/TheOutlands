# Development plan

## Phase 0 — Foundation

- [x] Establish NeoForge 1.21.1 / Java 21 / Gradle project.
- [x] Add mod metadata and client/server run configurations.
- [x] Implement an unbounded distance-based progression calculation.
- [x] Evaluate server-side progression once per second for Overworld players.
- [x] Document current lore, architecture, and engineering constraints.
- [ ] Verify the Gradle build in CI and fix integration issues.

## Phase 1 — Horror director

- [ ] Event definitions with eligibility predicates and outcomes.
- [ ] Per-player and per-area cooldowns and encounter budgets.
- [ ] Pacing controller that alternates pressure with breathing room.
- [ ] Context inputs: distance, biome, time, weather, player history, recent events, and nearby players.
- [ ] Debug-only inspection command for progression and event eligibility.

## Phase 2 — Null

- [ ] Null entity registration and visual identity.
- [ ] Watcher/stalker AI with distant observation and rare interventions.
- [ ] Long-range appearance rules without making Null a constant nuisance.
- [ ] Null's apostle framework and first subordinate concepts.
- [ ] Multiplayer-aware targeting and persistence.

## Phase 3 — Fear

- [ ] Fear introduction milestone and adaptive single-/multiplayer dialogue.
- [ ] Fear forest biome and biome ambience.
- [ ] Fear-exclusive structures and environmental effects.
- [ ] Increased entity/event activity with controlled spawn budgets.
- [ ] Confirm whether 15,000 blocks is the first appearance and 100,000 is the full territory milestone.

## Phase 4 — Death / death.jar

- [ ] Death entity and persistent hunter AI.
- [ ] Tracking, misdirection, pressure, escape windows, and attack commitment.
- [ ] death.jar ARG clue chain and reveal.
- [ ] Multiplayer hunt rules and fairness safeguards.

## Phase 5 — The Almighty

- [ ] Border-relative final territory calculations.
- [ ] Entity exclusion rules for the final 5,000-block region.
- [ ] The Almighty's presence and final encounter.
- [ ] Endgame revelations and possible outcomes.

## Phase 6 — World and ARG content

- [ ] Vanilla-compatible anomaly structures.
- [ ] Evidence, books, signs, coordinate puzzles, and connected clues.
- [ ] Safe world modification rules that protect player builds.
- [ ] Audio, visual effects, and client-side presentation.
- [ ] Content variation for long-term exploration.

## Release gates

- [ ] Client and dedicated-server smoke tests pass.
- [ ] Multiplayer synchronization and player-specific events pass.
- [ ] No unbounded chunk/entity scans or uncontrolled world writes.
- [ ] World-border behavior works with custom border settings.
- [ ] Full audit for correctness, performance, compatibility, and regressions.
- [ ] Build artifact and installation instructions verified.
