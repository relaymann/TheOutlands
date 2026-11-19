package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.entity.NullApostleEntity;
import com.relaymann.theoutlands.entity.OutlandsEntities;
import com.relaymann.theoutlands.horror.AlmightyTerritory;
import com.relaymann.theoutlands.horror.DistanceProgression;
import com.relaymann.theoutlands.horror.HorrorMemory;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Rare, player-specific appearances of Null's unnamed apostles. */
public final class NullApostleManager {
    private static final Map<UUID, ActiveApostle> ACTIVE = new HashMap<>();
    private static final long COOLDOWN = 24_000L;

    private NullApostleManager() {}

    public static void spawn(ServerPlayer player) {
        if (!player.serverLevel().dimension().equals(Level.OVERWORLD)
                || DistanceProgression.horizontalDistanceFromWorldSpawn(player) < 22_000.0D
                || AlmightyTerritory.contains(player.serverLevel(), player.getX(), player.getZ())
                || HorrorMemory.nullObservationCount(player) < 2
                || player.serverLevel().getGameTime() - HorrorMemory.lastNullApostleSpawn(player) < COOLDOWN
                || ACTIVE.values().stream().anyMatch(a -> a.level == player.serverLevel()
                    && a.playerId.equals(player.getUUID()))) return;

        ServerLevel level = player.serverLevel();
        int observations = HorrorMemory.nullObservationCount(player);
        int groupSize = observations >= 6 ? (player.getRandom().nextBoolean() ? 2 : 3)
                : observations >= 3 ? 2 : 1;
        List<BlockPos> positions = new ArrayList<>();
        long now = level.getGameTime();
        for (int i = 0; i < groupSize; i++) {
            BlockPos position = findPosition(player, 28.0D, 42.0D, positions);
            if (position == null) continue;
            NullApostleEntity apostle = OutlandsEntities.NULL_APOSTLE.get().create(level);
            if (apostle == null) continue;
            apostle.moveTo(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D,
                    player.getYRot() + 180.0F, 0.0F);
            apostle.setBlessedPlayer(player.getUUID());
            apostle.setTemporaryExpiresAt(now + 180L);
            apostle.setNoAi(true);
            // Apostles are mortal and intentionally fragile compared with Null.
            apostle.setInvulnerable(false);
            if (!level.noCollision(apostle, apostle.getBoundingBox()) || !level.addFreshEntity(apostle)) continue;
            positions.add(position);
            ACTIVE.put(apostle.getUUID(), new ActiveApostle(level, apostle.getUUID(), player.getUUID(),
                    now + 180L, now, -1L));
        }
        if (positions.isEmpty()) return;
        HorrorMemory.setLastNullApostleSpawn(player, now);
        HorrorMemory.recordDiscovery(player, "null_apostle_seen");
        if (positions.size() > 1) HorrorMemory.recordDiscovery(player, "null_apostle_group_seen");
    }

