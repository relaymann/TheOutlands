#!/usr/bin/env python3
"""Install pixel-preserved Alpha 1.2.6 PNG assets into local generated resources.

No resizing, palette conversion, filtering, or recompression is performed on the
PNG payloads. This is important for 16x16 Alpha pixel art and alpha-channel
cutouts. Output remains under src/generated/resources (gitignored).
"""
from __future__ import annotations

import argparse
import struct
import zipfile
from pathlib import Path, PurePosixPath

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/generated/resources"
EXPECTED = {
    "assets/minecraft/textures/block/": 40,
    "assets/minecraft/textures/entity/": 11,
    "assets/minecraft/textures/environment/": 3,
    "assets/minecraft/textures/colormap/": 2,
}


def png_dimensions(data: bytes) -> tuple[int, int]:
    if len(data) < 24 or data[:8] != b"\x89PNG\r\n\x1a\n" or data[12:16] != b"IHDR":
        raise ValueError("not a valid PNG with an IHDR header")
    return struct.unpack(">II", data[16:24])


def safe_member(member: str) -> bool:
    path = PurePosixPath(member)
    return (
        not path.is_absolute()
        and ".." not in path.parts
        and member.startswith("assets/")
        and member.lower().endswith(".png")
    )


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("resource_archive", type=Path, help="Prepared Alpha 1.2.6 resource ZIP")
    args = parser.parse_args()
    if not args.resource_archive.is_file() or not zipfile.is_zipfile(args.resource_archive):
        parser.error(f"Not a valid resource ZIP: {args.resource_archive}")

    counts = {prefix: 0 for prefix in EXPECTED}
    copied = 0
    references = 0
    with zipfile.ZipFile(args.resource_archive) as archive:
        for member in archive.namelist():
            if not safe_member(member):
                continue
            payload = archive.read(member)
            try:
                png_dimensions(payload)
            except ValueError as exc:
                raise SystemExit(f"Invalid PNG in archive: {member}: {exc}") from exc

            destination = OUT.joinpath(*PurePosixPath(member).parts)
            destination.parent.mkdir(parents=True, exist_ok=True)
            # Write the original PNG bytes unchanged: pixel values and alpha survive.
            destination.write_bytes(payload)
            copied += 1

            matched = False
            for prefix in counts:
                if member.startswith(prefix):
                    counts[prefix] += 1
                    matched = True
                    break
            if not matched and member.startswith("assets/theoutlands/textures/alpha_reference/"):
                references += 1

    problems = [
        f"{prefix}: expected {expected}, imported {counts[prefix]}"
        for prefix, expected in EXPECTED.items()
        if counts[prefix] != expected
    ]
    if problems:
        raise SystemExit("Alpha asset archive is incomplete or has changed:\n" + "\n".join(problems))

    print(f"Imported {copied} PNG files byte-for-byte into {OUT}.")
    print("Mapped original Alpha assets:")
    for prefix, count in counts.items():
        print(f"  {prefix}: {count}")
    print(f"  Alpha atlas/reference PNGs: {references}")
    print("No texture was resized, recoloured, filtered, or recompressed.")
    print("Generated resources remain local and are not committed to the public repository.")


if __name__ == "__main__":
    main()
