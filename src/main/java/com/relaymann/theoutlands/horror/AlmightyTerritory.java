package com.relaymann.theoutlands.horror;

import com.relaymann.theoutlands.OutlandsConfig;

/**
 * Geometry for the Almighty's exclusive endgame region.
 *
 * <p>The region is measured inward from the configured square world border, not from
 * assumed spawn-relative coordinates. A negative distance means the position is
 * outside the border. The four corners are handled using the nearest edge of the
 * square, so the region follows non-default border centers and sizes correctly.</p>
 */
public final class AlmightyTerritory {
    public static final double DEFAULT_DEPTH_BLOCKS = 5_000.0D;

    private AlmightyTerritory() {
    }

    public static double distanceInsideBorder(double centerX, double centerZ, double borderSize,
                                              double x, double z) {
        if (!Double.isFinite(centerX) || !Double.isFinite(centerZ)
                || !Double.isFinite(borderSize) || borderSize <= 0.0D
                || !Double.isFinite(x) || !Double.isFinite(z)) return Double.NaN;
        double halfSize = borderSize / 2.0D;
        return halfSize - Math.max(Math.abs(x - centerX), Math.abs(z - centerZ));
    }

    public static boolean contains(double centerX, double centerZ, double borderSize,
                                   double x, double z, double depthBlocks) {
        if (!Double.isFinite(depthBlocks) || depthBlocks < 0.0D) return false;
        double distance = distanceInsideBorder(centerX, centerZ, borderSize, x, z);
        return Double.isFinite(distance) && distance >= 0.0D && distance <= depthBlocks;
    }

    /** Reads the live configured border and configured endgame depth from the level. */
    public static boolean contains(net.minecraft.world.level.Level level, double x, double z) {
        var border = level.getWorldBorder();
        return contains(border.getCenterX(), border.getCenterZ(), border.getSize(),
                x, z, OutlandsConfig.ALMIGHTY_TERRITORY_DEPTH.get());
    }

    public static double distanceInsideBorder(net.minecraft.world.level.Level level, double x, double z) {
        var border = level.getWorldBorder();
        return distanceInsideBorder(border.getCenterX(), border.getCenterZ(), border.getSize(), x, z);
    }
}
