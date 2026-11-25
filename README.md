# The Outlands

**The Outlands** is a psychological survival-horror ARG mod for Minecraft Java Edition 1.21.1 on NeoForge.

The Overworld begins as recognizable vanilla Minecraft. Once players travel beyond roughly 1,000 blocks from the world's original shared spawn, increasingly strange, hostile, and supernatural events begin. Escalation has no fixed final tier: it continues outward toward the world's configured border.

## Project status

The current build includes a player-specific horror director, persistent behavioral/location memory, client-only terrain/structure afterimages and silhouettes, positional audio, locally attenuated Fear-territory ambience, adaptive Null stalking that remembers travel, sleep, and building locations, plus rare Null apostle observers, Fear's forest-territory overlay plus a registered Fear biome definition, Death's target-bound hunter AI and death.jar clue chain, and the Almighty's three-phase endgame encounter. It remains an experimental development build.

## Technical target

- Minecraft Java Edition 1.21.1
- NeoForge 21.1.266
- Java 21
- Gradle 9.2.1 with ModDevGradle
- Standalone mod JAR
- Integrated and dedicated-server compatibility

## Optional Simple Voice Chat integration

The Outlands can optionally integrate with **Simple Voice Chat**. It is not a required dependency: the mod's normal entity AI, environmental sounds, encounters, and survival systems continue to work when Simple Voice Chat is absent.

When Simple Voice Chat is installed and connected:

- A small **VOICE xx%** microphone meter appears immediately above the hotbar. It reflects the local microphone's normalized PCM activity level and returns to 0% after speech stops. Whisper mode is labeled separately.
- Voice activity, approximate player position, normalized level, and whisper state are used for server-side entity hearing. Raw microphone samples and conversations are never sent to The Outlands' custom network payload or stored by the mod.
- Different entities have distinct hearing ranges and reactions: Stalkers investigate and stalk, Hunters pursue, Mimics use misleading original creature sounds, Howlers can alert nearby threats, Observers watch, and cave predators react to underground voices.
- Whispering reduces detection range; louder speech increases it. Walls, rain, thunder, cave depth, and nearby talking players influence detection.
- Some loud voice events can trigger rare environmental knocks. Entity voice memories can persist briefly after a player stops speaking.

For multiplayer, install a compatible Simple Voice Chat build on both the client and server and configure its voice transport as required by the server host. The Outlands integration is disabled by setting `voiceHorrorEnabled=false` in the common config. Its base hearing range is controlled by `voiceDetectionRange`.

The integration does not transcribe speech, understand arbitrary words, record conversations, or copy a player's voice for imitation. Mimic behavior uses the mod's own creature sounds.

## Core design

- Preserve normal vanilla world generation and survival gameplay as the foundation.
- Begin the horror ramp around 1,000 blocks from the world's original shared spawn.
- Escalate continuously with distance; internal tiers organize content but never cap the experience.
- Blend psychological horror with increasingly aggressive survival threats, while leaving room for quiet periods.
- Combine grounded encounters with supernatural world manipulation.
- Make the Overworld the primary experience all the way to the world's configured border.
- Support multiplayer with player-specific experiences alongside shared world events.
- Persist bounded, per-player behavioral and location memory across logout and respawn.
- Keep familiar-place anomalies private to the returning player; client afterimages never change server-world blocks or player builds.
- Make Null adapt to prior sightings, look-backs, pauses, and repeated locations.
- Keep horror cues sparse and diegetic; ordinary events never explain themselves in chat or actionbar.
- Gate the Almighty's endgame encounter by entry into the configured world-border territory.

## Known hierarchy

- **The Almighty** — mastermind behind the events; waits in the final 5,000-block region before the configured world border.
- **Null** — first apostle; primarily watches and stalks around spawn, can intervene, commands apostles of his own, and may appear far beyond his usual territory.
- **Fear** — second apostle; first appears around 15,000 blocks and exerts a forest-territory overlay around the 100,000-block milestone. A separate Fear biome definition and biome modifiers are included; automatic insertion into vanilla's default biome source and permanent Fear structures remain future work.
- **Death / death.jar** — final apostle beneath The Almighty; a persistent hunter who stalks and attacks. First appears around 50,000 blocks and advances a three-artifact clue chain.

See [docs/LORE_AND_DESIGN.md](docs/LORE_AND_DESIGN.md), [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), and [docs/DEVELOPMENT_PLAN.md](docs/DEVELOPMENT_PLAN.md).

## Download and install

The latest successful build is published automatically to the rolling **Latest The Outlands build** GitHub Release. The download is a direct JAR asset, not a temporary Actions artifact.

- [Download TheOutlands.jar](https://github.com/relaymann/TheOutlands/releases/latest/download/TheOutlands.jar)
- [View the latest release and SHA-256 checksum](https://github.com/relaymann/TheOutlands/releases/latest)

Install it in the `mods` folder of a Minecraft Java Edition **1.21.1** instance using **NeoForge 21.1.266** and **Java 21**. Remove older copies of The Outlands first so only one version is loaded. Back up worlds before using experimental builds.

A release is published only after the build and tests pass on `main`. Pull requests run the build but do not publish downloads.

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


## Animated creatures (GeckoLib)

The Outlands uses GeckoLib 4.9.4+ for animated monster rigs. GeckoLib is a required dependency on both client and dedicated server. See [the GeckoLib integration guide](docs/GECKOLIB.md) for the shared entity animation architecture and asset workflow.
