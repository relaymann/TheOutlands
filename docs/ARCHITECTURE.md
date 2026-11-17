# Technical architecture

## Target
- Minecraft Java 1.21.1; NeoForge 21.1.266; Java 21; ModDevGradle.
- Mod id: `theoutlands`; package: `com.relaymann.theoutlands`.

## Implemented systems
The original shared Overworld spawn is the fixed distance origin. The first 1,000 horizontal blocks remain outside the horror range. Escalation grows continuously beyond it without an arbitrary content cap.

The director evaluates once per second, builds per-player pressure, enforces a distance-scaled quiet period, and selects weighted encounters. Context includes biome, night/day, rain, thunder, nearby-player count, and health fraction. Context changes weights only; it cannot bypass minimum distance, pressure, cooldown, or quiet-period gates.

Selected encounters execute on the logical server. Ambient displacement, false footsteps, distant figures, environmental distortion, hostile warnings, and direct intervention produce player-targeted audio, particles, and/or messages. Events do not alter blocks or force-load chunks.

## Null's visual identity
Null is a dedicated registered entity, not an Enderman or disguised player. The renderer uses the vanilla humanoid/player body proportions with a 64x64 custom skin. The texture is opaque pure black across the entire body and head, with exactly two white eye marks on the front of the head. No clothing, armor, pupils, or additional facial features are present.

The current sighting behavior creates a temporary invulnerable, silent Null that watches nearby players without attacking or wandering. This is an initial observer behavior, not the finished long-range stalking system or final animation suite.

Operators can use `/theoutlands debug` (permission level 2) to inspect distance, tier, pressure, and the latest director result.

## Multiplayer and performance
The server owns selection and execution. Targeted effects go only to the selected player. Progression runs once per second and performs one bounded nearby-player query. Watcher cleanup visits only tracked UUIDs. No chunk is force-loaded and no player-built blocks are modified.

## Planned
Null's advanced stalker AI, custom animations, apostles, persistent player memories, Fear biome/structures, Death hunter, configured-border-relative Almighty territory, custom audio, and ARG content remain future work. Border geometry must use the configured border center and size.

## Testing
Pure tests cover opening-range protection, pacing, cooldown boundaries, safe random rolls, and contextual weighting. Client/server and multiplayer smoke tests remain release requirements.
