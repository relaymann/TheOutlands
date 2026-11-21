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

Historical first-pass implementation: the original six structures were initially placed as a chunk-contained configured feature through a NeoForge biome modifier. That implementation was superseded in Phase 1 by the native 48x48 multi-chunk Structure/StructurePiece pipeline documented below. The biome source, terrain noise, cave carvers, and vanilla structure sets remain unchanged.

The current structure catalog contains 14 identities: Abandoned Cabin, Watchtower, Sealed Bunker, Silent Shrine, Null Observatory, Death Archive, Research Facility, Apostle Cathedral, Forsaken Settlement, Last Outpost, Forgotten Station, Null Chamber, Hollow Crypt, and Boundary Monument.
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

These four identities were subsequently migrated into the native 48x48 multi-chunk structure piece system. Their placements are additive, clipped per chunk, and only affect newly generated chunks.


## Lore structure batch 2

The second batch adds four more specialized locations:
- **Forgotten Station** — a buried platform and short rail segment ending at a sealed wall, implying a route that was deliberately abandoned.
- **Null Chamber** — a rare deep room with a stark central marker and four blackstone pillars, connected to failed attempts to contact Null.
- **Hollow Crypt** — a deep burial hall with empty marked alcoves and a sealed central tomb, tied to Death's presence without placing any Death progression artifacts.
- **Boundary Monument** — broken stone rings and an empty central frame, suggesting a boundary without creating a portal or changing dimensions.

These four identities also use the native multi-chunk piece pipeline, appear only in newly generated chunks, and leave vanilla terrain generation systems intact.


### Batch 2 fidelity pass

- Forgotten Station now has a waiting platform, benches, twin rails, and a blocked tunnel passage.
- Null Chamber remains intentionally sparse, with four pillars and a single central marker.
- Hollow Crypt has connected corridors, multiple empty tomb alcoves, and a sealed central burial feature.
- Boundary Monument has broken concentric rings, tall asymmetrical uprights, and an empty freestanding frame; it creates no portal.


## Structure placement foundation (Phase 1)

Outlands structure placement now uses deterministic 8x8-chunk candidate cells. A seed-derived anchor is selected per cell, and nearby anchors compete by stable priority using a configurable minimum chunk spacing (default 4 chunks). This avoids repeated per-chunk random rolls and reduces clusters without searching or force-loading neighboring chunks.

Eligible structures are selected with one weighted roll rather than a sequence of independent early-return rolls, so structures later in the registry are no longer suppressed by earlier checks. Per-cell probabilities are scaled to preserve approximately the previous per-area encounter rate. Placement is deterministic from the world seed and chunk coordinates.

Sites reject ocean, river, and beach biomes, water/fluid surfaces, leaf/log canopy tops, steep footprints, and unsafe build-height margins. The later native structure pipeline uses a 48x48 multi-chunk footprint clipped to each generating chunk. Vanilla terrain, caves, biome sources, and vanilla structure sets are unchanged.

This describes the intermediate placement-policy stage. It was superseded by the native multi-chunk piece/template-system migration in the following Phase 1 section; the current 14 builders are procedural modules invoked through persisted native pieces.


## Native multi-chunk structure pipeline

Outlands sites are now generated through Minecraft's native structure, structure-set, and structure-piece pipeline rather than a biome-injected configured feature. One seed-stable structure candidate is considered per chunk; the deterministic placement policy chooses an anchor from each 8x8-chunk cell and rejects nearby competing anchors using the configurable spacing value. A single structure set owns all Outlands sites, so they share one spacing/placement field and cannot be independently stacked by separate sets.

The native piece spans a 48x48-block footprint (normally three chunks across). Minecraft persists the piece and invokes it for each intersecting chunk; each invocation writes only the supplied chunk intersection. Layout selection is deterministic from the world seed and anchor coordinates, while the final distance gate uses the actual shared Overworld spawn available to the generation level. The existing distance/rarity progression is retained. The 14 site identities now receive larger connected wings, corridors, yards, side rooms, tunnels, crypt cells, and perimeter features around their original focal builds.

Candidate sites use a 3x3 sample of generator height averages and reject footprints with more than five blocks of height variation or unsafe build-height margins. The structure biome tag includes land-oriented Overworld biomes and excludes oceans, rivers, and beaches. Structure terrain adaptation is none; the mod does not alter vanilla noise settings, cave carvers, biome sources, or vanilla structure sets. Existing chunks are not retroactively populated.

