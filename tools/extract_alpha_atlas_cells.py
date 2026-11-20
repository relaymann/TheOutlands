#!/usr/bin/env python3
"""Extract every Alpha 1.2.6 atlas cell losslessly as a separate PNG.

This is a source-preserving atlas splitter, not an automatic semantic mapper.
The manifest records source coordinates and pixel hashes so modern identifiers
can be assigned explicitly without guessing from visual similarity.
"""
from __future__ import annotations
import argparse, hashlib, json, zipfile
from io import BytesIO
from pathlib import Path
from PIL import Image

ATLASES = {
    "terrain": ("terrain.png", 16, "textures/block/alpha_atlas"),
    "items": ("gui/items.png", 16, "textures/item/alpha_atlas"),
    "particles": ("particles.png", 8, "textures/particle/alpha_atlas"),
    "gui": ("gui/gui.png", 16, "textures/gui/alpha_atlas"),
}

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("client_zip", type=Path)
    parser.add_argument("--out", type=Path,
                        default=Path("src/generated/resources/assets/theoutlands"))
    args = parser.parse_args()
    if not zipfile.is_zipfile(args.client_zip):
        parser.error("client_zip must be a ZIP archive")
    manifest = {"source": "user-supplied Minecraft Alpha 1.2.6 client archive",
                "atlases": {}}
    with zipfile.ZipFile(args.client_zip) as archive:
        names = set(archive.namelist())
        for atlas, (member, cell_size, folder) in ATLASES.items():
            if member not in names:
                raise SystemExit(f"Missing atlas: {member}")
            original = archive.read(member)
            image = Image.open(BytesIO(original)).convert("RGBA")
            if image.width % cell_size or image.height % cell_size:
                raise SystemExit(f"Unexpected dimensions in {member}: {image.size}")
            columns, rows = image.width // cell_size, image.height // cell_size
            cells = []
            for y in range(rows):
                for x in range(columns):
                    crop = image.crop((x*cell_size, y*cell_size,
                                       (x+1)*cell_size, (y+1)*cell_size))
                    relative = f"{folder}/cell_{y:02d}_{x:02d}.png"
                    target = args.out / relative
                    target.parent.mkdir(parents=True, exist_ok=True)
                    crop.save(target, optimize=False)
                    cells.append({
                        "x": x, "y": y,
                        "resource": "theoutlands:" + relative.removeprefix("textures/").removesuffix(".png"),
                        "sha256_rgba": hashlib.sha256(crop.tobytes()).hexdigest()
                    })
            manifest["atlases"][atlas] = {
                "source_path": member,
                "source_sha256": hashlib.sha256(original).hexdigest(),
                "dimensions": list(image.size), "cell_size": cell_size,
                "columns": columns, "rows": rows, "cells": cells
            }
    target = args.out / "alpha_atlas_manifest.json"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    print("Extracted " + ", ".join(
        f"{name}: {len(data['cells'])} cells" for name, data in manifest["atlases"].items()))
    print(f"Coordinate manifest: {target}")
    print("Original RGBA pixels and cell dimensions are preserved; semantic names are not guessed.")

if __name__ == "__main__":
    main()
