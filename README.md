# The Outlands

**The Outlands** is a psychological survival-horror ARG mod for Minecraft Java Edition 1.21.1 on NeoForge.

The Overworld begins as recognizable vanilla Minecraft. Once players travel beyond roughly 1,000 blocks from the world's original shared spawn, increasingly strange, hostile, and supernatural events begin. Escalation has no fixed final tier: it continues outward toward the world's configured border.

## Project status

The current foundation includes distance-based horror progression, a player-specific encounter director, persistent ARG discoveries, Null watcher sightings, Fear's first encounter, and the **The World Remembers** milestone: coarse location memory, behavioral tracking, adaptive Null stalking, familiar-place anomalies, restrained environmental cues, and rare Almighty influence. This is an active development build, not yet a finished release.

## Technical target

- Minecraft Java Edition 1.21.1
- NeoForge 21.1.266
- Java 21
- Gradle 9.2.1 with ModDevGradle
- Standalone mod JAR
- Integrated and dedicated-server compatibility

## Core design

- Preserve normal vanilla world generation and survival gameplay as the foundation.
- Begin the horror ramp around 1,000 blocks from the world's original shared spawn.
- Escalate continuously with distance; internal tiers organize content but never cap the experience.
- Blend psychological horror with increasingly aggressive survival threats, while leaving room for quiet periods.
- Combine grounded encounters with supernatural world manipulation.
- Make the Overworld the primary experience all the way to the world's configured border.
- Support multiplayer with player-specific experiences alongside shared world events.
- Persist bounded, per-player behavioral and location memory across logout and respawn.
- Keep familiar-place anomalies private to the returning player and never overwrite player-built blocks.
- Make Null adapt to prior sightings, look-backs, pauses, and repeated locations.
- Keep horror cues sparse and diegetic; ordinary events never explain themselves in chat or actionbar.
- Gate rare Almighty influence by proximity to the configured world-border territory.

## Known hierarchy

- **The Almighty** — mastermind behind the events; waits in the final 5,000-block region before the configured world border.
- **Null** — first apostle; primarily watches and stalks around spawn, can intervene, commands apostles of his own, and may appear far beyond his usual territory.
- **Fear** — second apostle; introduces the Fear forest biome, heightened entity activity, and territory-specific structures. His first encounter and full territorial expansion are treated as separate provisional milestones.
- **Death / death.jar** — final apostle beneath The Almighty; a persistent hunter who toys with prey before attacking. First appears around 50,000 blocks.

See [docs/LORE_AND_DESIGN.md](docs/LORE_AND_DESIGN.md), [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), and [docs/DEVELOPMENT_PLAN.md](docs/DEVELOPMENT_PLAN.md).

## Development

Requirements:
- JDK 21
- Gradle 9.2.1
- Git
- Internet access for Gradle to download Minecraft and NeoForge development dependencies

Useful commands:

```bash
gradle tasks
gradle build
gradle runClient
gradle runServer
```

The built mod JAR is placed in `build/libs/`.

## Engineering principles

- Keep gameplay logic server-authoritative.
- Never scan all loaded chunks or entities every tick.
- Evaluate progression on controlled intervals and cache derived state.
- Do not overwrite player-built blocks when generating or staging anomalies.
- Persist important world/player state; recompute derived values such as distance.
- Keep horror content modular so new apostles, entities, events, structures, and clues can be added without rewriting progression.
- Audit changes for correctness, multiplayer behavior, performance, and regressions.

## License

All Rights Reserved. No permission to redistribute modified or unmodified builds is granted unless explicitly authorized by the copyright holder.