The native structure JSON is data-driven, while the custom codec only carries the footprint size. Structure pieces persist their anchor, surface height, size, and seed in NBT. All block writes are clipped to the current structure-piece chunk box, including chest placement, so generation does not force-load or write into adjacent chunks.


## Structure depth and exploration (Phase 2A–2C)

All 14 structure identities now use dedicated Outlands chest loot tables under `theoutlands:structures/`. Loot is themed to each location and remains bounded: common supplies are useful but modest, while unusual curiosities and rare materials are uncommon. The existing Death progression artifacts are deliberately excluded, preserving the player-specific clue chain and Almighty ending reward.

Each site chest can contain a written field record with location-specific observations. These records provide fragments rather than a narrator or a complete explanation; several references point toward other sites and leave the entities' origins unresolved.

Every structure also receives a concealed lower service chamber, reached through a moss-covered surface hatch and ladder shaft. Each chamber has structure-specific physical clues, a small cache, and a recovered record. Its placement is clipped to the native structure piece bounds. The piece's vertical bounds and candidate height margin include the chamber. These additions affect only newly generated Outlands structure chunks; vanilla terrain noise, caves, biome sources, and vanilla structure sets remain untouched.


## Structure atmosphere and encounters (Phase 2D–2F)

Structure ambience is player-private and only runs while a living, non-creative player is inside a registered Outlands structure. Cues are sparse, use positional audio, and have per-player cooldowns. Every structure identity now has its own original procedurally synthesized OGG atmosphere, registered as a dedicated sound event and played privately with structure-specific volume and pitch. The sound generator is deterministic and runs in the release workflow.

A structure can produce at most one deterministic, rare encounter when a survival player first explores it. Entity insertion occurs on the main server tick, never during asynchronous structure generation. Each site has a themed entity pool and respects existing progression milestones for Null, Null's Apostles, and Death. Encounters are not guaranteed, and the manager is gated by the entity-balance and ecosystem settings. Structure layouts also select seeded variations such as damaged rooms, extra wings, debris, and altered furnishings. All generated block writes, including chests and lower chambers, are clipped to the active structure-piece chunk box.

## Persistent structure discoveries (Phase 2G)

Opening a chest belonging to an Outlands structure records that site in the player's existing bounded, persistent HorrorMemory discovery list. Discoveries are private, de-duplicated, and survive logout and respawn through the existing memory-copy path. Every site's concealed lower chamber contains the same three-stroke mark. Each recovered site record describes that mark and points toward another structure, forming a distributed clue network rather than one explanatory document. These clues do not grant, duplicate, or bypass Death's progression-gated artifacts or the Almighty ending reward.

## Structure compatibility and verification (Phase 2H)

The native structure set remains a single additive Outlands set. It does not replace vanilla structure sets, biome sources, terrain noise, or cave carvers. Its placement excludes an eight-chunk radius around villages, and each generated chunk checks for intersecting non-Outlands structure starts before any blocks are written. Wet, log-obstructed, and unexpectedly uneven chunk surfaces are also rejected rather than being overwritten. Placement samples the no-leaves motion-blocking heightmap so forest canopies do not become false ground surfaces. Its placement excludes an eight-chunk radius around villages, and each generated chunk is checked against intersecting non-Outlands structure starts before any blocks are written, preventing Outlands pieces from overwriting vanilla or other-mod structures in overlapping chunks. Candidate placement remains seed-stable and spaced; structure pieces persist their anchors and clip writes to the current chunk intersection. The placement policy exposes a pure inclusive-boundary predicate with unit tests for all six faces and out-of-bounds coordinates. The structure audit validates all 14 layout identities, all 15 loot tables, unique multi-page field records, cross-site clues, shared markings, the native 48-block structure definition, biome exclusions, chunk clipping hooks, ambience gating, and encounter progression guards.

Automated checks are static/data and unit tests, not a substitute for dedicated-server world generation. Final compatibility release gates still include launching a dedicated server, generating fresh worlds across varied biomes and chunk borders, checking coexistence with vanilla structures, opening every loot table in-game, and multiplayer client audio/render testing. Existing chunks are not retroactively populated. No vanilla terrain or world-generation settings are changed.
