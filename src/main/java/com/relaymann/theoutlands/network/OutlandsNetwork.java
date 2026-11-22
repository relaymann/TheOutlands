package com.relaymann.theoutlands.network;

import com.relaymann.theoutlands.TheOutlands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Registers the small client-to-server voice intensity signal. */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class OutlandsNetwork {
    private OutlandsNetwork() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                VoiceLevelPayload.TYPE, VoiceLevelPayload.STREAM_CODEC, VoiceLevelPayload::handle);
    }
}
