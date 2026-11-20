#!/usr/bin/env python3
"""Generate original 16x16 Alpha-inspired block textures into Gradle generated resources.

These are newly drawn, deliberately low-detail pixel textures; no Mojang texture
files are copied or redistributed. Unlisted/newer assets retain their own vanilla
textures so their version-specific visual identity is preserved.
"""
from pathlib import Path
import random
import struct
import zlib

OUT = Path("src/generated/resources/assets/minecraft/textures/block")
OUT.mkdir(parents=True, exist_ok=True)

# Base colour, variation range, and pattern for the original Alpha-era palette.
PALETTES = {
    "stone": ((112, 112, 112), 20, "noise"),
    "cobblestone": ((105, 105, 105), 28, "cracks"),
    "dirt": ((134, 96, 67), 24, "noise"),
    "grass_block_top": ((91, 132, 54), 25, "noise"),
    "grass_block_side": ((116, 91, 60), 22, "grass"),
    "sand": ((219, 207, 145), 18, "noise"),
    "gravel": ((126, 120, 111), 34, "noise"),
    "bedrock": ((65, 65, 65), 30, "noise"),
    "oak_planks": ((156, 119, 72), 18, "wood"),
    "oak_log": ((113, 83, 49), 22, "bark"),
    "oak_log_top": ((151, 117, 72), 20, "rings"),
    "oak_leaves": ((62, 112, 43), 36, "leaves"),
    "coal_ore": ((112, 112, 112), 17, "coal"),
    "iron_ore": ((112, 112, 112), 17, "iron"),
    "gold_ore": ((112, 112, 112), 17, "gold"),
    "diamond_ore": ((112, 112, 112), 17, "diamond"),
    "redstone_ore": ((112, 112, 112), 17, "redstone"),
    "lapis_ore": ((112, 112, 112), 17, "lapis"),
    "netherrack": ((117, 55, 50), 24, "noise"),
    "obsidian": ((31, 25, 54), 18, "noise"),
    "stone_bricks": ((116, 116, 116), 17, "cracks"),
    "mossy_cobblestone": ((91, 105, 73), 27, "cracks"),
    "mossy_stone_bricks": ((100, 111, 83), 22, "cracks"),
    "bricks": ((150, 75, 57), 18, "brick"),
    "sandstone_top": ((218, 202, 145), 12, "noise"),
    "sandstone": ((203, 185, 126), 15, "brick"),
    "sandstone_bottom": ((190, 172, 119), 12, "noise"),
    "glass": ((188, 218, 222), 8, "glass"),
    "glass_pane_top": ((188, 218, 222), 8, "glass"),
    "oak_door": ((142, 101, 57), 18, "wood"),
    "oak_trapdoor": ((142, 101, 57), 18, "wood"),
    "bookshelf": ((151, 111, 66), 18, "wood"),
    "crafting_table_top": ((151, 111, 66), 18, "wood"),
    "furnace_front": ((105, 105, 105), 16, "cracks"),
    "furnace_side": ((105, 105, 105), 16, "noise"),
    "coal_block": ((37, 37, 37), 15, "noise"),
    "iron_block": ((190, 190, 190), 12, "noise"),
    "gold_block": ((229, 190, 48), 13, "noise"),
    "diamond_block": ((78, 205, 190), 13, "noise"),
    "emerald_block": ((49, 174, 91), 13, "noise"),
    "lapis_block": ((39, 67, 148), 13, "noise"),
    "redstone_block": ((157, 35, 29), 13, "noise"),
    "snow": ((235, 241, 245), 8, "noise"),
    "snow_block": ((231, 238, 242), 7, "noise"),
    "ice": ((154, 190, 210), 13, "glass"),
    "packed_ice": ((128, 166, 193), 12, "glass"),
    "clay": ((151, 158, 165), 13, "noise"),
    "terracotta": ((152, 91, 66), 14, "noise"),
    "cactus_side": ((57, 117, 49), 17, "leaves"),
    "cactus_top": ((57, 117, 49), 17, "noise"),
    "pumpkin_side": ((194, 112, 35), 18, "wood"),
    "pumpkin_top": ((194, 112, 35), 18, "noise"),
    "melon_side": ((111, 150, 57), 18, "leaves"),
    "melon_top": ((111, 150, 57), 18, "noise"),
    "netherrack": ((117, 55, 50), 24, "noise"),
    "soul_sand": ((84, 63, 48), 17, "noise"),
    "glowstone": ((190, 151, 75), 20, "noise"),
    "end_stone": ((215, 211, 151), 16, "noise"),
    "prismarine": ((70, 133, 122), 17, "cracks"),
}

ORE = {
    "coal": (30, 29, 27), "iron": (183, 145, 112),
    "gold": (235, 190, 45), "diamond": (69, 213, 198),
    "redstone": (172, 28, 24), "lapis": (38, 66, 158),
}

def clamp(v):
    return max(0, min(255, int(v)))

def png(path, pixels):
    raw = b"".join(b"\x00" + bytes(channel for pixel in row for channel in pixel)
                   for row in pixels)
    def chunk(kind, data):
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xffffffff)
    data = (b"\x89PNG\r\n\x1a\n" +
            chunk(b"IHDR", struct.pack(">2I5B", 16, 16, 8, 2, 0, 0, 0)) +
            chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))
    path.write_bytes(data)

def make(name, base, spread, pattern):
    rng = random.Random(name)
    pixels = []
    ore = ORE.get(pattern)
    for y in range(16):
        row = []
        for x in range(16):
            n = rng.randint(-spread, spread)
            color = tuple(clamp(c + n) for c in base)
            if pattern == "wood":
                n = rng.choice((-15, -8, 0, 5, 12))
                color = tuple(clamp(c + n + (7 if x % 4 == 0 else 0)) for c in base)
            elif pattern == "bark":
                n = -14 if x in (2, 7, 12) else rng.randint(-spread, spread)
                color = tuple(clamp(c + n) for c in base)
            elif pattern == "rings":
                n = 13 if (x-7)**2 + (y-7)**2 in range(5, 8) else rng.randint(-spread, spread)
                color = tuple(clamp(c + n) for c in base)
            elif pattern == "grass":
                color = tuple(clamp(c + n) for c in base)
                if y < 3:
                    color = tuple(clamp(c + (25 if y == 0 else 12)) for c in (78, 116, 43))
            elif pattern == "leaves":
                if rng.random() < .18:
                    color = (39, 83, 31)
            elif pattern == "cracks":
                if (x + y * 3) % 11 == 0:
                    color = (65, 65, 65)
            elif pattern == "brick":
                if y in (3, 8, 13) or (y // 5) % 2 == 0 and x in (0, 8) or (y // 5) % 2 == 1 and x in (4, 12):
                    color = tuple(clamp(c - 38) for c in base)
            elif pattern == "glass":
                if x == y or x == y + 1 or (x + y) % 13 == 0:
                    color = (220, 235, 238)
                else:
                    color = tuple(clamp(c - 12) for c in base)
            elif pattern in ORE:
                if rng.random() < .19:
                    color = ore
                elif rng.random() < .08:
                    color = tuple(clamp(c - 35) for c in base)
            row.append(color)
        pixels.append(row)
    return pixels

for name, (base, spread, pattern) in PALETTES.items():
    png(OUT / f"{name}.png", make(name, base, spread, pattern))
print(f"Generated {len(PALETTES)} original 16x16 Alpha-inspired block textures.")
