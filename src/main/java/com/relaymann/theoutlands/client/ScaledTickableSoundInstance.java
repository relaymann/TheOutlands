package com.relaymann.theoutlands.client;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;

final class ScaledTickableSoundInstance extends ScaledSoundInstance implements TickableSoundInstance {
    private final TickableSoundInstance original;

    ScaledTickableSoundInstance(TickableSoundInstance original, float scale) {
        super(original, scale);
        this.original = original;
    }

    @Override public void tick() { original.tick(); }
    @Override public boolean isStopped() { return original.isStopped(); }
}
