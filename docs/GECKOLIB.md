# GeckoLib animation integration

The Outlands targets Minecraft 1.21.1 / NeoForge 21.1.266 / Java 21 and requires GeckoLib 4.9.4 or newer in the 4.9 line.

## Runtime dependency

Gradle resolves GeckoLib from the official GeckoLib Cloudsmith Maven repository. The dependency is declared as an implementation dependency and as a required NeoForge mod dependency, so clients and dedicated servers both install the matching GeckoLib NeoForge 1.21.1 jar.

## Entity architecture

All registered Outlands creatures extend `OutlandsGeoMonster`, which preserves the existing `Monster` behavior and centralizes the GeckoLib instance cache and animation controllers. Each entity keeps its own attributes, AI goals, targeting, spawn rules, sounds, and server-side mechanics.

The shared rig currently exposes these controllers:

- `locomotion`: looping idle and walk cycles, selected from GeckoLib movement state.
- `combat`: a triggerable attack animation, fired only after a successful melee hit.

All current entity renderer registrations use `OutlandsGeoRenderer`. This removes the requirement to bake vanilla model layers for these creatures. The shared geo rig is in `assets/theoutlands/geo/entity/outlands_monster.geo.json`; keyframes are in `assets/theoutlands/animations/entity/outlands_monster.animation.json`. Textures remain creature-specific wherever the project already has a dedicated texture.

## Authoring creature-specific animation

Use Blockbench's GeckoLib model format. Keep bone names compatible with the shared controller contract (`root`, `body`, `head`, `jaw`, `spine`, `left_arm`, `right_arm`, `left_leg`, `right_leg`) or add a dedicated GeoModel and renderer when a creature needs a fundamentally different rig (for example, a ceiling crawler or burrowing creature). Add new controllers to the creature's class when an animation is driven by a unique AI state. Keep all animation triggers server-authoritative and avoid client-only gameplay decisions.

## Verification checklist

- Run `./gradlew build` with Java 21.
- Launch both the client and a dedicated server with GeckoLib 4.9.4 installed.
- Spawn every entity and verify model, texture, idle, locomotion, melee attack, death, and tracking synchronization.
- Test Nightmare variants and entity unload/reload to ensure animation state does not leak across instances.
