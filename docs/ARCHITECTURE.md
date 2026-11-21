# Technical architecture

## Target
- Minecraft Java 1.21.1; NeoForge 21.1.266; Java 21; ModDevGradle.
- Mod id: `theoutlands`; package: `com.relaymann.theoutlands`.

## Configuration
The common config exposes horror intensity, event frequency, event enablement, opening anomaly distance, Fear and Death milestone distances, Fear's full-territory distance, Almighty border depth, Null sightings, horror audio, and Fear's ambient attenuation. Server-owned progression and border calculations read the configured values. Client-only Fear audio/fog effects read the synchronized common config value.

## Horror director and reality deception
The original shared Overworld spawn is the fixed distance origin. The first 1,000 horizontal blocks remain outside the horror range. Escalation grows continuously beyond it without an arbitrary content cap.

The director evaluates once per second, builds per-player pressure, enforces a distance-scaled quiet period, and selects weighted encounters. Context includes biome, night/day, rain, thunder, nearby-player count, health, prior encounters, look-backs, pauses, and previous Null/Fear discoveries. Context changes weights only; it cannot bypass minimum distance, pressure, cooldown, or quiet-period gates.

Ordinary encounters never narrate themselves in chat or actionbar. Rare intentional character dialogue is reserved for actual story interventions and the ending.

`ClientAfterimageManager` sends temporary fake block states to one player's connection and restores the current server block state after a bounded lifetime. Terrain afterimages, silhouettes, and broken-arch false structures never write to the logical world. All candidate positions must be in already-loaded chunks. These illusions are not permanent generated structures.

Directional audio packets are sent only to the intended player at actual world coordinates, allowing vanilla distance attenuation. In Fear's distant forest territory, the client locally scales down ambient and weather sound volume and blends fog toward a darker forest palette. These client effects do not alter other players' sound or view.

## Persistent player memory
`HorrorMemory` stores bounded coarse visited cells, a bounded set of haunted cells, recent encounters, lifetime encounter count, unique discoveries, behavioral samples, bounded coarse sleep/build locations, Null eye-contact observations, Death encounter timing/counts, apostle cooldowns, and clue progression. Null's watcher placement can prefer remembered travel cells, sleeping places, and player-built areas without storing a precise movement trail. It deliberately avoids a precise movement trail. Story memory is copied when the player entity is replaced during respawn.

Returning to a previously haunted cell can escalate from a terrain afterimage to a false structure. A different player in the same location does not automatically receive the same hallucination.

## Null and Null's apostles
Null's watcher manager selects safe positions in already-loaded chunks, prefers cover and remembered locations, and relocates while unobserved. Sustained eye contact or close approach ends a sighting. Eye-contact observations are persisted and influence later stalking distance, lifetime, and relocation cadence.

Null's unnamed apostles are separate registered observer entities. They are rare, player-specific, require repeated Null observations and sufficient distance from spawn, and disappear after sustained eye contact or close approach. They do not attack or use a predictable chase loop. They are excluded from the Almighty's final territory.

## Fear's forest territory
Fear's first meeting remains a player-specific milestone, with the existing intentional introduction dialogue shared with nearby witnesses. At the full-territory distance, forest biomes receive a sparse environmental overlay: nearby mobs sometimes stop and watch, forest animals occasionally behave unnaturally, ash drifts through the trees, distant sounds occur, and false structures can appear.

A separate `theoutlands:fear_forest` biome registry definition and NeoForge biome modifiers are included with dark-forest vegetation and a limited hostile spawn table. The default vanilla Overworld multi-noise biome source is not replaced or rewritten by this mod, so the new biome is not yet naturally selected by vanilla world generation. The distant-territory overlay remains active on existing forest biomes; automatic biome-source integration and permanent Fear structures remain future work.

## Death and the death.jar chain
Death is a persistent hunter bound to the player whose trail he first picks up. He does not retarget random nearby players. He pursues, pauses to study the target, can stop after an apparent escape, and resumes only while the target remains within a bounded hunting radius. His target and pause state are saved with the entity.

The clue sequence is `death.jar` → `manifest.txt` → `author.dat`. Each later artifact is released on a subsequent Death manifestation after the previous artifact has been discovered. The Almighty drops `the_last_page` when defeated. Clue discoveries are stored per player and survive logout/respawn.

## Almighty border geometry and endgame
`AlmightyTerritory` measures the inward distance to the nearest edge of the live square world border. It reads the border's configured center and size and defines the configured final strip on all four sides, including corners. Positions outside the border are excluded.

Server-side entity lifecycle enforcement rejects non-player entities when they attempt to join inside the final region and discards non-exempt entities that cross into it later. Players and the tagged Almighty are exempt. Entering the borderland starts the one-time encounter.

The Almighty has a server boss bar and three health phases: a close pressure pulse, a wider inward pull, and a final knockback shockwave. Phase changes alter the boss bar, particles, and roar. Defeating him drops the final artifact and records the ending discovery for nearby players. This is the implemented combat/revelation ending beat; a cinematic credits sequence and multi-outcome epilogue remain future work.

## Presentation assets
Null and Fear retain dedicated 64x64 textures. Death and The Almighty now use original 64x64 pixel textures rather than vanilla entity skins. The entity rigs remain humanoid-based; bespoke geometry, authored animation clips, and original audio recordings are still planned.

