# GeckoLib animation integration

The Outlands targets Minecraft 1.21.1 / NeoForge 21.1.266 / Java 21 and requires GeckoLib 4.9.4 or newer within major version 4.

## Runtime dependency

Gradle resolves GeckoLib from the official GeckoLib Cloudsmith Maven repository. GeckoLib is declared as a required NeoForge mod dependency, so clients and dedicated servers must install the matching GeckoLib NeoForge 1.21.1 jar.

## Entity architecture

All 24 registered Outlands creature types use the shared `OutlandsGeoMonster` animation host. It preserves the existing `Monster` behavior and centralizes the GeckoLib instance cache and controllers. Each entity retains its own attributes, AI goals, targeting, spawn rules, sounds, and server-side mechanics.

The GeckoLib renderers use a dedicated geometry file for every creature under `assets/theoutlands/geo/entity/`, including all five Nightmare variants. `OutlandsGeoModel` selects each creature's geometry and texture while sharing the animation resource. The Mimic retains its block-disguise rendering until it reveals, and the Almighty retains its oversized rendering treatment.

## Animation system

The shared animation file currently contains 21 clips:

| Clip | Purpose |
|---|---|
| `idle` | Subtle breathing, head movement, jaw motion, and spine movement |
| `walk` | Standard biped locomotion |
| `crawl` | Low, forward-driven movement for crawlers and quadrupeds |
| `heavy_walk` | Slow, weighty steps for large entities |
| `hover` | Floating and drifting for stationary manifestations |
| `stalk_idle` | Restrained, watchful idle pose |
| `watch_idle` | Delayed, unsettling head movement for watchers |
| `statue_idle` | Almost motionless pose with a sudden head snap |
| `sprint` / `crawl_sprint` | Aggressive pursuit locomotion |
| `attack` | Melee strike, triggered only after a successful hit |
| `scream` | Howler's territorial call wind-up |
| `lunge` / `pounce` | Sudden forward attacks |
| `reveal` | Mimic's disguise break |
| `burrow` / `emerge` | Burrower's subterranean departure and return |
| `drop` | Ceiling Dweller's overhead ambush release |
| `ground_slam` | Colossus' telegraphed heavy strike |
| `stalk` | Lurker's patient stalking behavior |
| `death` | Shared creature death pose |

The locomotion controller chooses idle and movement styles by creature archetype and sprint state. The synchronized action controller handles one-shot combat and horror events. AI hooks are server-authoritative and synchronize the resulting animation to tracking clients.

These clips are presentation-only. They do not change attack timing, movement, hitboxes, damage, spawn rules, or AI authority. The melee attack clip still plays only after a real hit succeeds.

## Authoring creature-specific animation

Use Blockbench's GeckoLib model format. Keep shared animation bone names compatible: `root`, `body`, `head`, `jaw`, `spine`, `left_arm`, `right_arm`, `left_leg`, and `right_leg`. Creature-specific geometry may add bones for horns, tendrils, tails, mandibles, or extra limbs.

When adding or changing a clip:
1. Add the clip to `outlands_monster.animation.json`.
2. Use only bone names present in every rig that will play that shared clip.
3. Add unique rig bones to every applicable geometry file, or keep the animation on the shared rig.
4. Add a synchronized AI trigger only when the corresponding gameplay event already exists.
5. Keep animation presentation separate from gameplay timing, hitboxes, and server authority.

## Verification checklist

Automated validation checks all 24 entity registrations, dedicated geometry files, required shared bones, all 21 required clips, and every animated bone reference against every creature rig. It also checks the synchronized AI hooks.

- Run `./gradlew clean test build` with Java 21.
- Verify the packaged JAR contains every geometry, texture, and animation resource.
- Launch both the client and a dedicated server with GeckoLib 4.9.4 installed.
- Spawn all 24 entity types and verify each dedicated geo rig, texture, idle, locomotion, melee attack, death, and tracking synchronization.
- Test the Mimic's block disguise and reveal, the Almighty's scale, Nightmare variants, and entity unload/reload to ensure animation state does not leak across instances.

The automated build does not replace in-game visual testing; the client and dedicated-server checklist remains a manual verification step.
