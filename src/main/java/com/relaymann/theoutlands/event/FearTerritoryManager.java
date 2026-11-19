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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.AABB;

/** Sparse environmental behavior for Fear's forest domain; all cues are target-player specific. */
public final class FearTerritoryManager {
    private FearTerritoryManager() {}

    public static void tick(ServerPlayer player) {
        var level = player.serverLevel();
        if (DistanceProgression.horizontalDistanceFromWorldSpawn(player)
                < OutlandsConfig.FEAR_FULL_TERRITORY_DISTANCE.get()) return;
        if (AlmightyTerritory.contains(level, player.getX(), player.getZ())) return;
        var biomeKey = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .getKey(level.getBiome(player.blockPosition()).value());
        if (biomeKey == null || !(biomeKey.getPath().contains("forest")
                || biomeKey.getPath().equals("fear_forest"))) return;
        // This method is called on the player's 20-tick progression cadence; use
        // that same clock so an offset world time cannot suppress the event forever.
        if (player.tickCount % 100 != 0) return;

        HorrorMemory.recordDiscovery(player, "fear_forest_entered");
        // Fear disturbs nearby wildlife, not hostile hunters or other Outlands entities.
        var nearby = level.getEntitiesOfClass(Mob.class,
                new AABB(player.blockPosition()).inflate(24.0D),
                mob -> mob.isAlive() && mob.getType().getCategory() == MobCategory.CREATURE);
        if (!nearby.isEmpty() && player.getRandom().nextInt(3) == 0) {
            Mob chosen = nearby.get(player.getRandom().nextInt(nearby.size()));
            chosen.getNavigation().stop();
            chosen.getLookControl().setLookAt(player, 5.0F, 5.0F);
        }

        // Forest animals occasionally stop behaving naturally, but no mob is forcibly hostile.
        if (player.getRandom().nextInt(9) == 0) {
            for (Mob mob : nearby) {
                if (mob.getType() == EntityType.WOLF || mob.getType() == EntityType.FOX) {
                    mob.getNavigation().stop();
                    mob.getLookControl().setLookAt(player, 3.0F, 3.0F);
                    break;
                }
            }
        }

        if (player.getRandom().nextInt(6) == 0) {
            double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
            double distance = 24.0D + player.getRandom().nextDouble() * 18.0D;
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            if (OutlandsConfig.HORROR_AUDIO_ENABLED.get()) player.connection.send(new ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.AMBIENT_CAVE.value()),
                    SoundSource.AMBIENT, x, player.getY() + 1.0D, z, 0.10F,
                    0.42F + player.getRandom().nextFloat() * 0.16F, player.getRandom().nextLong()));
            level.sendParticles(player, ParticleTypes.ASH, true, x, player.getY() + 2.0D, z,
                    3, 0.3D, 0.6D, 0.3D, 0.001D);
        }
        if (player.getRandom().nextInt(24) == 0) ClientAfterimageManager.showFalseStructure(player);
    }
}
