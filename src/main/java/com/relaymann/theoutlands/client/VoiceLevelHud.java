package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.network.VoiceLevelPayload;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Client-only microphone meter. It renders above the hotbar while voice chat is connected. */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, value = Dist.CLIENT)
public final class VoiceLevelHud {
    private static volatile boolean connected;
    private static volatile int currentLevel;
    private static volatile boolean whispering;
    private static volatile long lastAudioNanos;
    private static final AtomicInteger FRAME_COUNTER = new AtomicInteger();
    private static float smoothedLevel;

    private VoiceLevelHud() {}

    public static void setConnected(boolean value) {
        connected = value;
        if (!value) {
            currentLevel = 0;
            whispering = false;
            lastAudioNanos = 0L;
            smoothedLevel = 0.0F;
            FRAME_COUNTER.set(0);
        }
    }

    /** Receives one unencoded PCM frame from the SVC client API; samples are never retained. */
    public static void acceptAudio(short[] samples, boolean isWhispering) {
        if (!connected || samples == null || samples.length == 0) return;
        int level = VoiceLevelMath.fromPcm(samples);
        currentLevel = level;
        whispering = isWhispering;
        lastAudioNanos = System.nanoTime();

        // Four PCM frames per network update is enough for responsive detection without packet spam.
        if (FRAME_COUNTER.incrementAndGet() % 4 == 0) {
            int sentLevel = level;
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.execute(() -> {
                // A queued microphone frame can outlive a world disconnect.
                if (connected && minecraft.player != null && minecraft.getConnection() != null) {
                    PacketDistributor.sendToServer(new VoiceLevelPayload(sentLevel, isWhispering));
                }
            });
        }
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!connected || minecraft.player == null || minecraft.options.hideGui) return;

        long age = lastAudioNanos == 0L ? Long.MAX_VALUE : System.nanoTime() - lastAudioNanos;
        int target = age > 180_000_000L ? 0 : currentLevel;
        smoothedLevel += (target - smoothedLevel) * 0.42F;
        int displayed = Math.max(0, Math.min(100, Math.round(smoothedLevel)));

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        int barWidth = 92;
        int x = width / 2 - barWidth / 2;
        int y = height - 48;
        var graphics = event.getGuiGraphics();

        String label = (whispering && displayed > 0 ? "WHISPER " : "VOICE ") + displayed + "%";
        int textWidth = minecraft.font.width(label);
        graphics.drawString(minecraft.font, Component.literal(label),
                width / 2 - textWidth / 2, y - 10, 0xFFE7E7E7, true);
        graphics.fill(x - 2, y - 1, x + barWidth + 2, y + 5, 0xA9000000);
        graphics.fill(x, y + 1, x + barWidth, y + 3, 0xFF393939);

        int fill = Math.round(barWidth * displayed / 100.0F);
        int color = displayed >= 75 ? 0xFFE45B4F : displayed >= 40 ? 0xFFE1B75A : 0xFF9FBF9A;
        if (fill > 0) graphics.fill(x, y + 1, x + fill, y + 3, color);
    }
}
