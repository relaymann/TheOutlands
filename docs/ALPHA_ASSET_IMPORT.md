# Importing the Minecraft Alpha 1.2.6 assets

The supplied `client.jar.zip` was verified as Minecraft Alpha v1.2.6. The prepared `alpha_1_2_6_imported_resources.zip` contains 672 PNG resources derived from that client, including mapped modern block/entity texture paths and reference cells for the legacy atlases.

The separate `InventivetalentDev minecraft-assets 1.2.5 assets-sound.zip` contains 491 OGG files. Its own label identifies it as a Minecraft 1.2.5-era sound archive, **not** Alpha 1.2.6 audio. It is used as a classic-era sound fallback, not misrepresented as verified Alpha audio. The Alpha client archive itself contains no sound recordings.

## Import locally

Keep all three supplied archives outside Git. From the repository root:

1. Install Pillow:

   `python -m pip install Pillow`

2. Import the prepared Alpha textures and atlas references:

   `python tools/import_prepared_alpha_resources.py alpha_1_2_6_imported_resources.zip`

3. Import the original client atlas references and the 1.2.5-era sound recordings:

   `python tools/import_classic_sounds.py "InventivetalentDev minecraft-assets 1.2.5 assets-sound.zip"`

4. Build:

   `gradle --no-daemon clean test build`

The texture importer writes only to `src/generated/resources`, which is ignored by Git. The sound importer writes OGGs there and updates the mod's tracked `sounds.json` aliases. Do not commit or publish the extracted Mojang art or sound files. The procedural block-texture generator skips files already imported, so it cannot overwrite those original crops.

## What gets applied

- 40 mapped classic block textures from the 16×16 cells of Alpha's `terrain.png` atlas.
- Classic 64×32 skins for chicken, cow, creeper, ghast, pig, sheep, skeleton, slime and spider at their corresponding modern entity texture paths.
- Alpha rain/snow and sun images, plus grass and foliage colour maps, at compatible modern resource paths.
- 491 local OGG recordings copied under The Outlands' generated sound resources. The importer updates The Outlands' existing `sounds.json` definitions; it does not replace Minecraft's entire `minecraft:sounds.json`.
- Runtime sound-event redirection for selected footsteps, block hit/break, wooden/iron doors, player hurt/fall, water, swimming, lava, cave ambience, rain/thunder, and classic mob/animal calls. This can be switched with `alphaSoundsEnabled` in the common config.

## What is retained for conversion, not blindly substituted

The archive preserves every terrain atlas tile and item/particle atlas cell as reference images, along with GUI, environmental and other legacy textures. These are not automatically installed as modern one-file-per-item sprites or modern GUI screens: their atlas coordinates, layouts and rendering contracts differ. Player, zombie and pigman skins are likewise retained as references where modern UV/model layouts do not safely match.

## Terrain generation and renderer compatibility

**World generation is not changed.** No biome placement, terrain formation, cave generation, structure placement, height distribution or world-generation algorithm is replaced. Imported block textures change only how existing blocks are drawn.

The Alpha client archive has no standalone lightmap or renderer configuration. Alpha-like lighting therefore still requires a carefully implemented modern rendering adaptation; copying textures cannot recreate the old lighting algorithm. The current fog behaviour is an approximation, not a claim of pixel-perfect renderer parity.

Newer 1.21.1 content that did not exist in Alpha retains its modern models and gameplay. The procedural texture palette gives selected newer blocks a restrained Alpha-inspired appearance; a full earliest-release texture audit still needs version-specific source assets and explicit sprite mappings.
