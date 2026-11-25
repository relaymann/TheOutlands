#!/usr/bin/env python3
"""Run ten repeated audits of non-model Outlands gameplay systems."""
from __future__ import annotations
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CHECKS = (
    ("creature intelligence and behavior", "tools/validate_creature_intelligence.py"),
    ("world memory and atmosphere", "tools/validate_batch1.py"),
    ("survival and defense systems", "tools/validate_batch2.py"),
    ("Nightmare progression and survival assets", "tools/validate_batch3.py"),
    ("entity behavior and environmental presence", "tools/validate_phase3.py"),
    ("structure generation, loot, ambience, and encounters", "tools/validate_structure_phase2.py"),
    ("optional voice-chat integration and privacy", "tools/validate_voice_integration.py"),
    ("trap cooldowns and encounter-resolution regressions", "tools/validate_non_model_regressions.py"),
)

def main() -> None:
    for pass_number in range(1, 11):
        print(f"\n=== NON-MODEL OUTLANDS AUDIT {pass_number}/10 ===", flush=True)
        for description, script in CHECKS:
            print(f"--- {description} ---", flush=True)
            subprocess.run([sys.executable, str(ROOT / script)], cwd=ROOT, check=True)
    print("\nAll ten non-model audit passes succeeded.", flush=True)

if __name__ == "__main__":
    main()
