package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Rare, diegetic environmental cues. These are sent only to the affected player:
 * no chat narration, entity spawning, forced movement, block edits, or fake terrain.
 */
public final class EnvironmentalAnomalyManager {
    private static final int CHECK_INTERVAL = 20;
    private static final int BASE_CHANCE_DENOMINATOR = 240;
    private static final long MIN_COOLDOWN = 6_000L;
    private static final long COOLDOWN_VARIANCE = 4_001L;
    private static final Map<UUID, Long> NEXT_ALLOWED = new HashMap<>();

    private EnvironmentalAnomalyManager() {}

    public static void tick(ServerLevel level) {
        if (!level.dimension().equals(Level.OVERWORLD)
                || !OutlandsConfig.AMBIENT_ANOMALIES_ENABLED.get()
                || !OutlandsConfig.HORROR_AUDIO_ENABLED.get()
                || level.getGameTime() % CHECK_INTERVAL != 0L) return;

        long now = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            if (!eligible(player) || !farEnoughFromSpawn(level, player)) continue;

            long nextAllowed = NEXT_ALLOWED.getOrDefault(player.getUUID(), 0L);
            if (now < nextAllowed) continue;

            int denominator = Math.max(80, (int) Math.round(
                    BASE_CHANCE_DENOMINATOR / OutlandsConfig.EVENT_FREQUENCY.get()));
            if (player.getRandom().nextInt(denominator) != 0) continue;

            playOne(player);
            NEXT_ALLOWED.put(player.getUUID(), now + MIN_COOLDOWN
                    + player.getRandom().nextInt((int) COOLDOWN_VARIANCE));
        }
    }

    private static boolean eligible(ServerPlayer player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator()
                && !player.isSleeping() && !player.isPassenger() && !player.isInWaterOrBubble();
    }

    private static boolean farEnoughFromSpawn(ServerLevel level, ServerPlayer player) {
        BlockPos spawn = level.getSharedSpawnPos();
        double dx = player.getX() - (spawn.getX() + 0.5D);
        double dz = player.getZ() - (spawn.getZ() + 0.5D);
        double minimum = OutlandsConfig.AMBIENT_ANOMALY_MIN_DISTANCE.get();
        return dx * dx + dz * dz >= minimum * minimum;
    }

    private static void playOne(ServerPlayer player) {
        double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
        double distance = 12.0D + player.getRandom().nextDouble() * 14.0D;
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        double y = player.getY();
        switch (player.getRandom().nextInt(7)) {
            case 0 -> {
                // A short pair of steps, far enough away to invite a look without starting a chase.
                send(player, SoundEvents.GRAVEL_STEP, SoundSource.BLOCKS, x, y, z,
                        0.16F, 0.62F + player.getRandom().nextFloat() * 0.22F);
                send(player, SoundEvents.GRASS_STEP, SoundSource.BLOCKS, x + 1.15D, y, z + 0.7D,
                        0.12F, 0.7F);
            }
            case 1 -> send(player, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS,
                    x, y, z, 0.12F, 0.58F + player.getRandom().nextFloat() * 0.18F);
            case 2 -> {
                double behindX = player.getX() - Math.sin(Math.toRadians(player.getYRot())) * 3.2D;
                double behindZ = player.getZ() + Math.cos(Math.toRadians(player.getYRot())) * 3.2D;
                send(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT,
                        behindX, player.getY() + 1.0D, behindZ, 0.10F, 0.48F);
            }
            case 3 -> send(player, OutlandsSounds.STALKER_BREATH.get(), SoundSource.AMBIENT,
                    x, y + 1.0D, z, 0.075F, 0.62F + player.getRandom().nextFloat() * 0.16F);
            case 4 -> {
                // A faint scrape several blocks above the listener, most effective in caves.
                double aboveY = Math.min(player.level().getMaxBuildHeight() - 2.0D, player.getY() + 5.0D);
                send(player, SoundEvents.GRAVEL_STEP, SoundSource.BLOCKS,
                        x, aboveY, z, 0.11F, 0.48F);
                send(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT,
                        x + 1.0D, aboveY, z - 1.0D, 0.07F, 0.52F);
            }
            case 5 -> send(player, OutlandsSounds.DREDGE_CRAWL.get(), SoundSource.AMBIENT,
                    x, y - 1.0D, z, 0.09F, 0.55F);
            case 6 -> {
                // Two quiet knocks from a single distant direction, not a repeating event loop.
                send(player, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS,
                        x, y, z, 0.12F, 0.72F);
                send(player, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS,
                        x + 0.28D, y, z + 0.18D, 0.09F, 0.82F);
            }
            default -> send(player, SoundEvents.GRAVEL_STEP, SoundSource.BLOCKS,
                    x, y, z, 0.09F, 0.42F);
        }
    }

    private static void send(ServerPlayer player, SoundEvent sound, SoundSource source,
                             double x, double y, double z, float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), source,
                x, y, z, volume, pitch, player.getRandom().nextLong()));
    }

    public static void clear(ServerPlayer player) {
        NEXT_ALLOWED.remove(player.getUUID());
    }

    public static void clear() {
        NEXT_ALLOWED.clear();
    }
}
