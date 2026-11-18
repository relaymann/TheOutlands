# Technical architecture

## Target
- Minecraft Java 1.21.1; NeoForge 21.1.266; Java 21; ModDevGradle.
- Mod id: `theoutlands`; package: `com.relaymann.theoutlands`.

## Configuration
The common config exposes horror intensity, event frequency, event enablement, opening anomaly distance, Fear and Death milestone distances, Fear's full-territory distance, Almighty border depth, Null sightings, and horror audio. Defaults preserve the canonical design values. The server-authoritative director and border calculations read the configured values.

## Implemented systems
The original shared Overworld spawn is the fixed distance origin. The first 1,000 horizontal blocks remain outside the horror range. Escalation grows continuously beyond it without an arbitrary content cap.

The director evaluates once per second, builds per-player pressure, enforces a distance-scaled quiet period, and selects weighted encounters. Context includes biome, night/day, rain, thunder, nearby-player count, and health. Context changes weights only; it cannot bypass minimum distance, pressure, cooldown, or quiet-period gates.

Selected encounters execute on the logical server. Ambient displacement, false footsteps, distant figures, environmental distortion, hostile warnings, direct intervention, private unexplained messages, false-death messages, and returning-observer encounters are executable. These events do not alter blocks or force-load chunks.

## Persistent player memory
`HorrorMemory` stores a bounded recent encounter list, a lifetime encounter count, last encounter game time, and unique discovery identifiers in NeoForge's persistent player entity data. Encounter execution records the event immediately. Story memory is copied when the player entity is replaced during respawn. The discovery API is ready for future structure and ARG clue integrations.

## Apostle milestone model
`ApostleProgression` centralizes the canonical story thresholds: Fear's first appearance at 15,000 blocks, Death's first appearance at 50,000 blocks, and Fear's full-territory milestone at 100,000 blocks. Null remains eligible at any distance. The Almighty's final territory overrides Death's territory. These are progression gates, not a cap on general horror escalation.

This milestone model is currently used for operator diagnostics. It does not yet spawn Fear or Death, generate the Fear biome, or persist apostle story encounters.

## Almighty border geometry
`AlmightyTerritory` measures the inward distance to the nearest edge of the live square world border. It reads the border's configured center and size from the level and defines the final 5,000-block strip on all four sides, including corners. Positions outside the border are excluded. The geometry is isolated and unit-tested against shifted centers, non-default sizes, all sides, and boundary conditions.

Server-side entity lifecycle enforcement now rejects non-player entities when they attempt to join inside the final region and discards non-exempt entities that cross into it later. Players remain able to enter for the endgame. The future Almighty entity can opt in using the dedicated `theoutlands_almighty` entity tag. The Almighty encounter itself remains unimplemented.

## Null's visual identity and sightings
Null is a dedicated registered entity, not an Enderman or disguised player. The renderer uses vanilla humanoid/player proportions with a 64x64 custom skin. The PNG is 64x64 RGBA, fully opaque black except for eight white pixels forming two separated 2x2 eye marks on the front of the head.

The current sighting manager creates temporary invulnerable, silent Null entities at safe positions in already-loaded chunks. They watch the target, disappear after sustained eye contact or close approach, and can relocate to another safe hiding position while unobserved. This remains an initial stalker behavior, not the finished long-range stalking intelligence or final animation suite.

Operators can use `/theoutlands debug` (permission level 2) to inspect spawn distance, escalation tier, pressure, the latest director result, border distance, and apostle milestone eligibility.

## Multiplayer and performance
The server owns selection and execution. Targeted effects go only to the selected player. Progression runs once per second and performs one bounded nearby-player query. Watcher cleanup visits only tracked UUIDs. No chunk is force-loaded and no player-built blocks are modified.

## Planned
Advanced Null behavior adaptation, false sightings, custom animations, Null's apostles, Fear and Death entities, Fear biome/structures, Death hunting AI, entity exclusion from the Almighty territory, configured-border-relative finale, custom audio, and ARG content remain future work.

## Testing
Pure tests cover opening-range protection, pacing, cooldown boundaries, safe random rolls, contextual weighting, persistent player memory, Almighty territory geometry, and apostle milestone boundaries. Client/server and multiplayer smoke tests remain release requirements.
