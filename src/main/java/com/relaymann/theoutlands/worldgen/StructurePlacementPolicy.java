package com.relaymann.theoutlands.worldgen;

/** Seed-stable spacing policy for additive Outlands structure features. */
public final class StructurePlacementPolicy {
    public static final int CELL_CHUNKS = 8;
    private static final long X_SALT = 0x9E3779B97F4A7C15L;
    private static final long Z_SALT = 0xC2B2AE3D27D4EB4FL;

    private StructurePlacementPolicy() {}

    /** Prevents structure footprints from overlapping even when config spacing is set too low. */
    public static int minimumSpacingForFootprint(int configuredSpacingChunks, int footprintSizeBlocks) {
        if (footprintSizeBlocks < 1) throw new IllegalArgumentException("footprintSizeBlocks must be positive");
        int footprintChunks = (footprintSizeBlocks + 15) / 16;
        return Math.max(Math.max(1, configuredSpacingChunks), footprintChunks);
    }

    /** Allows small terrain variation but rejects chunks whose actual ground is too far from the anchor height. */
    public static boolean isWithinSurfaceTolerance(int sampledSurfaceY, int expectedSurfaceY, int tolerance) {
        return tolerance >= 0
                && Math.abs((long) sampledSurfaceY - expectedSurfaceY) <= tolerance;
    }

    public static boolean isSpacedAnchor(long seed, int chunkX, int chunkZ, int minimumSpacing) {
        return isSpacedAnchor(seed, chunkX, chunkZ, minimumSpacing, CELL_CHUNKS);
    }

    /** NIGHTMARE uses smaller deterministic placement cells without changing vanilla world generation. */
    public static boolean isSpacedAnchor(long seed, int chunkX, int chunkZ,
                                         int minimumSpacing, int cellChunks) {
        if (cellChunks < 1 || (cellChunks & (cellChunks - 1)) != 0)
            throw new IllegalArgumentException("cellChunks must be a positive power of two");
        long cellX = Math.floorDiv(chunkX, cellChunks);
        long cellZ = Math.floorDiv(chunkZ, cellChunks);
        int[] ownAnchor = anchorForCell(seed, cellX, cellZ, cellChunks);
        if (ownAnchor[0] != chunkX || ownAnchor[1] != chunkZ) return false;
        long ownPriority = priority(seed, cellX, cellZ);
        int spacing = Math.max(1, minimumSpacing);
        int radius = (spacing + cellChunks - 1) / cellChunks + 1;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) continue;
                long otherX = cellX + dx;
                long otherZ = cellZ + dz;
                int[] other = anchorForCell(seed, otherX, otherZ, cellChunks);
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
        return anchorForCell(seed, cellX, cellZ, CELL_CHUNKS);
    }

    public static int[] anchorForCell(long seed, long cellX, long cellZ, int cellChunks) {
        if (cellChunks < 1 || (cellChunks & (cellChunks - 1)) != 0)
            throw new IllegalArgumentException("cellChunks must be a positive power of two");
        long value = priority(seed, cellX, cellZ);
        int shift = Integer.numberOfTrailingZeros(cellChunks);
        int mask = cellChunks - 1;
        int offsetX = (int) (value & mask);
        int offsetZ = (int) ((value >>> shift) & mask);
        return new int[] {(int) (cellX * cellChunks + offsetX), (int) (cellZ * cellChunks + offsetZ)};
    }

    /** Evenly samples a footprint and always includes its final block coordinate. */
    public static int[] surfaceSampleOffsets(int size, int step) {
        if (size < 1 || step < 1) throw new IllegalArgumentException("size and step must be positive");
        int last = size - 1;
        int count = last / step + 1 + (last % step == 0 ? 0 : 1);
        int[] offsets = new int[count];
        int index = 0;
        for (int offset = 0; offset <= last; offset += step) offsets[index++] = offset;
        if (last % step != 0) offsets[index] = last;
        return offsets;
    }

    /** Inclusive block clipping shared by every piece write and its tests. */
    public static boolean containsBlock(int minX, int minY, int minZ,
                                        int maxX, int maxY, int maxZ,
                                        int x, int y, int z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
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
