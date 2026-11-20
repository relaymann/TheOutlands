# Importing the Minecraft Alpha 1.2.6 client assets

The supplied archive was verified as **Minecraft Alpha v1.2.6** from its embedded client version string.

The uploaded client archive contains the classic texture atlases and environment images, but **does not contain any original sound recordings**. Inspection found no OGG, WAV, MP3, AU, or MIDI assets in the archive. The sound-engine Java classes are not sound recordings.

## Local import (keep the archive private)

1. Keep your own client archive outside the Git repository (for example, `client.jar.zip` in the project root).
2. Install Pillow: `python -m pip install Pillow`.
3. From the repository root, run:

   ```bash
   python tools/import_alpha_assets.py client.jar.zip
   ```

4. Build normally:

   ```bash
   gradle --no-daemon clean test build
   ```

The importer writes to `src/generated/resources`, which is a local generated-resource directory. Do not commit the extracted Mojang art or upload it to the public repository. The normal procedural texture generator now leaves imported block textures untouched and only fills missing fallback textures.

## What is imported

- Exact 16x16 crops from the legacy `terrain.png` atlas for the mapped classic blocks, installed as modern block texture overrides.
- Every cell of the terrain atlas as a reference tile, including cells that are not yet mapped to a modern block name.
- Compatible 64x32 classic skins for chicken, cow, creeper, ghast, pig, sheep, skeleton, slime and spider are installed at the matching modern entity texture paths. The original skin files are also retained as references.\n- Original item/UI atlases, particles, clouds, weather, sun/moon, water and biome colour maps are retained as local reference assets; the grass and foliage colour maps are applied to modern biome tint paths.

The item and GUI images are retained as original atlases, not falsely presented as modern one-file-per-item sprites. Legacy player, zombie and pigman skins are retained as references because their modern UV/model layouts are not a safe direct replacement. More mapping work is needed for full automatic conversion of every item, particle and screen.

## Sound limitation

This archive has **zero audio files**. To reproduce Alpha footsteps, mining, animal calls, doors, damage, water and ambient sounds, provide the corresponding original sound files separately (or another client archive that actually contains them). The current modern vanilla sounds and The Outlands' generated creature sounds are not Alpha recordings.

## Lighting and fog limitation

The archive contains clouds, sun, moon, water and biome-colour images, but no standalone Alpha lightmap or executable renderer configuration. Exact Alpha lighting requires recreating the old light calculation/rendering behaviour in the modern client, not merely copying a PNG. The current fog update is a compatibility-oriented approximation; it is not claimed to be a pixel-perfect port of Alpha's renderer.

## Modern compatibility

The importer targets Minecraft 1.21.1 resource paths while retaining original source images. Some historical models, atlases, UV layouts and gameplay systems changed substantially after Alpha. Those need explicit conversion or client-side compatibility code; they are not safely interchangeable by renaming files.
