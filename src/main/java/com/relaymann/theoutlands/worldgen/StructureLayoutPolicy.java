package com.relaymann.theoutlands.worldgen;

import java.util.Random;

/** Pure, seed-stable structure identity selection; intentionally independent of Minecraft runtime classes. */
public final class StructureLayoutPolicy {
    private static final String[] IDS = {
            "cabin", "tower", "bunker", "shrine", "observatory", "archive",
            "research_facility", "apostle_cathedral", "forsaken_settlement", "last_outpost",
            "forgotten_station", "null_chamber", "hollow_crypt", "boundary_monument"
    };

    private StructureLayoutPolicy() {}

    public static String chooseLayout(long seed, double distance, boolean forest,
                                      double configuredMinimum, double multiplier) {
        if (!Double.isFinite(distance) || distance < configuredMinimum || multiplier <= 0.0D) return null;
        double scale = StructurePlacementPolicy.CELL_CHUNKS * StructurePlacementPolicy.CELL_CHUNKS;
        double[] weights = new double[14];
        weights[0] = forest ? weight(distance, 1_000, 5_500, multiplier, scale) : 0;
        weights[1] = weight(distance, 1_800, 7_000, multiplier, scale);
        weights[2] = weight(distance, 2_500, 8_500, multiplier, scale);
        weights[3] = weight(distance, 15_000, 14_000, multiplier, scale);
        weights[4] = forest ? weight(distance, 22_000, 20_000, multiplier, scale) : 0;
        weights[5] = weight(distance, 50_000, 32_000, multiplier, scale);
        weights[6] = weight(distance, 3_000, 10_000, multiplier, scale);
        weights[7] = weight(distance, 12_000, 16_000, multiplier, scale);
        weights[8] = weight(distance, 2_000, 8_000, multiplier, scale);
        weights[9] = weight(distance, 4_000, 12_000, multiplier, scale);
        weights[10] = weight(distance, 5_000, 15_000, multiplier, scale);
        weights[11] = weight(distance, 25_000, 28_000, multiplier, scale);
        weights[12] = weight(distance, 35_000, 30_000, multiplier, scale);
        weights[13] = weight(distance, 18_000, 22_000, multiplier, scale);
        double total = 0.0D;
        for (double weight : weights) total += weight;
        if (total <= 0.0D) return null;
        Random random = new Random(seed);
        if (random.nextDouble() >= Math.min(1.0D, total)) return null;
        double pick = random.nextDouble() * total;
        for (int i = 0; i < weights.length; i++) {
            pick -= weights[i];
            if (pick < 0.0D) return IDS[i];
        }
        return IDS[IDS.length - 1];
    }

    private static double weight(double distance, int minimum, int denominator,
                                 double multiplier, double scale) {
        return distance >= minimum ? scale * multiplier / denominator : 0.0D;
    }
}
