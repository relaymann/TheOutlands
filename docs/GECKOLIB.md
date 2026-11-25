# GeckoLib animation integration

The Outlands targets Minecraft 1.21.1 / NeoForge 21.1.266 / Java 21 and requires GeckoLib 4.9.4 or newer within major version 4.

## Runtime dependency

Gradle resolves GeckoLib from the official GeckoLib Cloudsmith Maven repository. GeckoLib is declared as a required NeoForge mod dependency, so clients and dedicated servers must install the matching GeckoLib NeoForge 1.21.1 jar.

## Entity architecture

All 24 registered Outlands creature types use the shared `OutlandsGeoMonster` animation host. It preserves the existing `Monster` behavior and centralizes the GeckoLib instance cache and controllers. Each entity retains its own attributes, AI goals, targeting, spawn rules, sounds, and server-side mechanics.

The GeckoLib renderers use a dedicated geometry file for every creature under `assets/theoutlands/geo/entity/`, including all five Nightmare variants. `OutlandsGeoModel` selects each creature's geometry and texture while sharing the animation resource. The Mimic retains its block-disguise rendering until it reveals, and the Almighty retains its oversized rendering treatment.

## Individual creature behavior

The model applies client-side head/gaze tracking using GeckoLib's entity model data, layered additively over the authored head keyframes. Watchers have a wider, slower-looking tracking range; other creatures use a more restrained range. Stalker, Death, and the False Survivor also receive tiny jaw micro-movements to make their idle faces feel less static.

This is head-and-gaze-line tracking, not independently animated eyeball geometry. The current shared creature rigs have no separate eye bones; independent pupils/eyeballs require a dedicated facial-geometry and texture pass rather than invisible placeholder bones.

## Animation system

The shared animation file contains 29 clips:

- **Locomotion and idle:** `idle`, `walk`, `crawl`, `heavy_walk`, `hover`, `stalk_idle`, `watch_idle`, `statue_idle`, `sprint`, `crawl_sprint`
- **Shared actions:** `attack`, `scream`, `lunge`, `pounce`, `reveal`, `burrow`, `emerge`, `drop`, `ground_slam`, `stalk`, `death`
- **Creature-specific attacks:** `attack_stalker`, `attack_bite`, `attack_heavy`, `attack_sweep`
- **Creature-specific deaths:** `death_stalker`, `death_heavy`, `death_crawler`, `death_watcher`

Successful melee hits select an attack style by creature family: watchful stalkers use a delayed claw strike, crawlers and biters snap forward, heavy entities use a weighty overhead blow, and pouncing hunters use a sweeping strike. Death poses are also selected by archetype: stalkers/watcher entities settle into an unnervingly crooked stillness, heavy entities collapse, and crawlers fold down low.

The synchronized action controller still handles the existing AI events. AI hooks are server-authoritative and synchronize the resulting action animation to tracking clients. Gaze tracking and facial micro-motion are client-side presentation only.

These clips do not change attack timing, movement, hitboxes, damage, spawn rules, or AI authority. Attack animations still play only after a real melee hit succeeds.

## Authoring creature-specific animation

Use Blockbench's GeckoLib model format. Keep shared animation bone names compatible: `root`, `body`, `head`, `jaw`, `spine`, `left_arm`, `right_arm`, `left_leg`, and `right_leg`. Creature-specific geometry may add bones for horns, tendrils, tails, mandibles, eyes, or extra limbs.

When adding or changing a clip:
1. Add the clip to `outlands_monster.animation.json`.
2. Use only bone names present in every rig that will play that shared clip.
3. Add unique rig bones to every applicable geometry file, or keep the animation on the shared rig.
4. Add a synchronized AI trigger only when the corresponding gameplay event already exists.
5. Keep animation presentation separate from gameplay timing, hitboxes, and server authority.

## Verification checklist

Automated validation checks all 24 entity registrations, dedicated geometry files, required shared bones, every required animation clip, every animated bone reference against every creature rig, the synchronized AI hooks, and the gaze/facial animation hooks.

- Run `./gradlew clean test build` with Java 21.
- Verify the packaged JAR contains every geometry, texture, and animation resource.
- Launch both the client and a dedicated server with GeckoLib 4.9.4 installed.
- Spawn all 24 entity types and verify each dedicated geo rig, texture, gaze tracking, idle, locomotion, creature-specific attack, death, and tracking synchronization.
- Test the Mimic's block disguise and reveal, the Almighty's scale, Nightmare variants, and entity unload/reload to ensure animation state does not leak across instances.

The automated build does not replace in-game visual testing; client and dedicated-server rendering checks remain manual.
