# Lore and horror design

This document records the current direction. Items marked **provisional** are not final canon until confirmed.

## The world

The Overworld begins as a normal vanilla Minecraft world. Vanilla biomes, terrain, structures, survival progression, Nether access, and End access remain recognizable. Horror is layered onto this world rather than replacing it with a separate dimension or prebuilt map.

The first roughly 1,000 horizontal blocks from the world's original shared spawn are intended to feel normal. Beyond that point, abnormalities become increasingly likely, noticeable, dangerous, and supernatural. There is no final horror tier: escalation continues outward toward the world's configured border.

The experience blends psychological and survival horror. It leans toward meaningful threats and faster engagement than a very slow-burn experience, but retains quiet intervals. Aggression is not constant jumpscares or nonstop attacks.

## Escalation principles

- Distance from the original shared Overworld spawn is the primary long-range pressure.
- Player history, discoveries, recent encounters, and local conditions influence event selection.
- Internal tiers are authoring tools only; they never cap content or intensity.
- Preserve vanilla normality as contrast, even in distant regions.
- Use supernatural manipulation increasingly often, but keep some events grounded and ambiguous.
- Major encounters should be memorable and consequential, not disposable random spawns.
- Multiplayer combines shared world events with player-specific perceptions and memories.
- The world remembers places the player revisits; a location can become increasingly wrong for that player without changing the actual blocks.

## The hierarchy

### The Almighty

The Almighty is the mastermind behind the events and the ultimate authority in the world. The Almighty is not merely the strongest monster: the world and its horrors are part of a deliberate design.

The final configured strip before the world's border is The Almighty's land. No other entity may enter this territory; this exclusion includes Null, Null's apostles, Fear, Death, and ordinary hostile entities. The player may enter. The Almighty waits in the borderland.

The Almighty's power includes creating a mod and inserting it into an existing world. This is how Death was made. The player reaches the final borderland, where the Almighty manifests as the endgame boss. His encounter escalates through three phases. Defeating him reveals the final artifact, `the_last_page`, and the line: “You found the seam.” The last page is a revelation fragment, not a complete explanation of the entire story.

### Null — First Apostle

Null is primarily the watcher and stalker associated with spawn. He observes, follows, and studies players, but can intervene directly and dispatch his own apostles.

Null is the only apostle besides The Almighty who commands apostles of his own. His unnamed apostles are now represented by rare, silent observer entities. They are not ordinary hostile mobs: they watch, relocate when unseen, and disappear after sustained eye contact or close approach. Their identities, number, and individual roles remain open.

**Appearance and hierarchy:** Every apostle serving Null is a completely black, featureless humanoid silhouette. They have no eyes, mouth, markings, glow, or other visible details. They share a uniform appearance rather than having individual faces or decorative variants. They are materially weaker than Null: an apostle has 18 health and no armor, while Null has 80 health and 20 armor. Apostles are mortal and are not invulnerable.

**Null's blessing:** The apostles do not see with eyes. Null grants each one a supernatural blessing that lets it sense the specific player it has been sent to observe, even without ordinary line of sight. This is how they know where to appear and how they can relocate around the player. The blessing does not make them omnipotent: they remain fragile, can be killed, and vanish if the player catches them watching for long enough or gets too close.

Null is not geographically confined to spawn. He can appear in outer regions, including millions of blocks away. His presence should be rare enough to remain unsettling, and his behavior should not be reduced to a simple chase loop. Repeated eye contact, look-backs, pauses, and flight influence his later stalking behavior. He also remembers coarse areas where a player travels, sleeps, and builds, allowing later sightings to feel personally placed rather than randomly spawned.

### Fear — Second Apostle

Fear embodies intimidation and the act of making prey experience fear. His introduction is a scripted story milestone. He teleports in front of the player and above them, looking down as if addressing someone beneath him.

Single-player introduction:

> You are the first person to reach me.
>
> You are worthy of my fear.

In multiplayer, the dialogue adapts naturally to address the group rather than treating several players as one person.

Fear controls a forest territory called **Fear**. At the full-territory milestone, existing forest biomes receive darker fog, quieter ambient/weather audio, strange mob attention, animal behavior changes, drifting ash, distant positional sounds, and occasional false structures. A separate `theoutlands:fear_forest` biome definition and worldgen modifiers are included. The vanilla Overworld biome source is deliberately not replaced, so automatic natural placement of that new biome and permanent Fear structures are not complete yet.

**Distance reconciliation:** Fear's first personal appearance occurs around 15,000 blocks; the broader/full territorial presence begins around 100,000 blocks. These are separate milestones.

### Death / death.jar — Final Apostle

Death is the final apostle beneath The Almighty. The Almighty created Death by making a mod and inserting it into the world; Death is therefore an installed presence, not a naturally occurring creature.

Death first appears around 50,000 blocks. He is a hunter and pursuer who enjoys toying with prey before devouring them. He follows the player whose trail he picked up, studies them, sometimes allows an apparent escape, and may resume the hunt if they remain within range. He does not immediately retarget other players or teleport behind a player on a predictable timer.

The meta-ARG clue sequence is discovered through three rare artifacts: `death.jar`, `manifest.txt`, and `author.dat`. The next artifact is only released after the previous one is found and Death manifests again. Defeating The Almighty reveals a final `the_last_page` artifact. The sequence is the beginning of a larger ARG, not a one-book lore dump.

Death's territory extends outward until the final configured region belonging exclusively to The Almighty.

## World geography

- **0–1,000 blocks:** ordinary vanilla baseline.
- **Beyond 1,000:** continuous escalation begins.
- **Around 15,000:** Fear's first appearance.
- **Around 22,000 and after repeated Null observations:** rare appearances of Null's unnamed apostles become eligible.
- **Around 50,000:** Death's first appearance.
- **Around 100,000:** full Fear forest-territory overlay.
- **Final configured border strip:** The Almighty's exclusive land.
- **At the borderland:** The Almighty waits.

These are story milestones, not isolated difficulty zones. Null can appear far outside spawn territory. Fear's influence can be foreshadowed before the full territory appears. Death can hunt across his territory. Horror continues to escalate between all milestones.

## ARG structure

The story should be discovered through interconnected evidence:
- Abandoned and anomalous structures.
- Books, signs, records, and hidden coordinate references.
- Repeated symbols and phrases whose meaning changes with context.
- Evidence that one discovery anticipated another.
- Environmental clues tied to the apostles.
- The death.jar revelation.
- Evidence of The Almighty's authorship and control.

Do not put the entire hierarchy or explanation in one lore dump. Let players form theories, make connections, and revisit earlier discoveries with new understanding. Current clue items are the first connected artifact chain; encrypted files, coordinate puzzles, and permanent story structures remain future expansions.

## Player experience safeguards

- Never rely on constant jumpscares.
- Give dangerous encounters readable warning signs and counterplay.
- Allow quiet periods, preparation, retreat, and recovery.
- Do not destroy player builds for cheap shock value.
- Make major events multiplayer-aware and avoid forcing every player to see identical hallucinations.
- Keep horror intensity configurable where appropriate, without undermining the intended default experience.
- Never use routine chat/actionbar narration to tell players what they just experienced.
