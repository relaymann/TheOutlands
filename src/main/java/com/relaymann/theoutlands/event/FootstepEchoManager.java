package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Rare, player-private footsteps that finish a beat after the player has stopped. */
public final class FootstepEchoManager {
    private static final Map<UUID, State> STATES = new HashMap<>();
    private FootstepEchoManager() {}

    public static void tick(ServerPlayer player) {
        if (player.level().isClientSide) return;
        ServerLevel level = player.serverLevel();
        UUID id = player.getUUID();
        State state = STATES.computeIfAbsent(id, ignored -> new State());
        long now = level.getGameTime();

        if (state.pendingAt >= 0L && now >= state.pendingAt) {
            if (state.pendingLevel == level && player.isAlive() && OutlandsConfig.HORROR_AUDIO_ENABLED.get()) {
                player.connection.send(new ClientboundSoundPacket(
                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.GRAVEL_STEP),
                        SoundSource.BLOCKS, state.x, state.y, state.z, 0.22F, 0.72F, player.getRandom().nextLong()));
            }
            state.pendingAt = -1L;
            state.pendingLevel = null;
        }

        double vx = player.getDeltaMovement().x;
        double vz = player.getDeltaMovement().z;
        boolean moving = player.onGround() && (vx * vx + vz * vz) > 0.012D;
        if (state.wasMoving && !moving && now - state.lastEchoAt >= 900L
                && player.getRandom().nextInt(9) == 0 && state.pendingAt < 0L) {
            double yaw = Math.toRadians(player.getYRot());
            state.x = player.getX() - Math.sin(yaw) * 2.8D;
            state.y = player.getY();
            state.z = player.getZ() + Math.cos(yaw) * 2.8D;
            state.pendingAt = now + 2L + player.getRandom().nextInt(3);
            state.pendingLevel = level;
            state.lastEchoAt = now;
        }
        state.wasMoving = moving;
    }

    public static void clear(ServerPlayer player) { STATES.remove(player.getUUID()); }
    public static void clear() { STATES.clear(); }

    private static final class State {
        private boolean wasMoving;
        private long lastEchoAt = Long.MIN_VALUE / 2L;
        private long pendingAt = -1L;
        private ServerLevel pendingLevel;
        private double x, y, z;
    }
}
