package com.relaymann.theoutlands.horror;

import com.relaymann.theoutlands.OutlandsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Distance-based progression calculations.
 *
 * <p>The original shared Overworld spawn is the anchor. A player's personal bed or
 * respawn point must never move the horror origin. Progression is deliberately unbounded:
 * tiers are organizational milestones, not a maximum difficulty stage.</p>
 */
public final class DistanceProgression {
    public static final double FIRST_ANOMALY_DISTANCE = 1_000.0D;
    private static final double LOG_TWO = Math.log(2.0D);

    private DistanceProgression() {
    }

    public static double firstAnomalyDistance() {
        return OutlandsConfig.FIRST_ANOMALY_DISTANCE.get();
    }

    public static double horizontalDistanceFromWorldSpawn(ServerPlayer player) {
        ServerLevel overworld = player.getServer().overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        double spawnCenterX = spawn.getX() + 0.5D;
        double spawnCenterZ = spawn.getZ() + 0.5D;
        return Math.hypot(player.getX() - spawnCenterX, player.getZ() - spawnCenterZ);
    }

    /** Returns zero through the configured opening range, then an unbounded logarithmic escalation value. */
    public static double escalationValue(double distanceFromSpawn) {
        double openingRange = firstAnomalyDistance();
        if (!Double.isFinite(distanceFromSpawn) || distanceFromSpawn <= openingRange) return 0.0D;
        return Math.log1p((distanceFromSpawn - openingRange) / openingRange);
    }

    /** Returns an unbounded, doubling-distance milestone index. */
    public static int escalationTier(double distanceFromSpawn) {
        double openingRange = firstAnomalyDistance();
        if (!Double.isFinite(distanceFromSpawn) || distanceFromSpawn <= openingRange) return 0;
        double ratio = distanceFromSpawn / openingRange;
        return 1 + (int) Math.floor(Math.log(ratio) / LOG_TWO);
    }

    public static HorrorSnapshot createSnapshot(ServerPlayer player) {
        double distance = horizontalDistanceFromWorldSpawn(player);
        return new HorrorSnapshot(distance, escalationValue(distance), escalationTier(distance));
    }
}
