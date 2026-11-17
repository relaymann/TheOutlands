# The Outlands

**The Outlands** is a psychological survival-horror ARG mod for Minecraft Java Edition 1.21.1 on NeoForge.

The Overworld begins as recognizable vanilla Minecraft. Once players travel beyond roughly 1,000 blocks from the world's original shared spawn, increasingly strange, hostile, and supernatural events begin. Escalation has no fixed final tier: it continues outward toward the world's configured border.

## Project status

Early foundation — the initial mod framework and distance-progression model are being established. This repository is not yet a playable horror release.

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
