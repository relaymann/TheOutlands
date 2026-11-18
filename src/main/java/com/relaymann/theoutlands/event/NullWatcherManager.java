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
import net.minecraft.world.level.levelgen.Heightmap;\nimport net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;\nimport net.minecraft.world.phys.HitResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Manages short-lived Null sightings. Sightings relocate to safe, loaded hiding
 * positions while the target is not looking, disappear when approached or observed,
 * and gradually become closer and more persistent after repeated encounters.
 */
public final class NullWatcherManager {
    private static final long BASE_LIFETIME = 260L;
    private static final long EYE_CONTACT_DELAY = 12L;
    private static final long BASE_RELOCATION_INTERVAL = 75L;
    private static final double LOOK_DOT_THRESHOLD = 0.965D;
    private static final double APPROACH_DISTANCE_SQR = 5.0D * 5.0D;
    private static final Map<UUID, ActiveWatcher> ACTIVE = new HashMap<>();

    private NullWatcherManager() {}

    public static void spawn(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        UUID playerId = player.getUUID();
        if (ACTIVE.values().stream().anyMatch(watcher ->
                watcher.level == level && watcher.playerId.equals(playerId))) return;

        int priorSightings = (int) HorrorMemory.recentEncounters(player).stream()
                .filter(id -> id.equals("distant_figure") || id.equals("the_return")).count();
        priorSightings = Math.max(0, priorSightings - 1);
        NullStalkingProfile profile = NullStalkingProfile.fromPreviousSightings(priorSightings);

        BlockPos feet = findSafeHidingPosition(player, profile);
        if (feet == null) return;
        NullEntity watcher = OutlandsEntities.NULL.get().create(level);
        if (watcher == null) return;
        watcher.moveTo(feet.getX() + 0.5D, feet.getY(), feet.getZ() + 0.5D,
                player.getYRot() + 180.0F, 0.0F);
        watcher.setInvulnerable(true);
        watcher.setSilent(true);
        watcher.setPersistenceRequired();
        watcher.addTag("theoutlands_null_watcher");

        if (level.addFreshEntity(watcher)) {
            long now = level.getGameTime();
            ACTIVE.put(watcher.getUUID(), new ActiveWatcher(level, watcher.getUUID(), playerId,
                    now + profile.lifetimeTicks(), now, profile));
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

            var trackedPlayer = level.getPlayerByUUID(watcher.playerId);
            if (!(trackedPlayer instanceof ServerPlayer target)
                    || !target.isAlive() || target.isRemoved()) {
                entity.discard();
                it.remove();
                continue;
            }

            if (target.distanceToSqr(entity) <= APPROACH_DISTANCE_SQR) {
                entity.discard();
                it.remove();
                continue;
            }

            if (isLookingAt(target, entity)) {
                if (watcher.lookedAtSince < 0L) watcher.lookedAtSince = level.getGameTime();
                if (level.getGameTime() - watcher.lookedAtSince >= EYE_CONTACT_DELAY) {
                    entity.discard();
                    it.remove();
                }
                continue;
            }

            watcher.lookedAtSince = -1L;
            if (level.getGameTime() - watcher.lastRelocatedAt >= watcher.profile.relocationIntervalTicks()) {
                BlockPos hidingPosition = findSafeHidingPosition(target, watcher.profile);
                if (hidingPosition != null && !isLookingAtPosition(target, hidingPosition)) {
                    entity.moveTo(hidingPosition.getX() + 0.5D, hidingPosition.getY(),
                            hidingPosition.getZ() + 0.5D, target.getYRot() + 180.0F, 0.0F);
                    watcher.lastRelocatedAt = level.getGameTime();
                }
            }
        }
    }

    private static BlockPos findSafeHidingPosition(ServerPlayer player, NullStalkingProfile profile) {
        ServerLevel level = player.serverLevel();
        double yaw = Math.toRadians(player.getYRot());
        BlockPos visibleFallback = null;

        // Prefer already-loaded natural cover. No chunk is requested or generated here.
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = yaw + (player.getRandom().nextDouble() - 0.5D) * (attempt < 5 ? 1.2D : Math.PI * 2.0D);
            double distance = profile.minimumDistance()
                    + player.getRandom().nextDouble() * (profile.maximumDistance() - profile.minimumDistance());
            int bx = (int) Math.floor(player.getX() - Math.sin(angle) * distance);
            int bz = (int) Math.floor(player.getZ() + Math.cos(angle) * distance);
            BlockPos column = new BlockPos(bx, player.blockPosition().getY(), bz);
            if (!level.hasChunkAt(column)) continue;

            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
            BlockPos feet = new BlockPos(bx, y, bz);
            if (y >= level.getMaxBuildHeight() - 2
                    || !level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                    || !level.getFluidState(feet).isEmpty()
                    || !level.getBlockState(feet).isAir()
                    || !level.getBlockState(feet.above()).isAir()
                    || !level.getBlockState(feet.above(2)).isAir()) continue;

            if (visibleFallback == null) visibleFallback = feet;
            Vec3 eye = player.getEyePosition();
            Vec3 target = Vec3.atCenterOf(feet).add(0.0D, 0.9D, 0.0D);
            boolean concealed = level.clip(new ClipContext(eye, target,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.BLOCK;
            if (concealed) return feet;
        }
        return visibleFallback;
    }

