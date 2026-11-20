#!/usr/bin/env python3
"""Import legacy Alpha client textures from a user-supplied client JAR/ZIP.

Copyrighted source art is read from the user's local file and written only to
src/generated/resources (which is ignored by git). Never commit generated art.
Requires Pillow: python -m pip install Pillow
"""
from __future__ import annotations
import argparse
import zipfile
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/generated/resources/assets"
REF = OUT / "theoutlands/textures/alpha_reference"

# Coordinates are 16x16 cells in the legacy 256x256 terrain.png atlas.
# These are the unambiguous classic terrain cells; the entire atlas is also
# preserved as individually addressable reference tiles below.
BLOCK_TILES = {
    "stone": (0, 0), "cobblestone": (0, 1), "dirt": (2, 0),
    "grass_block_top": (3, 0),
    "oak_planks": (4, 0), "bedrock": (1, 1), "sand": (2, 1),
    "gravel": (3, 1), "oak_log": (4, 1), "oak_log_top": (5, 1),
    "bricks": (7, 0), "gold_block": (7, 1), "diamond_block": (8, 1),
    "gold_ore": (0, 2), "iron_ore": (1, 2), "coal_ore": (2, 2),
    "bookshelf": (3, 2), "mossy_cobblestone": (4, 2), "obsidian": (5, 2),
    "lapis_ore": (0, 3), "diamond_ore": (2, 3), "redstone_ore": (3, 3),
    "cobweb": (4, 3), "glass": (6, 1), "ice": (2, 4), "oak_leaves": (5, 4),
    "cactus_side": (6, 4), "cactus_top": (7, 4),
    "torch": (0, 5),
    "oak_sapling": (15, 0), "redstone_torch": (3, 6),
    "pumpkin_side": (6, 7), "netherrack": (7, 7),
    "soul_sand": (8, 7), "glowstone": (9, 7),
    "furnace_front": (13, 2), "furnace_front_on": (13, 3),
    "furnace_side": (13, 1), "crafting_table_side": (12, 3),
    "crafting_table_top": (12, 2),
}

MOB_TARGETS = {
    "chicken": "entity/chicken.png",
    "cow": "entity/cow/cow.png",
    "creeper": "entity/creeper/creeper.png",
    "ghast": "entity/ghast/ghast.png",
    "pig": "entity/pig/pig.png",
    "sheep": "entity/sheep/sheep.png",
    "sheep_fur": "entity/sheep/sheep_fur.png",
    "skeleton": "entity/skeleton/skeleton.png",
    "slime": "entity/slime/slime.png",
    "spider": "entity/spider/spider.png",
    "spider_eyes": "entity/spider_eyes.png",
}
MOBS = (
    "chicken", "cow", "creeper", "ghast", "ghast_fire", "pig", "pigman",
    "pigzombie", "sheep", "sheep_fur", "skeleton", "slime", "spider",
    "spider_eyes", "zombie", "char", "saddle",
)
ITEM_ATLASES = ("gui/items.png", "gui/gui.png", "gui/icons.png",
                "gui/inventory.png", "gui/container.png", "gui/crafting.png",
                "gui/furnace.png", "gui/background.png", "gui/logo.png")
ENVIRONMENT = ("environment/clouds.png", "environment/rain.png",
               "environment/snow.png", "terrain/sun.png", "terrain/moon.png",
               "misc/water.png", "misc/grasscolor.png",
               "misc/foliagecolor.png", "misc/vignette.png",
               "misc/pumpkinblur.png", "font/default.png")

def save_image(source: zipfile.ZipFile, member: str, destination: Path) -> bool:
    try:
        with source.open(member) as stream, Image.open(stream) as image:
            destination.parent.mkdir(parents=True, exist_ok=True)
            image.convert("RGBA").save(destination, format="PNG")
        return True
    except KeyError:
        return False

def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("client_archive", type=Path,
                        help="User-owned legacy Minecraft client JAR or ZIP")
    args = parser.parse_args()
    if not args.client_archive.is_file():
        parser.error(f"File not found: {args.client_archive}")
    if not zipfile.is_zipfile(args.client_archive):
        parser.error("Input is not a valid JAR/ZIP archive")

    with zipfile.ZipFile(args.client_archive) as jar:
        names = set(jar.namelist())
        version_marker = b"Minecraft Alpha v1.2.6"
        detected_alpha_126 = any(
            version_marker in jar.read(name)
            for name in names if name.endswith(".class")
        )
        if "terrain.png" not in names:
            parser.error("Archive has no terrain.png; this does not look like the expected legacy client")
        terrain = Image.open(jar.open("terrain.png")).convert("RGBA")
        if terrain.size != (256, 256):
            parser.error(f"Expected a 256x256 legacy terrain atlas, got {terrain.size}")

        # Preserve every cell—including unused cells—for exact visual reference.
        (REF / "terrain_tiles").mkdir(parents=True, exist_ok=True)
        for y in range(16):
            for x in range(16):
                tile = terrain.crop((x * 16, y * 16, (x + 1) * 16, (y + 1) * 16))
                tile.save(REF / "terrain_tiles" / f"{x:02d}_{y:02d}.png")

        # Direct modern block texture overrides use exact 16x16 crops.
        for name, (x, y) in BLOCK_TILES.items():
            tile = terrain.crop((x * 16, y * 16, (x + 1) * 16, (y + 1) * 16))
            dest = OUT / "minecraft/textures/block" / f"{name}.png"
            dest.parent.mkdir(parents=True, exist_ok=True)
            tile.save(dest)

        # Preserve mob skins, item atlas, UI atlases, particles and environmental
        # maps as local reference resources. Atlas images are not falsely treated
        # as individual modern item/UI sprites.
        copied = []
        for mob in MOBS:
            member = f"mob/{mob}.png"
            dest = REF / "mobs" / f"{mob}.png"
            if save_image(jar, member, dest):
                copied.append(member)
                if mob in MOB_TARGETS:
                    # These classic mob skins retain the 64x32 UV layout used by
                    # the corresponding modern entity models.
                    modern_dest = OUT / "minecraft/textures" / MOB_TARGETS[mob]
                    save_image(jar, member, modern_dest)

        # Alpha biome tint maps share the modern 256x256 colormap dimensions.
        for member, modern_name in (("misc/grasscolor.png", "grass.png"),
                                    ("misc/foliagecolor.png", "foliage.png")):
            save_image(jar, member, OUT / "minecraft/textures/colormap" / modern_name)

        for member in ITEM_ATLASES:
            dest = REF / "atlases" / Path(member).name
            if save_image(jar, member, dest):
                copied.append(member)

        for member in ENVIRONMENT:
            dest = REF / "environment" / Path(member).name
            if save_image(jar, member, dest):
                copied.append(member)

        for member, dest_name in (("particles.png", "particles.png"),
                                  ("terrain.png", "terrain.png")):
            dest = REF / "atlases" / dest_name
            if save_image(jar, member, dest):
                copied.append(member)

        # Retain the original Alpha player skin separately: its 64x32 layout
        # predates modern 64x64 player skin overlays and is not silently remapped.
        if detected_alpha_126:
            print("Verified source client: Minecraft Alpha v1.2.6.")
        else:
            print("Warning: could not verify the Alpha 1.2.6 version marker.")
        print(f"Imported {len(BLOCK_TILES)} exact legacy block crops and {len(copied)} "
              f"reference assets into {OUT}.")
        print("No audio files exist in this archive; original Alpha sounds must be supplied separately.")
        print("Generated source textures are local build inputs and must not be committed.")

if __name__ == "__main__":
    main()
