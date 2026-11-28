#!/usr/bin/env python3
from __future__ import annotations
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CHECKS = (
    ("entity registry, models, textures, sounds, and spawn rules", "tools/validate_entity_registry.py"),
    ("structures, layout selection, loot tables, placement, and worldgen safety", "tools/validate_structure_phase2.py"),
    ("entity behavior, environmental presence, interiors, and design exclusions", "tools/validate_phase3.py"),
    ("optional voice integration, network privacy, and client/server separation", "tools/validate_voice_integration.py"),
    ("persistent world memory, traces, ambience, and ecosystem interactions", "tools/validate_batch1.py"),
    ("survival defenses, equipment, Nightmare expansion, and resource assets", "tools/validate_batch3.py"),
)

def main() -> None:
    for number in range(1, 6):
        print(f"\n=== FULL PROJECT AUDIT {number}/5 ===", flush=True)
        for description, script in CHECKS:
            print(f"--- {description} ---", flush=True)
            subprocess.run([sys.executable, str(ROOT / script)], cwd=ROOT, check=True)
    print("\nAll five full-project audit passes succeeded.", flush=True)

if __name__ == "__main__":
    main()
