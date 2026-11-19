package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.entity.DeathEntity;
import com.relaymann.theoutlands.entity.FearEntity;
import com.relaymann.theoutlands.entity.OutlandsEntities;
import com.relaymann.theoutlands.horror.AggressiveJumpscarePolicy;
import com.relaymann.theoutlands.horror.AlmightyTerritory;
import com.relaymann.theoutlands.horror.HorrorMemory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Rare, close-range Fear/Death manifestations. The normal first meetings remain
 * separate story beats; this manager only adds occasional, short-lived scares.
 */
public final class AggressiveJumpscareManager {
    private static final long ENTITY_LIFETIME_TICKS = 14L;
    private static final Map<UUID, ActiveScare> ACTIVE = new HashMap<>();

    private AggressiveJumpscareManager() {}

    public static void evaluate(ServerPlayer player, double distanceFromSpawn) {
        ServerLevel level = player.serverLevel();
        if (!level.dimension().equals(Level.OVERWORLD) || !player.isAlive()
                || player.isSpectator() || player.isCreative()
                || AlmightyTerritory.contains(level, player.getX(), player.getZ())) return;

        boolean fearIntroduced = HorrorMemory.hasDiscovered(player, "fear_first_introduction");
        boolean deathIntroduced = HorrorMemory.hasDiscovered(player, "death_first_sighting");
        long now = level.getGameTime();
        if (HorrorMemory.initializeAggressiveJumpscareCooldown(player, now)) return;
        long elapsed = now - HorrorMemory.lastAggressiveJumpscare(player);
        if (!AggressiveJumpscarePolicy.shouldAttempt(distanceFromSpawn, fearIntroduced, deathIntroduced,
                false, elapsed, player.getRandom().nextDouble())) return;

        // Death is the harsher encounter, but it cannot appear before his own story introduction.
        boolean useDeath = deathIntroduced && (!fearIntroduced || player.getRandom().nextInt(100) < 62);
        Entity scare = spawnCloseManifestation(player, useDeath);
        if (scare == null) return;

        HorrorMemory.setLastAggressiveJumpscare(player, now);
        if (OutlandsConfig.HORROR_AUDIO_ENABLED.get()) {
            player.playNotifySound(useDeath ? SoundEvents.ENDERMAN_SCREAM : SoundEvents.WARDEN_ROAR,
                    SoundSource.HOSTILE, useDeath ? 0.95F : 0.85F, useDeath ? 0.78F : 0.62F);
        }
        ACTIVE.put(scare.getUUID(), new ActiveScare(level, scare.getUUID(), player.getUUID(), now + ENTITY_LIFETIME_TICKS));
    }

    private static Entity spawnCloseManifestation(ServerPlayer player, boolean death) {
        ServerLevel level = player.serverLevel();
        double yaw = Math.toRadians(player.getYRot());
        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);
        double sideX = Math.cos(yaw);
        double sideZ = Math.sin(yaw);

        // Try the center first, then slight offsets so a wall does not cancel every scare.
        double[][] offsets = {{0.0D, 2.65D}, {-0.8D, 2.45D}, {0.8D, 2.45D}, {0.0D, 3.2D}};
        for (double[] offset : offsets) {
            double x = player.getX() + forwardX * offset[1] + sideX * offset[0];
            double z = player.getZ() + forwardZ * offset[1] + sideZ * offset[0];
            BlockPos position = BlockPos.containing(x, player.getY(), z);
            if (!level.hasChunkAt(position) || !level.hasChunkAt(position.above())) continue;

            Mob entity = death ? OutlandsEntities.DEATH.get().create(level)
                    : OutlandsEntities.FEAR.get().create(level);
            if (entity == null) return null;

            double dx = player.getX() - x;
            double dz = player.getZ() - z;
            float facingYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            entity.moveTo(x, player.getY(), z, facingYaw, 0.0F);
            entity.setYHeadRot(facingYaw);
            entity.setYBodyRot(facingYaw);
            entity.setInvulnerable(true);
            entity.setSilent(true);
            entity.setNoAi(true);
            entity.setNoGravity(true);
            long expiresAt = level.getGameTime() + ENTITY_LIFETIME_TICKS;
            if (entity instanceof DeathEntity deathEntity) deathEntity.setTemporaryExpiresAt(expiresAt);
            else if (entity instanceof FearEntity fearEntity) fearEntity.setTemporaryExpiresAt(expiresAt);
            entity.addTag("theoutlands_aggressive_jumpscare");
            if (!level.noCollision(entity, entity.getBoundingBox())) {
                entity.discard();
                continue;
            }
            if (level.addFreshEntity(entity)) return entity;
            entity.discard();
        }
        return null;
    }

    public static void tick(ServerLevel level) {
        Iterator<Map.Entry<UUID, ActiveScare>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            ActiveScare active = iterator.next().getValue();
            if (active.level != level) continue;
            Entity entity = level.getEntity(active.entityId);
            if (entity == null || level.getGameTime() >= active.expiresAt) {
                if (entity != null) entity.discard();
                iterator.remove();
            }
        }
    }

    public static void clear(ServerPlayer player) {
        Iterator<Map.Entry<UUID, ActiveScare>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            ActiveScare active = iterator.next().getValue();
            if (!active.playerId.equals(player.getUUID())) continue;
            Entity entity = active.level.getEntity(active.entityId);
            if (entity != null) entity.discard();
            iterator.remove();
        }
    }

    public static void clear() {
        for (ActiveScare active : ACTIVE.values()) {
            Entity entity = active.level.getEntity(active.entityId);
            if (entity != null) entity.discard();
        }
        ACTIVE.clear();
    }

    private record ActiveScare(ServerLevel level, UUID entityId, UUID playerId, long expiresAt) {}
}
