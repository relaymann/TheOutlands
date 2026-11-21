package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Rare, player-private signs of nearby creatures. These cues do not spawn
 * entities, reveal exact locations, force encounters, or alter the world.
 */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class EnvironmentalPresenceManager {
    private static final long COOLDOWN = 9_000L;
    private static final Map<UUID, Long> NEXT_CUE = new HashMap<>();

    private EnvironmentalPresenceManager() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!OutlandsConfig.AMBIENT_ANOMALIES_ENABLED.get()
                || !OutlandsConfig.HORROR_AUDIO_ENABLED.get()
                || event.getServer().getTickCount() % 20 != 0) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!level.dimension().equals(Level.OVERWORLD)) continue;
            long now = level.getGameTime();
            for (ServerPlayer player : level.players()) {
                if (!player.isAlive() || player.isCreative() || player.isSpectator()
                        || player.isSleeping() || player.isPassenger() || player.isInWaterOrBubble()
                        || !farEnoughFromSpawn(level, player)) continue;
                if (now < NEXT_CUE.getOrDefault(player.getUUID(), 0L)
                        || player.getRandom().nextInt(420) != 0) continue;
                playDistantSign(player);
                NEXT_CUE.put(player.getUUID(), now + COOLDOWN
                        + player.getRandom().nextInt(4_001));
            }
        }
    }

    private static boolean farEnoughFromSpawn(ServerLevel level, ServerPlayer player) {
        double dx = player.getX() - level.getSharedSpawnPos().getX();
        double dz = player.getZ() - level.getSharedSpawnPos().getZ();
        double minimum = Math.max(4_000.0D, OutlandsConfig.AMBIENT_ANOMALY_MIN_DISTANCE.get());
        return dx * dx + dz * dz >= minimum * minimum;
    }

    private static void playDistantSign(ServerPlayer player) {
        double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
        double distance = 22.0D + player.getRandom().nextDouble() * 18.0D;
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        double y = player.getY() + (player.getRandom().nextInt(5) - 2);
        SoundEvent sound = switch (player.getRandom().nextInt(5)) {
            case 0 -> OutlandsSounds.LURKER_BREATH.get();
            case 1 -> OutlandsSounds.STALKER_BREATH.get();
            case 2 -> OutlandsSounds.CAVEJAR_STALK.get();
            case 3 -> OutlandsSounds.DREDGE_CRAWL.get();
            default -> OutlandsSounds.HOWLER_BREATH.get();
        };
        player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), SoundSource.AMBIENT,
                x, y, z, 0.12F, 0.72F + player.getRandom().nextFloat() * 0.2F,
                player.getRandom().nextLong()));
    }

    public static void clear(ServerPlayer player) { NEXT_CUE.remove(player.getUUID()); }
    public static void clear() { NEXT_CUE.clear(); }
}
