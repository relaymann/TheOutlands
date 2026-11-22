#!/usr/bin/env python3
"""Run ten independent repeat passes of the entity and structure integrity audits."""
from __future__ import annotations
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CHECKS = (
    ("entity registry, models, textures, sounds, and natural spawning", "tools/validate_entity_registry.py"),
    ("structure layouts, loot, lore, generation, ambience, and encounter safety", "tools/validate_structure_phase2.py"),
    ("Phase 3 behaviors, environmental presence, interior dressing, and exclusions", "tools/validate_phase3.py"),
    ("optional Simple Voice Chat integration, network privacy, and regression guards", "tools/validate_voice_integration.py"),
)

def main() -> None:
    for pass_number in range(1, 11):
        print(f"\n=== OUTLANDS INTEGRITY AUDIT {pass_number}/10 ===", flush=True)
        for description, script in CHECKS:
            print(f"--- {description} ---", flush=True)
            subprocess.run([sys.executable, str(ROOT / script)], cwd=ROOT, check=True)
    print("\nAll ten integrity audit passes succeeded.", flush=True)

if __name__ == "__main__":
    main()
