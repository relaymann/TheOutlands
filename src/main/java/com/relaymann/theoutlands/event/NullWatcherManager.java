package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.entity.NullEntity;
import com.relaymann.theoutlands.entity.OutlandsEntities;
import com.relaymann.theoutlands.horror.HorrorMemory;
import com.relaymann.theoutlands.horror.NullStalkingProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Player-specific Null sightings that adapt to remembered places and observed behavior. */
public final class NullWatcherManager {
    private static final long EYE_CONTACT_DELAY = 24L;
    private static final double LOOK_DOT_THRESHOLD = 0.965D;
    private static final double APPROACH_DISTANCE_SQR = 5.0D * 5.0D;
    private static final Map<UUID, ActiveWatcher> ACTIVE = new HashMap<>();
    private NullWatcherManager() {}

    public static void spawn(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        UUID playerId = player.getUUID();
        if (ACTIVE.values().stream().anyMatch(watcher -> watcher.level == level && watcher.playerId.equals(playerId))) return;

        int priorSightings = (int) HorrorMemory.recentEncounters(player).stream()
                .filter(id -> id.equals("distant_figure") || id.equals("the_return") || id.equals("observer")).count();
        priorSightings = Math.max(0, priorSightings - 1); // The triggering encounter was recorded before spawn().
        priorSightings += HorrorMemory.nullObservationCount(player) / 3;
        priorSightings += HorrorMemory.nullStalkingCount(player) / 2;
        NullStalkingProfile profile = NullStalkingProfile.fromPlayerHistory(priorSightings,
                HorrorMemory.lookBackCount(player), HorrorMemory.pauseCount(player), HorrorMemory.flightCount(player));

        BlockPos rememberedPosition = findRememberedHidingPosition(player, profile);
        BlockPos feet = rememberedPosition;
        if (feet == null) feet = findSafeHidingPosition(player, profile);
        if (feet == null) return;
        NullEntity watcher = OutlandsEntities.NULL.get().create(level);
        if (watcher == null) return;
        watcher.moveTo(feet.getX() + 0.5D, feet.getY(), feet.getZ() + 0.5D,
                player.getYRot() + 180.0F, 0.0F);
        watcher.setInvulnerable(true);
        watcher.setPersistenceRequired();
        long now = level.getGameTime();
        watcher.setTemporaryExpiresAt(now + profile.lifetimeTicks());
        watcher.addTag("theoutlands_null_watcher");
        if (level.addFreshEntity(watcher)) {
            ACTIVE.put(watcher.getUUID(), new ActiveWatcher(level, watcher.getUUID(), playerId,
                    now + profile.lifetimeTicks(), now, profile,
                    HorrorMemory.nullObservationCount(player) >= 5 || priorSightings >= 7));
            HorrorMemory.recordNullStalking(player, now);
            HorrorMemory.recordDiscovery(player, "null_first_sighting");
            if (rememberedPosition != null) HorrorMemory.recordDiscovery(player, "null_found_personal_place");
        }
    }

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
            var trackedPlayer = level.getPlayerByUUID(watcher.playerId);
            if (!(trackedPlayer instanceof ServerPlayer target) || !target.isAlive() || target.isRemoved()) {
                entity.discard(); it.remove(); continue;
            }
            if (target.distanceToSqr(entity) <= APPROACH_DISTANCE_SQR) {
                entity.discard(); it.remove(); continue;
            }
            if (isLookingAt(target, entity)) {
                if (watcher.lookedAtSince < 0L) watcher.lookedAtSince = level.getGameTime();
                if (level.getGameTime() - watcher.lookedAtSince >= EYE_CONTACT_DELAY) {
                    HorrorMemory.recordNullObservation(target, level.getGameTime());
                    HorrorMemory.recordDiscovery(target, "null_eye_contact");
                    if (watcher.staresBack && !watcher.hasStaredBack) {
                        BlockPos retreat = findSafeHidingPosition(target, watcher.profile);
                        if (retreat != null && !isLookingAtPosition(target, retreat)) {
                            entity.moveTo(retreat.getX() + 0.5D, retreat.getY(), retreat.getZ() + 0.5D,
                                    target.getYRot() + 180.0F, 0.0F);
                            watcher.lastRelocatedAt = level.getGameTime();
                            watcher.lookedAtSince = -1L;
                            watcher.hasStaredBack = true;
                            continue;
                        }
                    }
                    entity.discard(); it.remove();
                }
                continue;
            }
            watcher.lookedAtSince = -1L;
            if (level.getGameTime() - watcher.lastRelocatedAt >= watcher.profile.relocationIntervalTicks()) {
                BlockPos hidingPosition = findRememberedHidingPosition(target, watcher.profile);
                if (hidingPosition == null) hidingPosition = findSafeHidingPosition(target, watcher.profile);
                if (hidingPosition != null && !isLookingAtPosition(target, hidingPosition)) {
                    entity.moveTo(hidingPosition.getX() + 0.5D, hidingPosition.getY(),
                            hidingPosition.getZ() + 0.5D, target.getYRot() + 180.0F, 0.0F);
                    watcher.lastRelocatedAt = level.getGameTime();
                }
            }
        }
    }

    /** Null may reuse the player's own remembered places, but only in already-loaded chunks. */
    private static BlockPos findRememberedHidingPosition(ServerPlayer player, NullStalkingProfile profile) {
        List<BlockPos> remembered = new ArrayList<>(HorrorMemory.rememberedLocations(player));
        remembered.addAll(HorrorMemory.rememberedSleepLocations(player));
        remembered.addAll(HorrorMemory.rememberedBuildLocations(player));\n        remembered.addAll(HorrorMemory.rememberedDeathLocations(player));
        if (remembered.isEmpty()) return null;
        ServerLevel level = player.serverLevel();
        for (int i = remembered.size() - 1; i >= 0; i--) {
            BlockPos rememberedCell = remembered.get(i);
            if (!level.hasChunkAt(rememberedCell)) continue;
            double distance = Math.sqrt(player.position().distanceToSqr(rememberedCell.getX() + 16.5D,
                    player.getY(), rememberedCell.getZ() + 16.5D));
            if (distance < profile.minimumDistance() || distance > profile.maximumDistance() + 28.0D) continue;
            int x = rememberedCell.getX() + 16;
            int z = rememberedCell.getZ() + 16;
            if (!level.hasChunkAt(new BlockPos(x, player.blockPosition().getY(), z))) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos candidate = new BlockPos(x, y, z);
            if (isSafeStandingPosition(level, candidate)) return candidate;
        }
        return null;
    }

    private static BlockPos findSafeHidingPosition(ServerPlayer player, NullStalkingProfile profile) {
        ServerLevel level = player.serverLevel();
        double yaw = Math.toRadians(player.getYRot());
        BlockPos visibleFallback = null;
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = yaw + (player.getRandom().nextDouble() - 0.5D)
                    * (attempt < 5 ? 1.2D : Math.PI * 2.0D);
            double distance = profile.minimumDistance()
                    + player.getRandom().nextDouble() * (profile.maximumDistance() - profile.minimumDistance());
            int bx = (int) Math.floor(player.getX() - Math.sin(angle) * distance);
            int bz = (int) Math.floor(player.getZ() + Math.cos(angle) * distance);
            BlockPos column = new BlockPos(bx, player.blockPosition().getY(), bz);
            if (!level.hasChunkAt(column)) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
            BlockPos feet = new BlockPos(bx, y, bz);
            if (!isSafeStandingPosition(level, feet)) continue;
            if (visibleFallback == null) visibleFallback = feet;
            Vec3 target = Vec3.atCenterOf(feet).add(0.0D, 0.9D, 0.0D);
            boolean concealed = level.clip(new ClipContext(player.getEyePosition(), target,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.BLOCK;
            if (concealed) return feet;
        }
        return visibleFallback;
    }

    private static boolean isSafeStandingPosition(ServerLevel level, BlockPos feet) {
        return feet.getY() < level.getMaxBuildHeight() - 2
                && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                && level.getFluidState(feet).isEmpty()
                && level.getBlockState(feet).isAir()
                && level.getBlockState(feet.above()).isAir()
                && level.getBlockState(feet.above(2)).isAir();
    }

    private static boolean isLookingAt(ServerPlayer player, Entity entity) {
        if (player.distanceToSqr(entity) > 64.0D * 64.0D || !player.hasLineOfSight(entity)) return false;
        Vec3 target = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D).subtract(player.getEyePosition());
        return target.lengthSqr() < 1.0E-6D || player.getLookAngle().dot(target.normalize()) >= LOOK_DOT_THRESHOLD;
    }
    private static boolean isLookingAtPosition(ServerPlayer player, BlockPos position) {
        Vec3 target = Vec3.atCenterOf(position).subtract(player.getEyePosition());
        return target.lengthSqr() < 1.0E-6D || player.getLookAngle().dot(target.normalize()) >= LOOK_DOT_THRESHOLD;
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
        private final NullStalkingProfile profile;
        private long lookedAtSince = -1L;
        private long lastRelocatedAt;
        private final boolean staresBack;
        private boolean hasStaredBack;
        private ActiveWatcher(ServerLevel level, UUID entityId, UUID playerId, long expiresAt,
                              long lastRelocatedAt, NullStalkingProfile profile, boolean staresBack) {
            this.level = level; this.entityId = entityId; this.playerId = playerId;
            this.expiresAt = expiresAt; this.lastRelocatedAt = lastRelocatedAt;
            this.profile = profile; this.staresBack = staresBack;
        }
    }
}
