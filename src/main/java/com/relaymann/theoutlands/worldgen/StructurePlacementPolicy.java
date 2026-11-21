package com.relaymann.theoutlands.worldgen;

/** Seed-stable spacing policy for additive Outlands structure features. */
public final class StructurePlacementPolicy {
    public static final int CELL_CHUNKS = 8;
    private static final long X_SALT = 0x9E3779B97F4A7C15L;
    private static final long Z_SALT = 0xC2B2AE3D27D4EB4FL;

    private StructurePlacementPolicy() {}

    public static boolean isSpacedAnchor(long seed, int chunkX, int chunkZ, int minimumSpacing) {
        long cellX = Math.floorDiv(chunkX, CELL_CHUNKS);
        long cellZ = Math.floorDiv(chunkZ, CELL_CHUNKS);
        int[] ownAnchor = anchorForCell(seed, cellX, cellZ);
        if (ownAnchor[0] != chunkX || ownAnchor[1] != chunkZ) return false;
        long ownPriority = priority(seed, cellX, cellZ);
        int spacing = Math.max(1, minimumSpacing);
        int radius = (spacing + CELL_CHUNKS - 1) / CELL_CHUNKS + 1;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) continue;
                long otherX = cellX + dx;
                long otherZ = cellZ + dz;
                int[] other = anchorForCell(seed, otherX, otherZ);
                if (Math.max(Math.abs(other[0] - chunkX), Math.abs(other[1] - chunkZ)) >= spacing) continue;
                long otherPriority = priority(seed, otherX, otherZ);
                if (Long.compareUnsigned(otherPriority, ownPriority) < 0
                        || (otherPriority == ownPriority && (otherX < cellX
                        || (otherX == cellX && otherZ < cellZ)))) return false;
            }
        }
        return true;
    }

    public static int[] anchorForCell(long seed, long cellX, long cellZ) {
        long value = priority(seed, cellX, cellZ);
        int offsetX = (int) (value & (CELL_CHUNKS - 1));
        int offsetZ = (int) ((value >>> 3) & (CELL_CHUNKS - 1));
        return new int[] {(int) (cellX * CELL_CHUNKS + offsetX), (int) (cellZ * CELL_CHUNKS + offsetZ)};
    }

    public static long chunkSeed(long seed, int chunkX, int chunkZ) {
        return mix64(seed ^ ((long) chunkX * X_SALT) ^ Long.rotateLeft((long) chunkZ * Z_SALT, 27));
    }

    private static long priority(long seed, long cellX, long cellZ) {
        return mix64(seed ^ (cellX * X_SALT) ^ Long.rotateLeft(cellZ * Z_SALT, 27));
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }
}
