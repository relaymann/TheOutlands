# Original entity audio

The seven original non-verbal effects for Cave.jar, Stalker, Observer, Stillborn Statue, and Agitated One are synthesized by `generate_entity_audio.py`.

Run from the repository root with Python 3 and FFmpeg installed:

```bash
python3 tools/generate_entity_audio.py
```

The script writes the OGG Vorbis assets into `src/main/resources/assets/theoutlands/sounds/`. The GitHub Actions build installs FFmpeg and runs this generator before Gradle packages the mod, so the rolling release contains the generated sounds. No external audio samples or copyrighted recordings are used.
