package com.relaymann.theoutlands.voicechat;

import com.relaymann.theoutlands.event.VoicePerceptionManager;
import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerStateChangedEvent;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;

@ForgeVoicechatPlugin
public final class OutlandsVoicechatPlugin implements VoicechatPlugin {
    @Override
    public String getPluginId() {
        return "theoutlands";
    }

    @Override
    public void initialize(VoicechatApi api) {

    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
        registration.registerEvent(PlayerStateChangedEvent.class, this::onPlayerStateChanged);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientVoicechatHooks.register(registration);
        }
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatConnection connection = event.getSenderConnection();
        if (connection == null) return;
        Object rawPlayer = connection.getPlayer().getPlayer();
        if (rawPlayer instanceof ServerPlayer player && player.getServer() != null) {
            if (!VoicePerceptionManager.shouldAcceptVoicePacket(player.getUUID())) return;
            boolean whispering = event.getPacket().isWhispering();
            player.getServer().execute(() -> {
                if (!player.isRemoved() && player.isAlive()) {
                    VoicePerceptionManager.onVoicePacket(player, whispering);
                }
            });
        }
    }

    private void onPlayerStateChanged(PlayerStateChangedEvent event) {
        if (event.isDisabled() || event.isDisconnected()) {
            VoicePerceptionManager.clear(event.getPlayerUuid());
        }
    }
}
