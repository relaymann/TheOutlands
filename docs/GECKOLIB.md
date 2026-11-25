# GeckoLib animation integration

The Outlands targets Minecraft 1.21.1 / NeoForge 21.1.266 / Java 21 and requires GeckoLib 4.9.4 or newer within major version 4.

## Runtime dependency

Gradle resolves GeckoLib from the official GeckoLib Cloudsmith Maven repository. GeckoLib is declared as a required NeoForge mod dependency, so clients and dedicated servers must install the matching GeckoLib NeoForge 1.21.1 jar.

## Entity architecture

All 24 registered Outlands creature types use the shared `OutlandsGeoMonster` animation host. It preserves the existing `Monster` behavior and centralizes the GeckoLib instance cache and controllers. Each entity retains its own attributes, AI goals, targeting, spawn rules, sounds, and server-side mechanics.

The GeckoLib renderers use a dedicated geometry file for every creature under `assets/theoutlands/geo/entity/`, including all five Nightmare variants. `OutlandsGeoModel` selects each creature's geometry and texture while sharing the animation resource. Every creature rig now includes independently animated `eye_left`, `eye_right`, `pupil_left`, and `pupil_right` bones, with dedicated eye/pupil tiles generated into each creature's texture atlas. The Mimic retains its block-disguise rendering until it reveals, and the Almighty retains its oversized rendering treatment.

## Individual creature behavior

The model applies client-side head, eye-shell, and pupil tracking using GeckoLib entity model data. The eye shells move more cautiously than the pupils, so a creature can keep its head still while its eyes shift toward a player. Watchers have a wider tracking range; other creatures use restrained limits. Stalker, Death, and the False Survivor also receive tiny jaw micro-movements.

Eye geometry is real, textured geometry: every creature rig has separate eye-shell and pupil bones, and each texture atlas receives dedicated eye and pupil tiles. The render-side tracking is presentation-only and does not change server-side targeting or combat.

## Animation system

The shared animation file contains **147 clips**:

- **Shared horror actions:** scream, lunge, pounce, reveal, burrow, emerge, drop, ground slam, and stalk, plus common fallback clips
- **Creature-specific idle and locomotion:** 24 unique idle clips, 24 unique walk gaits, and 24 unique sprint gaits
- **Creature-specific combat and death:** 24 unique attack sequences and 24 unique death sequences

The individual gaits use each creature's silhouette and archetype: crawlers stay low, heavy entities brace and drive their weight forward, stalkers use uneven shoulder/head movement, and the Nightmare variants have separate pacing. Idle clips also animate the eye shells and pupils so they make small independent saccades. Attack and death clips use creature-specific timing, head/jaw movement, arm poses, and collapse styles.

Successful melee hits select the matching creature-specific attack. The attack animation is still presentation-only and is triggered only after a real hit succeeds. Death poses are synchronized by the server. Existing special action hooks remain in place for Howler calls, Mimic reveals, Burrower burrows/emergence, Ceiling Dweller drops, Dredge lunges, Hunter pounces, Colossus ground slams, and Lurker stalking.

The shared locomotion controller selects the creature's own idle, walk, or sprint clip. Horror movement cues are intentionally low-volume and irregular; existing custom footsteps for Pilgrim and Colossus remain separate to avoid duplicate sounds.
## Unique creature anatomy generation

Blockbench source rigs remain under `src/main/resources/assets/theoutlands/geo/entity/`. The build runs `tools/generate_creature_anatomy.py` and writes anatomy overlays into `src/generated/resources`, which takes resource precedence without destructively rewriting the source rigs. The Gradle `processResources` task depends on this generator, so local builds and CI package the same generated models.

The generator uses different anatomical families and per-creature additions rather than one humanoid template:
- Humanoid stalkers and survivors gain brow plates, upper fangs, hand claws, and neck/spinal tendons.
- Predators gain muzzles, tusks, articulated mandibles, foreclaws, and dorsal spines.
- Burrowers and jar creatures gain paired mandibles, feelers, carapace ridges, digging claws, and shell ribs.
- Heavy creatures gain brow armor, tusks, knuckle plates, shoulder tendons, and sternum keels.
- Eldritch creatures gain asymmetric crowns, split mandibles, hanging tendrils, floating ribs, and optic stalks.

Every anatomy bone is keyed into that creature's own idle, walk, sprint, attack, and death clip. Facial plates, jaws, claws, tendons, and feelers therefore move with the creature instead of remaining static decorations. The overlay preserves existing entity hitboxes, attributes, AI, and combat timing; it is purely visual.

Run `python3 tools/generate_creature_anatomy.py` to regenerate the overlay and `python3 tools/validate_creature_anatomy.py` to audit all 24 rigs and 120 articulated clips.

## Authoring creature-specific animation

Use Blockbench's GeckoLib model format. Keep shared animation bone names compatible: `root`, `body`, `head`, `jaw`, `spine`, `left_arm`, `right_arm`, `left_leg`, `right_leg`, `eye_left`, `eye_right`, `pupil_left`, and `pupil_right`. Creature-specific geometry may add bones for horns, tendrils, tails, mandibles, eyes, or extra limbs.

When adding or changing a clip:
1. Add the clip to `outlands_monster.animation.json`.
2. Use only bone names present in every rig that will play that shared clip.
3. Add unique rig bones to every applicable geometry file, or keep the animation on the shared rig.
4. Add a synchronized AI trigger only when the corresponding gameplay event already exists.
5. Keep animation presentation separate from gameplay timing, hitboxes, and server authority.

## Verification checklist

Automated validation checks all 24 entity registrations, dedicated geometry files, required shared bones, every required animation clip and every eye/pupil bone, every animated bone reference against every creature rig, the synchronized AI hooks, and the gaze/facial animation hooks.

- Run `./gradlew clean test build` with Java 21.
- Verify the packaged JAR contains every geometry, texture, and animation resource.
- Launch both the client and a dedicated server with GeckoLib 4.9.4 installed.
- Spawn all 24 entity types and verify each dedicated geo rig, texture, gaze tracking, idle, locomotion, creature-specific attack, death, and tracking synchronization.
- Test the Mimic's block disguise and reveal, the Almighty's scale, Nightmare variants, and entity unload/reload to ensure animation state does not leak across instances.

The automated build does not replace in-game visual testing; client and dedicated-server rendering checks remain manual.
