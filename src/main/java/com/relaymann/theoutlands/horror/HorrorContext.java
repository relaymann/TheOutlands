package com.relaymann.theoutlands.horror;
public record HorrorContext(String biomeId, boolean night, boolean raining, boolean thundering,
                            int nearbyPlayers, float healthFraction) {
    public HorrorContext {
        biomeId = biomeId == null ? "unknown" : biomeId;
        nearbyPlayers = Math.max(0, nearbyPlayers);
        healthFraction = Float.isFinite(healthFraction) ? Math.max(0.0F, Math.min(1.0F, healthFraction)) : 1.0F;
    }
    public static HorrorContext neutral() { return new HorrorContext("unknown", false, false, false, 0, 1.0F); }
    public double weightMultiplier(String id) {
        double weight = 1.0D;
        if ("distant_figure".equals(id) && night) weight *= 1.55D;
        if ("environmental_distortion".equals(id) && (raining || thundering)) weight *= 1.45D;
        if ("hostile_warning".equals(id) && healthFraction <= 0.35F) weight *= 1.5D;
        if ("direct_intervention".equals(id) && nearbyPlayers > 0) weight *= 0.8D;
        if ("ambient_displacement".equals(id) && biomeId.contains("forest")) weight *= 1.2D;
        return weight;
    }
}
