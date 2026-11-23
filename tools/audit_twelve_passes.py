#!/usr/bin/env python3
"""Run twelve complete independent project audits across Batches 1-3."""
from __future__ import annotations
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CHECKS = (
    ("entity registration, attributes, spawning, models, textures, and audio", "tools/validate_entity_registry.py"),
    ("all structure layouts, loot, placement, clipping, encounters, and ambience", "tools/validate_structure_phase2.py"),
    ("entity AI, behavior, and environmental presence", "tools/validate_phase3.py"),
    ("optional voice-chat integration, privacy, and side isolation", "tools/validate_voice_integration.py"),
    ("persistent memory, traces, atmosphere, and ecosystem", "tools/validate_batch1.py"),
    ("investigation tools, exploration sites, and mystery constraints", "tools/validate_batch2.py"),
    ("survival defenses, equipment, and Nightmare expansion", "tools/validate_batch3.py"),
)

def main() -> None:
    for pass_number in range(1, 13):
        print(f"\n=== COMPLETE OUTLANDS PROJECT AUDIT {pass_number}/12 ===", flush=True)
        for description, script in CHECKS:
            print(f"--- {description} ---", flush=True)
            subprocess.run([sys.executable, str(ROOT / script)], cwd=ROOT, check=True)
    print("\nAll twelve complete project audit passes succeeded.", flush=True)

if __name__ == "__main__":
    main()
