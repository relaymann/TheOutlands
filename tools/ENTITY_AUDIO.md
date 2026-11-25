# Original entity audio

The Outlands audio pipeline synthesizes original, non-verbal entity and structure cues with `generate_entity_audio.py`. It uses deterministic procedural noise, tonal layers, transient impacts, and per-creature envelopes; it does not use external recordings or copyrighted samples.

The generated palette covers quiet presence cues, breathing, movement/footstep textures, stalking clicks, pounces/lunges, burrowing and emergence, disguise reveals, stone impacts, territorial calls, Nightmare identities, and the structure ambience set. The Hunter has a dedicated pounce snap/rush sound, while existing creature events use their own sound hooks where appropriate.

Run from the repository root with Python 3 and FFmpeg installed:

```bash
python3 tools/generate_entity_audio.py
```

The script writes mono OGG Vorbis assets into `src/main/resources/assets/theoutlands/sounds/`. The matching registry entries live in `OutlandsSounds.java`, and playback definitions live in `sounds.json`. Creature movement tells are low-volume and irregular; the `HORROR_AUDIO_ENABLED` option disables those entity cues. Pilgrim and Colossus retain their own dedicated footstep cadence to avoid duplicate generic movement audio.

GitHub Actions installs FFmpeg and runs this generator before Gradle packages the mod, so generated OGGs are included in the distributable JAR. The generated files are reproducible from the checked-in script.
