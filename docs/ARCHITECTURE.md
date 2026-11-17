# Technical architecture

## Target

- Minecraft Java Edition 1.21.1
- NeoForge 21.1.266
- Java 21
- Gradle + ModDevGradle
- Mod id: `theoutlands`
- Base package: `com.relaymann.theoutlands`

## Current implementation

### Bootstrap

`TheOutlands` is the mod entry point. Keep it limited to initialization and registration.

### Distance progression

`DistanceProgression` measures horizontal distance from the world's original shared Overworld spawn. Personal respawn points do not change the horror origin.

The initial 1,000 blocks return zero escalation. Beyond that, the continuous value grows logarithmically and the internal milestone tier grows at doubling-distance intervals. Neither calculation has a content cap.

### Snapshot manager

`HorrorProgressionManager` keeps a lightweight in-memory snapshot for each online player. Snapshots are refreshed once per second and removed on logout. They are derived data, not permanent player memory.

### Event hook

`ServerHorrorEvents` listens to the server-side player tick event. It evaluates only Overworld players, at a 20-tick interval. No entity search, chunk scan, structure generation, or world mutation belongs in this tick handler.

## Planned subsystem boundaries

- `horror`: distance, intensity, encounter budgets, escalation profiles.
- `event`: event definitions, eligibility, scheduling, cooldowns, outcomes.
- `entity`: entity registration, AI goals, behavior profiles, apostle systems.
- `world`: anomaly placement, protected-area checks, world-border calculations.
- `worldgen`: Fear biome, structures, features, placement rules, data generation.
- `memory`: persistent player discoveries, encounter history, world-level story flags.
- `investigation`: clues, evidence, coordinate puzzles, ARG progression.
- `network`: synchronization for player-specific visual/audio events and shared state.
- `client`: rendering, audio presentation, effects, and client-only presentation code.

Introduce these packages as systems are implemented; do not create empty placeholder classes for every future feature.

## Server authority and multiplayer

- Server decides whether an event is eligible and when it occurs.
- Client handles presentation only after receiving an authorized event.
- Player-specific hallucinations are sent only to the affected player.
- Shared world events are synchronized to relevant players.
- Persistent discoveries and consequences survive reconnects and server restarts.
- Never trust client-reported distance, encounter results, or story progress.

## Performance rules

- Evaluate progression on a controlled interval, not every frame.
- Never scan all loaded chunks or all entities to decide whether a player is eligible for an event.
- Use spatial indexes, bounded searches, or event-local queries when entity awareness is needed.
- Add cooldowns and per-player/per-area encounter budgets.
- Avoid repeated world writes; validate target blocks and protect player-built areas.
- Keep world generation deterministic and compatible with chunk regeneration rules.
- Clean up transient player state on logout and server stop.

## World border

Read the world's configured border rather than assuming a fixed default coordinate. The Almighty's final region is defined relative to the actual border and its center/size. Border geometry must be handled deliberately, including non-default border centers and sizes.

## Persistence strategy (planned)

- Derived distance/intensity: recompute; do not save.
- Player discoveries, evidence, encounter history, and lasting consequences: persistent player data.
- World-level story flags, major structure/event completion, and shared milestones: Overworld saved data.
- Transient active encounters and cooldowns: server runtime state unless a restart must preserve them.

## Testing strategy

- Compile on every change.
- Add pure tests for distance math, border geometry, escalation curves, event eligibility, and cooldown behavior.
- Test integrated single-player and dedicated-server startup.
- Test multiple world-border sizes and non-zero border centers.
- Test multiplayer with different player distances and player-specific events.
