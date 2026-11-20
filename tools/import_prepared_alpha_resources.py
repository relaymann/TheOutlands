#!/usr/bin/env python3
"""Install a prepared Alpha 1.2.6 resource archive into local generated resources.

This copies only PNG assets from the supplied archive and preserves their
assets/<namespace>/... resource paths. Output is ignored by Git so Mojang art
is never added to the public repository.
"""
from __future__ import annotations
import argparse
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/generated/resources"

def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("resource_archive", type=Path,
                        help="Prepared Alpha 1.2.6 resource ZIP")
    args = parser.parse_args()
    if not args.resource_archive.is_file() or not zipfile.is_zipfile(args.resource_archive):
        parser.error(f"Not a valid resource ZIP: {args.resource_archive}")

    copied = 0
    with zipfile.ZipFile(args.resource_archive) as archive:
        for member in archive.namelist():
            if not member.startswith("assets/") or not member.lower().endswith(".png"):
                continue
            destination = OUT / member
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_bytes(archive.read(member))
            copied += 1

    print(f"Installed {copied} Alpha 1.2.6 PNG assets into {OUT}.")
    print("The imported assets remain local generated resources and are not committed.")

if __name__ == "__main__":
    main()
