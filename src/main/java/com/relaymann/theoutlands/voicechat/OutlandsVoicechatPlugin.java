package com.relaymann.theoutlands.voicechat;

import com.relaymann.theoutlands.client.VoiceLevelHud;
import com.relaymann.theoutlands.event.VoicePerceptionManager;
import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerStateChangedEvent;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;

/**
 * Loaded by Simple Voice Chat's plugin discovery only when its API is present.
 * The Outlands core never references this class.
 */
@ForgeVoicechatPlugin
public final class OutlandsVoicechatPlugin implements VoicechatPlugin {
    @Override
    public String getPluginId() {
        return "theoutlands";
    }

    @Override
    public void initialize(VoicechatApi api) {
        // API presence is intentionally detected through its connection event.
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
        registration.registerEvent(PlayerStateChangedEvent.class, this::onPlayerStateChanged);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            registration.registerEvent(ClientSoundEvent.class, event ->
                    VoiceLevelHud.acceptAudio(event.getRawAudio(), event.isWhispering()));
            registration.registerEvent(ClientVoicechatConnectionEvent.class,
                    event -> VoiceLevelHud.setConnected(event.isConnected()));
        }
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatConnection connection = event.getSenderConnection();
        if (connection == null) return;
        ServerPlayer player = connection.getPlayer();
        if (player != null) {
            VoicePerceptionManager.onVoicePacket(player, event.getPacket().isWhispering());
        }
    }

    private void onPlayerStateChanged(PlayerStateChangedEvent event) {
        if (event.isDisabled() || event.isDisconnected()) {
            VoicePerceptionManager.clear(event.getPlayerUuid());
        }
    }
}
