#!/usr/bin/env python3
"""Guard the optional Simple Voice Chat integration against packaging and privacy regressions."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")

def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"VOICE INTEGRATION AUDIT FAILED: {message}")

def main() -> None:
    build = read("build.gradle")
    metadata = read("src/main/templates/META-INF/neoforge.mods.toml")
    entry = read("src/main/java/com/relaymann/theoutlands/TheOutlands.java")
    plugin = read("src/main/java/com/relaymann/theoutlands/voicechat/OutlandsVoicechatPlugin.java")
    hooks = read("src/main/java/com/relaymann/theoutlands/voicechat/ClientVoicechatHooks.java")
    payload = read("src/main/java/com/relaymann/theoutlands/network/VoiceLevelPayload.java")
    hud = read("src/main/java/com/relaymann/theoutlands/client/VoiceLevelHud.java")
    perception = read("src/main/java/com/relaymann/theoutlands/event/VoicePerceptionManager.java")
    events = read("src/main/java/com/relaymann/theoutlands/event/ServerHorrorEvents.java")

    require('compileOnly "de.maxhenkel.voicechat:voicechat-api:' in build,
            "voice API must remain compile-only")
    require('localRuntime "maven.modrinth:simple-voice-chat:' in build,
            "Simple Voice Chat must be available in development runtime")
    require('modId="voicechat_api"\ntype="optional"' in metadata,
            "voicechat_api must remain an optional NeoForge dependency")
    require("de.maxhenkel.voicechat" not in entry,
            "the core mod entry point must not load optional API classes")
    require("@ForgeVoicechatPlugin" in plugin and "connection.getPlayer().getPlayer()" in plugin,
            "voice plugin discovery and API-to-Minecraft player conversion must be present")
    require("net.minecraft.client" not in plugin and "FMLEnvironment.dist == Dist.CLIENT" in plugin,
            "client classes must stay isolated from dedicated-server plugin loading")
    require("ClientSoundEvent" in hooks and "VoiceLevelHud.acceptAudio" in hooks,
            "client microphone activity must feed the local meter")
    require("record VoiceLevelPayload(int level, boolean whispering)" in payload,
            "custom network payload may contain only normalized level and whisper state")
    require("short[]" not in payload and "byte[]" not in payload and "short[]" not in perception,
            "raw microphone audio must never enter server payloads or perception memory")
    require("minecraft.getConnection() != null" in hud and "connected && minecraft.player != null" in hud,
            "queued meter packets must be guarded against world disconnects")
    require("VoicePerceptionManager.clear(player.getUUID())" in events,
            "voice perception state must be cleared on player logout")
    require((ROOT / "src/test/java/com/relaymann/theoutlands/client/VoiceLevelMathTest.java").is_file(),
            "microphone level tests must be present")
    require("shouldAcceptLevelReport" in perception and "normalizeLevel" in perception,
            "client meter reports must be rate-limited and whisper-capped")
    require((ROOT / "src/test/java/com/relaymann/theoutlands/event/VoiceHearingMathTest.java").is_file(),
            "hearing range tests must be present")
    print("Optional Simple Voice Chat integration checks passed.")

if __name__ == "__main__":
    main()
