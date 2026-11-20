# Importing the Minecraft Alpha 1.2.6 assets

The supplied `client.jar.zip` was verified as Minecraft Alpha v1.2.6. The prepared `alpha_1_2_6_imported_resources.zip` contains 672 PNG resources derived from that client, including 40 mapped modern block textures, 11 mapped entity textures, 3 environmental textures, 2 biome colour maps, and the original atlas/reference images.

The separate `InventivetalentDev minecraft-assets 1.2.5 assets-sound.zip` contains 491 OGG files. Its own label identifies it as a Minecraft 1.2.5-era sound archive, **not** Alpha 1.2.6 audio. It is used as a classic-era sound fallback, not misrepresented as verified Alpha audio. The Alpha client archive itself contains no sound recordings.

## Import locally

Keep all three supplied archives outside Git. From the repository root:

1. Install Pillow for the atlas splitter and legacy client importer:

   `python -m pip install Pillow`

2. Import the prepared Alpha textures and atlas references:

   `python tools/import_prepared_alpha_resources.py alpha_1_2_6_imported_resources.zip`

3. Split all four original Alpha atlases into individually addressable RGBA cell textures and write a coordinate/hash manifest:

   `python tools/extract_alpha_atlas_cells.py client.jar.zip`

   This extracts 256 terrain cells (16×16), 256 item cells (16×16), 256 particle cells (8×8), and 256 GUI cells (16×16). Cells keep their original pixel values. Their names are coordinate-based until a verified semantic mapping is assigned; no block/item is guessed from appearance.

4. Import the 1.2.5-era sound recordings:

   `python tools/import_classic_sounds.py "InventivetalentDev minecraft-assets 1.2.5 assets-sound.zip"`

5. Build:

   `gradle --no-daemon clean test build`

The texture importer validates the expected mapped asset counts and PNG headers, then copies the original PNG bytes unchanged. It does not resize, recolour, filter, or recompress the files. The importer writes only to `src/generated/resources`, which is ignored by Git. The sound importer writes OGGs there and updates the mod's tracked `sounds.json` aliases. Do not commit or publish the extracted Mojang art or sound files.

## What is applied exactly

- **40 mapped Alpha block textures** at their corresponding modern resource paths. The 16×16 pixel art, colours and alpha are retained exactly from the supplied Alpha-derived PNGs.
- **11 mapped classic entity textures** for chicken, cow, creeper, ghast, pig, sheep, sheep wool, skeleton, slime and spider assets. These are copied without image conversion; entity-model UV compatibility is still checked separately by the target renderer/model.
- **3 environmental textures** (rain, snow and sun) and **2 biome colour maps** (grass and foliage) at compatible modern resource paths.
- All 1,024 atlas cells can now be extracted as separate, lossless RGBA PNGs: terrain, item, particle and GUI atlases. A manifest records each source coordinate and a SHA-256 hash of the decoded pixel data. This is a complete cell extraction, not yet a complete semantic map to every modern registry identifier.
- **491 local classic-era OGG recordings** are copied under The Outlands' generated sound resources. The importer updates The Outlands' existing `sounds.json` definitions; it does not replace Minecraft's entire `minecraft:sounds.json`.
- Runtime sound-event redirection covers selected footsteps, block hit/break, wooden/iron doors, player hurt/fall, water, swimming, lava, cave ambience, rain/thunder, and classic mob/animal calls. This can be switched with `alphaSoundsEnabled` in the common config.

## What is not yet pixel-perfect

The 40 block paths and the other mapped PNGs above use the actual supplied Alpha 1.2.6 image data, not procedural redraws. The procedural generator remains only a fallback for selected block paths that are not included in the mapped set; those fallback textures are Alpha-inspired, **not** authentic Alpha pixels.

The atlas splitter preserves item, particle and GUI cells individually, but it does not yet wire every cell to a modern one-file-per-item sprite or replace modern screen layouts. Modern GUI screens have different rendering contracts; a faithful conversion needs screen-by-screen layout recreation and explicit mappings for each relevant atlas cell. Player, zombie and pigman skins are also retained as references where modern UV/model layouts do not safely match.

The classic sound pack is not verified Alpha audio. Some sound event families also differ between 1.2.5 and Alpha 1.2.6; a truly 1:1 sound pass requires authentic Alpha recordings or a verified Alpha sound archive, plus a per-event comparison in game.

## Terrain generation and renderer compatibility

**World generation is not changed.** No biome placement, terrain formation, cave generation, structure placement, height distribution or world-generation algorithm is replaced. Imported block textures change only how existing blocks are drawn.

The current fog handler is still a sky-colour blend and fog-plane scale, not Alpha's original renderer. The client archive does not provide a standalone lightmap algorithm/configuration that can simply be copied. Exact adaptation needs a measured lightmap/brightness curve, day/night sampling, weather handling, and controlled render tests. No world-generation code is changed.

Newer 1.21.1 content that did not exist in Alpha retains its modern models and gameplay. The intended rule is to use the earliest-release-era art available for newer content, but a complete version-by-version sprite audit remains outstanding.