    public static void tick(ServerLevel level) {
        Iterator<Map.Entry<UUID, ActiveApostle>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            ActiveApostle active = it.next().getValue();
            if (active.level != level) continue;

            Entity entity = level.getEntity(active.entityId);
            var trackedPlayer = level.getPlayerByUUID(active.playerId);
            if (!(entity instanceof NullApostleEntity apostle)
                    || !(trackedPlayer instanceof ServerPlayer player)
                    || !player.isAlive() || !apostle.isBlessedTo(player.getUUID())
                    || level.getGameTime() >= active.expiresAt) {
                if (entity != null) entity.discard();
                it.remove();
                continue;
            }

            if (player.distanceToSqr(apostle) < 6.0D * 6.0D) {
                apostle.discard();
                it.remove();
                continue;
            }

            // The blessing lets it sense its assigned player through obstructions.
            // Ordinary line of sight is only used to decide whether the player has
            // actually caught it looking at them.
            if (player.hasLineOfSight(apostle) && isLookingAt(player, apostle)) {
                if (active.lookedAtSince < 0L) active.lookedAtSince = level.getGameTime();
                if (level.getGameTime() - active.lookedAtSince >= 18L) {
                    if (OutlandsConfig.HORROR_AUDIO_ENABLED.get())
                        level.playSound(player, apostle.blockPosition(), OutlandsSounds.NULL_APOSTLE_DEPARTURE.get(),
                                SoundSource.HOSTILE, 0.22F, 0.72F + player.getRandom().nextFloat() * 0.12F);
                    apostle.discard();
                    it.remove();
                }
                continue;
            }

            active.lookedAtSince = -1L;
            if (level.getGameTime() - active.lastRelocatedAt >= 65L) {
                List<BlockPos> occupied = new ArrayList<>();
                for (ActiveApostle other : ACTIVE.values()) {
                    if (other.level == level && other.playerId.equals(player.getUUID()) && !other.entityId.equals(apostle.getUUID())) {
                        Entity otherEntity = level.getEntity(other.entityId);
                        if (otherEntity != null) occupied.add(otherEntity.blockPosition());
                    }
                }
                BlockPos next = findPosition(player, 24.0D, 40.0D, occupied);
                if (next != null && !isLookingAtPosition(player, next)) {
                    apostle.moveTo(next.getX() + 0.5D, next.getY(), next.getZ() + 0.5D,
                            player.getYRot() + 180.0F, 0.0F);
                    active.lastRelocatedAt = level.getGameTime();
                }
            }
        }
    }

    private static BlockPos findPosition(ServerPlayer player, double min, double max, List<BlockPos> occupied) {
        ServerLevel level = player.serverLevel();
        for (int i = 0; i < 18; i++) {
            double angle = Math.toRadians(player.getYRot())
                    + (player.getRandom().nextDouble() - 0.5D) * Math.PI * 1.4D;
            double distance = min + player.getRandom().nextDouble() * (max - min);
            int x = (int) Math.floor(player.getX() - Math.sin(angle) * distance);
            int z = (int) Math.floor(player.getZ() + Math.cos(angle) * distance);
            BlockPos column = new BlockPos(x, player.blockPosition().getY(), z);
            if (!level.hasChunkAt(column)) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos feet = new BlockPos(x, y, z);
            boolean separated = occupied.stream().allMatch(other -> other.distSqr(feet) >= 25.0D);
            if (separated && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), net.minecraft.core.Direction.UP)
                    && level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir()
                    && level.getFluidState(feet).isEmpty()) return feet;
        }
        return null;
    }

    private static boolean isLookingAt(ServerPlayer player, Entity entity) {
        Vec3 target = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D)
                .subtract(player.getEyePosition());
        return target.lengthSqr() > 1.0E-6D && player.getLookAngle().dot(target.normalize()) > 0.965D;
    }

    private static boolean isLookingAtPosition(ServerPlayer player, BlockPos pos) {
        Vec3 target = Vec3.atCenterOf(pos).subtract(player.getEyePosition());
        return target.lengthSqr() > 1.0E-6D && player.getLookAngle().dot(target.normalize()) > 0.965D;
    }

    public static void clear() {
        for (ActiveApostle active : ACTIVE.values()) {
            Entity entity = active.level.getEntity(active.entityId);
            if (entity != null) entity.discard();
        }
        ACTIVE.clear();
    }

    private static final class ActiveApostle {
        final ServerLevel level;
        final UUID entityId;
        final UUID playerId;
        final long expiresAt;
        long lastRelocatedAt;
        long lookedAtSince;

        ActiveApostle(ServerLevel level, UUID entityId, UUID playerId, long expiresAt,
                      long lastRelocatedAt, long lookedAtSince) {
            this.level = level;
            this.entityId = entityId;
            this.playerId = playerId;
            this.expiresAt = expiresAt;
            this.lastRelocatedAt = lastRelocatedAt;
            this.lookedAtSince = lookedAtSince;
        }
    }
}
