package com.relaymann.theoutlands.client;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import java.util.concurrent.CompletableFuture;

class ScaledSoundInstance implements SoundInstance {
    private final SoundInstance original;
    private final float scale;

    ScaledSoundInstance(SoundInstance original, float scale) {
        this.original = original;
        this.scale = Math.max(0.0F, Math.min(1.0F, scale));
    }
    @Override public ResourceLocation getLocation() { return original.getLocation(); }
    @Override public WeighedSoundEvents resolve(SoundManager manager) { return original.resolve(manager); }
    @Override public Sound getSound() { return original.getSound(); }
    @Override public SoundSource getSource() { return original.getSource(); }
    @Override public boolean isLooping() { return original.isLooping(); }
    @Override public int getDelay() { return original.getDelay(); }
    @Override public float getVolume() { return original.getVolume() * scale; }
    @Override public float getPitch() { return original.getPitch(); }
    @Override public double getX() { return original.getX(); }
    @Override public double getY() { return original.getY(); }
    @Override public double getZ() { return original.getZ(); }
    @Override public Attenuation getAttenuation() { return original.getAttenuation(); }
    @Override public boolean isRelative() { return original.isRelative(); }
    @Override public boolean canStartSilent() { return original.canStartSilent(); }
    @Override public boolean canPlaySound() { return original.canPlaySound(); }
    @Override public CompletableFuture<AudioStream> getStream(SoundBufferLibrary buffers, Sound sound, boolean looping) {
        return original.getStream(buffers, sound, looping);
    }
}
