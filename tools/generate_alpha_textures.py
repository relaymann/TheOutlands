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
