package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.horror.AlmightyTerritory;
import com.relaymann.theoutlands.horror.DistanceProgression;
import com.relaymann.theoutlands.horror.HorrorMemory;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/** Player-local environmental behavior for Fear's forest domain. */
public final class FearTerritoryManager {
    private FearTerritoryManager() {}

    public static void tick(ServerPlayer player) {
        var level = player.serverLevel();
        if (DistanceProgression.horizontalDistanceFromWorldSpawn(player)
                < OutlandsConfig.FEAR_FULL_TERRITORY_DISTANCE.get()) return;
        if (AlmightyTerritory.contains(level, player.getX(), player.getZ())) return;
        var biomeKey = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .getKey(level.getBiome(player.blockPosition()).value());
        if (biomeKey == null || !biomeKey.getPath().contains("forest")) return;

        HorrorMemory.recordDiscovery(player, "fear_forest_entered");
        // The forest notices the player: nearby mobs occasionally turn toward them without a warning cue.
        for (Mob mob : level.getEntitiesOfClass(Mob.class,
                new AABB(player.blockPosition()).inflate(22.0D), Mob::isAlive)) {
            if (player.getRandom().nextInt(5) == 0) {
                mob.getLookControl().setLookAt(player, 5.0F, 5.0F);
            }
        }

        if (player.getRandom().nextInt(7) == 0) {
            double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
            double distance = 22.0D + player.getRandom().nextDouble() * 16.0D;
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            player.connection.send(new ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.AMBIENT_CAVE.value()),
                    SoundSource.AMBIENT, x, player.getY() + 1.0D, z, 0.12F,
                    0.48F + player.getRandom().nextFloat() * 0.18F, player.getRandom().nextLong()));
            level.sendParticles(player, ParticleTypes.ASH, true,
                    x, player.getY() + 2.0D, z, 2, 0.25D, 0.5D, 0.25D, 0.001D);
        }
    }
}
