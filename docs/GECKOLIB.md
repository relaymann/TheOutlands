# GeckoLib animation integration

The Outlands targets Minecraft 1.21.1 / NeoForge 21.1.266 / Java 21 and requires GeckoLib 4.9.4 or newer within major version 4.

## Runtime dependency

Gradle resolves GeckoLib from the official GeckoLib Cloudsmith Maven repository. The dependency is declared as an implementation dependency and as a required NeoForge mod dependency, so clients and dedicated servers both install the matching GeckoLib NeoForge 1.21.1 jar.

## Entity architecture

All 24 registered Outlands creature types use the shared `OutlandsGeoMonster` animation host. It preserves the existing `Monster` behavior and centralizes the GeckoLib instance cache and controllers. Each entity retains its own attributes, AI goals, targeting, spawn rules, sounds, and server-side mechanics.

The GeckoLib renderers use a dedicated geometry file for every creature under `assets/theoutlands/geo/entity/`, including all five Nightmare variants. The animation file is shared and currently provides:

- `idle`: subtle breathing, head movement, jaw motion, and spine movement
- `walk`: standard biped locomotion
- `crawl`: low, forward-driven movement for crawlers and quadrupeds
- `heavy_walk`: slower, heavier steps for large entities
- `hover`: floating and drifting for stationary manifestations
- `attack`: a server-triggered animation fired after a successful melee hit

Textures remain creature-specific. The Mimic retains its block-disguise rendering until it reveals, and the Almighty retains its oversized rendering treatment.

## Authoring creature-specific animation

Use Blockbench's GeckoLib model format. Keep shared animation bone names compatible (`root`, `body`, `head`, `jaw`, `spine`, `left_arm`, `right_arm`, `left_leg`, `right_leg`). Creature-specific geometry may add bones for horns, tendrils, tails, mandibles, or extra limbs. Add dedicated controllers when an animation is driven by a unique AI state, such as stalking, ceiling movement, burrowing, feinting, or a pounce. Keep all animation triggers server-authoritative and avoid client-only gameplay decisions.

## Verification checklist

- Run `./gradlew build` with Java 21.
- Launch both the client and a dedicated server with GeckoLib 4.9.4 installed.
- Spawn all 24 entity types and verify each dedicated geo rig, texture, idle, locomotion, melee attack, death, and tracking synchronization.
- Test the Mimic's block disguise and reveal, the Almighty's scale, Nightmare variants, and entity unload/reload to ensure animation state does not leak across instances.
