package com.relaymann.theoutlands.network;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.event.VoicePerceptionManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Carries only a normalized microphone level and whisper flag, never audio samples. */
public record VoiceLevelPayload(int level, boolean whispering) implements CustomPacketPayload {
    public static final Type<VoiceLevelPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID, "voice_level"));
    public static final StreamCodec<ByteBuf, VoiceLevelPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, VoiceLevelPayload::level,
            ByteBufCodecs.BOOL, VoiceLevelPayload::whispering,
            VoiceLevelPayload::new);

    public VoiceLevelPayload {
        level = Math.max(0, Math.min(100, level));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VoiceLevelPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || !VoicePerceptionManager.shouldAcceptLevelReport(player.getUUID())) return;
        context.enqueueWork(() -> {
            if (!player.isRemoved()) {
                VoicePerceptionManager.onClientLevel(player, payload.level(), payload.whispering());
            }
        });
    }
}
