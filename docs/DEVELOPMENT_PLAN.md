# Development Plan

## Current milestone — The World Remembers

The horror director is player-specific and server-authoritative. The current milestone extends that foundation with:

- Bounded coarse location history and persistent behavioral signals (look-backs, pauses, sprinting).
- Familiar-place anomalies that only the returning player receives and that never replace or edit blocks.
- Null stalking that adapts its distance and relocation cadence to the player's remembered behavior and may reuse remembered, already-loaded locations.
- Sparse environmental cues that rely on silence, restrained particles, and quiet sound rather than explanatory chat/actionbar narration.
- Rare Almighty influence events gated by proximity to the configured world-border territory.

All memory is stored in the player's persistent data and copied across respawn. Location memory is intentionally coarse and bounded. The director must never force chunk generation or scan the world globally.

## Follow-up content milestones

- Dedicated client-side ambient audio attenuation for true localized silence.
- Actual client-only visual misdirection (brief silhouette/occlusion effects) with no server-world block changes.
- The Almighty's apostles, territory-specific environmental logic, and authored ARG clues.
- Structure and biome generation for Fear's territory, with placement and progression tests.