## Multiplayer and performance
The server owns event selection and entity behavior. Targeted sounds, particles, and block-state afterimages go only to the selected player. Fear's first introduction is a shared story event. Progression runs once per second, and nearby entity/player queries are bounded. Watcher and illusion cleanup visits only tracked UUIDs/positions. No chunk is force-loaded and no player-built blocks are changed.

## Testing and remaining release gates
Pure tests cover opening-range protection, pacing, cooldown boundaries, safe random rolls, contextual weighting, persistent memory policy, Almighty territory geometry, and apostle milestone boundaries. GitHub Actions runs `clean test build` and publishes the rolling JAR only after success. Dedicated-server startup, full client rendering/audio, multiplayer playtesting, and a full-worldgen compatibility pass remain release gates.


## Environmental pressure and existing-creature systems

The ambient anomaly layer adds rare, player-private positional audio cues after the configured distance from the shared Overworld spawn. It uses existing vanilla sound events and never edits blocks, creates fake terrain, teleports players, spawns a creature, or writes narration to chat. Ambient anomalies have their own enable switch and minimum-distance setting.

The existing-creature intelligence and ecology managers can be independently disabled in the common config. Intelligence retains bounded disturbance history, per-creature temperament, investigation cooldowns, pursuit adaptation, and virtual home ranges. Ecology handles established creature relationships and rare inter-entity encounters. Both systems remain server-authoritative; their cleanup removes stale per-entity state when creatures despawn or unload.

No terrain-generation hooks or world-generation settings are changed by these systems.


## Distance-based natural spawning and encounter balance

All 16 registered entities retain their biome-based natural spawn entries and habitat predicates. The spawn-placement layer now applies an additional Overworld distance profile measured horizontally from the original shared world spawn; it does not change biome generation, terrain, or the world seed.

The opening 500 blocks are protected from ordinary Outlands natural spawns. Common trackers (Lurker, Dredge, and Stalker) can begin appearing beyond 500 blocks, with spawn rolls becoming progressively less restrictive across the 500–1,000, 1,000–2,000, 2,000–4,000, and 4,000–7,000 block bands. Other creatures have their own later minimum distances: Howler at 1,500; Hunter, Observer, Burrower, and Mimic at 1,800; Cave.jar at 2,500; Stillborn Statue and Agitated One at 3,500; Null and Null Apostle at 7,000. Fear and Death use their existing configurable 15,000- and 50,000-block story milestones. Almighty retains its separate End-only spawn rule.

Natural encounters also use a nearby threat budget. Each entity has a threat cost, and the allowed local population grows gradually with distance: one entity/one threat point before 1,000 blocks, two/two from 1,000–2,000, three/four from 2,000–4,000, four/six from 4,000–7,000, and five/nine beyond 7,000. Existing Outlands creatures near the nearest survival player consume that budget, preventing repeated spawn attempts from stacking multiple powerful threats. Spawn eggs, commands, conversions, and other non-natural spawn reasons are not blocked by the progression gate. The entire balancing layer can be disabled with the common config option `entitySpawnBalanceEnabled` for controlled testing.

Surface and cave biome weights were modestly raised for common and mid-tier threats; rare story entities retain low weights and their larger distance requirements. Existing biome, light, height, and cave predicates remain in force.


## Additive Outlands structures

The Overworld receives a rare, configurable structure-feature layer registered through a NeoForge biome modifier on the existing Overworld biome tag. Vanilla biome sources, terrain noise, caves, and vanilla structure sets are not replaced or rewritten. The feature keeps its maximum 9x9 footprint inside the chunk being decorated, rejects sites within the configured distance from the original shared spawn, and requires a relatively level non-fluid surface.

The structures are:
- **Abandoned cabin** — forest/taiga wilderness, beyond 1,000 blocks.
- **Watchtower** — isolated observation post, beyond 1,800 blocks.
- **Sealed bunker** — buried stone room with a surface access shaft, beyond 2,500 blocks.
- **Silent shrine** — Fear-associated stone and crying-obsidian site, beyond 15,000 blocks.
- **Null observatory** — remote forest ring and dark pillars, beyond 22,000 blocks.
- **Death archive** — buried deepslate chamber, beyond 50,000 blocks.

Each structure uses a dedicated vanilla loot table appropriate to its setting. The structures do not place progression-gated ARG artifacts directly, preserving the existing Death clue chain. Player-specific false structures remain separate temporary illusions and never write permanent terrain changes. Common config options are `outlandsStructuresEnabled`, `structureMinDistance`, and `structureChanceMultiplier`.


## Lore structure batch 1

Four additional lore sites extend the additive feature pool:
- **Research Facility** — buried observation and containment rooms, implying earlier attempts to study the entities.
- **Apostle Cathedral** — a ruined sanctuary with seven sealed niches and a central altar, tied to the Almighty and the Apostles.
- **Forsaken Settlement** — a small cluster of damaged homes, a dry well and abandoned supplies, suggesting an abrupt disappearance.
- **Last Outpost** — a barricaded field position with lookout posts and a command cache, showing a failed attempt to hold the wilderness.

These use compact footprints that fit the existing single-chunk placement model. They are generated only in newly generated chunks and do not modify vanilla terrain generation rules or existing structures.
