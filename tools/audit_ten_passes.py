#!/usr/bin/env python3
"""Run ten broad, independent integrity passes over all Outlands systems."""
from __future__ import annotations
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CHECKS = (
    ("entity registry, models, textures, sounds, and natural spawning", "tools/validate_entity_registry.py"),
    ("generated anatomy, UV atlas bounds, rig hierarchy, and all creature animations", "tools/validate_creature_anatomy.py"),
    ("creature intelligence, hearing, memory, and tactical behavior", "tools/validate_creature_intelligence.py"),
    ("world memory, atmosphere, and environmental systems", "tools/validate_batch1.py"),
    ("structure layouts, loot, lore, generation, ambience, and encounter safety", "tools/validate_structure_phase2.py"),
    ("Phase 3 behaviors, environmental presence, interior dressing, and exclusions", "tools/validate_phase3.py"),
    ("optional Simple Voice Chat integration, network privacy, and regression guards", "tools/validate_voice_integration.py"),
    ("Batch 2 survival, defenses, traps, and progression", "tools/validate_batch2.py"),
    ("Batch 3 nightmare, survival, and entity regression coverage", "tools/validate_batch3.py"),
)

def main() -> None:
    for pass_number in range(1, 11):
        print(f"\n=== OUTLANDS GENERAL INTEGRITY AUDIT {pass_number}/10 ===", flush=True)
        for description, script in CHECKS:
            print(f"--- {description} ---", flush=True)
            subprocess.run([sys.executable, str(ROOT / script)], cwd=ROOT, check=True)
    print("\nAll ten general integrity audit passes succeeded.", flush=True)

if __name__ == "__main__":
    main()
