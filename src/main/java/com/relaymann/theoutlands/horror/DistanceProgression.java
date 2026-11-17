package com.relaymann.theoutlands.horror;

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

    public static double horizontalDistanceFromWorldSpawn(ServerPlayer player) {
        ServerLevel overworld = player.getServer().overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        double spawnCenterX = spawn.getX() + 0.5D;
        double spawnCenterZ = spawn.getZ() + 0.5D;
        return Math.hypot(player.getX() - spawnCenterX, player.getZ() - spawnCenterZ);
    }

    /**
     * Returns zero through the opening 1,000 blocks, then an unbounded logarithmic
     * escalation value. This avoids a hard cap at any arbitrary distance.
     */
    public static double escalationValue(double distanceFromSpawn) {
        if (!Double.isFinite(distanceFromSpawn) || distanceFromSpawn <= FIRST_ANOMALY_DISTANCE) {
            return 0.0D;
        }

        return Math.log1p((distanceFromSpawn - FIRST_ANOMALY_DISTANCE) / FIRST_ANOMALY_DISTANCE);
    }

    /**
     * Returns an unbounded, doubling-distance milestone index. It is not a content
     * ceiling; new milestones can continue to be added at greater distances.
     */
    public static int escalationTier(double distanceFromSpawn) {
        if (!Double.isFinite(distanceFromSpawn) || distanceFromSpawn <= FIRST_ANOMALY_DISTANCE) {
            return 0;
        }

        double ratio = distanceFromSpawn / FIRST_ANOMALY_DISTANCE;
        return 1 + (int) Math.floor(Math.log(ratio) / LOG_TWO);
    }

    public static HorrorSnapshot createSnapshot(ServerPlayer player) {
        double distance = horizontalDistanceFromWorldSpawn(player);
        return new HorrorSnapshot(distance, escalationValue(distance), escalationTier(distance));
    }
}
