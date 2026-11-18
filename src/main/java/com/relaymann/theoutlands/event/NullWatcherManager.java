package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.entity.NullEntity;
import com.relaymann.theoutlands.entity.OutlandsEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Creates temporary Null sightings that vanish after sustained eye contact. */
public final class NullWatcherManager {
    private static final long LIFETIME = 260L;
    private static final long EYE_CONTACT_DELAY = 12L;
    private static final double LOOK_DOT_THRESHOLD = 0.965D;
    private static final Map<UUID, ActiveWatcher> ACTIVE = new HashMap<>();

    private NullWatcherManager() {}

    public static void spawn(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        UUID playerId = player.getUUID();
        if (ACTIVE.values().stream().anyMatch(watcher ->
                watcher.level == level && watcher.playerId.equals(playerId))) return;

        double yaw = Math.toRadians(player.getYRot());
        double angle = yaw + (player.getRandom().nextDouble() - 0.5D) * 0.9D;
        double distance = 20.0D + player.getRandom().nextDouble() * 9.0D;
        double x = player.getX() - Math.sin(angle) * distance;
        double z = player.getZ() + Math.cos(angle) * distance;
        int bx = (int) Math.floor(x);
        int bz = (int) Math.floor(z);
        BlockPos column = new BlockPos(bx, player.blockPosition().getY(), bz);
        if (!level.hasChunkAt(column)) return;

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
        BlockPos feet = new BlockPos(bx, y, bz);
        if (y >= level.getMaxY() - 2
                || !level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                || !level.getFluidState(feet).isEmpty()
                || !level.getBlockState(feet).isAir()
                || !level.getBlockState(feet.above()).isAir()
                || !level.getBlockState(feet.above(2)).isAir()) return;

        NullEntity watcher = OutlandsEntities.NULL.get().create(level);
        if (watcher == null) return;
        watcher.moveTo(x + 0.5D, y, z + 0.5D, player.getYRot() + 180.0F, 0.0F);
        watcher.setInvulnerable(true);
        watcher.setSilent(true);
        watcher.setPersistenceRequired();
        watcher.addTag("theoutlands_null_watcher");

        if (level.addFreshEntity(watcher)) {
            ACTIVE.put(watcher.getUUID(), new ActiveWatcher(
                    level, watcher.getUUID(), playerId, level.getGameTime() + LIFETIME));
        }
    }

    /** Visits only managed watcher UUIDs; safe when several players update together. */
    public static void tick(ServerLevel level) {
        Iterator<Map.Entry<UUID, ActiveWatcher>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            ActiveWatcher watcher = it.next().getValue();
            if (watcher.level != level) continue;
            Entity entity = level.getEntity(watcher.entityId);
            if (entity == null || level.getGameTime() >= watcher.expiresAt) {
                if (entity != null) entity.discard();
                it.remove();
                continue;
            }

            ServerPlayer target = level.getPlayerByUUID(watcher.playerId);
            if (target == null || !target.isAlive() || !isLookingAt(target, entity)) {
                watcher.lookedAtSince = -1L;
                continue;
            }
            if (watcher.lookedAtSince < 0L) {
                watcher.lookedAtSince = level.getGameTime();
            } else if (level.getGameTime() - watcher.lookedAtSince >= EYE_CONTACT_DELAY) {
                entity.discard();
                it.remove();
            }
        }
    }

    private static boolean isLookingAt(ServerPlayer player, Entity entity) {
        if (player.distanceToSqr(entity) > 64.0D * 64.0D || !player.hasLineOfSight(entity)) return false;
        Vec3 target = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D)
                .subtract(player.getEyePosition());
        return target.lengthSqr() < 1.0E-6D
                || player.getLookAngle().dot(target.normalize()) >= LOOK_DOT_THRESHOLD;
    }

    public static void clear() {
        for (ActiveWatcher watcher : ACTIVE.values()) {
            Entity entity = watcher.level.getEntity(watcher.entityId);
            if (entity != null) entity.discard();
        }
        ACTIVE.clear();
    }

    private static final class ActiveWatcher {
        private final ServerLevel level;
        private final UUID entityId;
        private final UUID playerId;
        private final long expiresAt;
        private long lookedAtSince = -1L;

        private ActiveWatcher(ServerLevel level, UUID entityId, UUID playerId, long expiresAt) {
            this.level = level;
            this.entityId = entityId;
            this.playerId = playerId;
            this.expiresAt = expiresAt;
        }
    }
}
