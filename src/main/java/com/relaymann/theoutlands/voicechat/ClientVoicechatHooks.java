package com.relaymann.theoutlands.voicechat;

import com.relaymann.theoutlands.client.VoiceLevelHud;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import net.minecraft.client.Minecraft;

/** Client-only Simple Voice Chat hooks, isolated from dedicated-server class loading. */
public final class ClientVoicechatHooks {
    private ClientVoicechatHooks() {}

    public static void register(EventRegistration registration) {
        registration.registerEvent(ClientSoundEvent.class, event ->
                VoiceLevelHud.acceptAudio(event.getRawAudio(), event.isWhispering()));
        registration.registerEvent(ClientVoicechatConnectionEvent.class,
                event -> Minecraft.getInstance().execute(
                        () -> VoiceLevelHud.setConnected(event.isConnected())));
    }
}
