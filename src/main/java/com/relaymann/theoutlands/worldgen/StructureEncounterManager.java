package com.relaymann.theoutlands.worldgen;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.NightmareMode;
import com.relaymann.theoutlands.entity.OutlandsEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;

public final class StructureEncounterManager {
    private static final long ENCOUNTER_SALT = 0x3C6EF372FE94F82BL;

    private StructureEncounterManager() {}

    public static void tick(ServerLevel level) {
        if (!level.dimension().equals(Level.OVERWORLD)
                || !OutlandsConfig.OUTLANDS_STRUCTURES_ENABLED.get()
                || !OutlandsConfig.ENTITY_SPAWN_BALANCE_ENABLED.get()
                || !OutlandsConfig.ENTITY_ECOSYSTEM_ENABLED.get()) return;
        StructureEncounterSavedData resolvedSites = StructureEncounterSavedData.get(level);
        for (ServerPlayer player : level.players()) {
            if (!player.isAlive() || player.isCreative() || player.isSpectator()
                    || player.isSleeping() || player.isPassenger()) continue;
            var start = level.structureManager().getStructureWithPieceAt(player.blockPosition(),
                    holder -> holder.value() instanceof OutlandsStructure);
            if (start == null || !start.isValid()) continue;
            var bounds = start.getBoundingBox();
            if (!bounds.isInside(player.blockPosition())) continue;
            int centerX = bounds.minX() + 24;
            int centerZ = bounds.minZ() + 24;
            int centerChunkX = Math.floorDiv(centerX, 16);
            int centerChunkZ = Math.floorDiv(centerZ, 16);
            long seed = StructurePlacementPolicy.chunkSeed(level.getSeed(), centerChunkX, centerChunkZ);
            String key = level.dimension().location() + ":" + level.getSeed()
                    + ":" + bounds.minX() + ":" + bounds.minZ();
            BlockPos center = new BlockPos(centerX, bounds.maxY() - 12, centerZ);
            if (!level.hasChunkAt(center)) continue;
            double dx = centerX - level.getSharedSpawnPos().getX();
            double dz = centerZ - level.getSharedSpawnPos().getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            String biome = level.getBiome(center).unwrapKey().map(k -> k.location().getPath()).orElse("");
            boolean forest = biome.contains("forest") || biome.contains("taiga")
                    || biome.contains("grove") || biome.contains("wooded");
            boolean nightmare = NightmareMode.isEnabled(level);
            double minimumDistance = nightmare
                    ? Math.max(250.0D, OutlandsConfig.STRUCTURE_MIN_DISTANCE.get() * 0.25D)
                    : Math.max(1_000.0D, OutlandsConfig.STRUCTURE_MIN_DISTANCE.get());
            double frequency = OutlandsConfig.STRUCTURE_CHANCE_MULTIPLIER.get() * (nightmare ? 2.5D : 1.0D);
            String noLayoutKey = key + ":no-layout:" + nightmare + ":"
                    + Double.doubleToLongBits(minimumDistance) + ":" + Double.doubleToLongBits(frequency);
            if (resolvedSites.hasResolved(key) || resolvedSites.hasResolved(noLayoutKey)) continue;
            String layout = OutlandsStructureFeature.chooseLayout(seed, distance, forest,
                    minimumDistance, frequency, nightmare);
            if (layout == null) {

                resolvedSites.markResolved(noLayoutKey);
                continue;
            }

            if (resolve(level, layout, center, distance, seed)) {
                resolvedSites.markResolved(key);
            }
        }
    }

    private static boolean resolve(ServerLevel level, String structure, BlockPos center,
                                   double distance, long seed) {
        RandomSource random = RandomSource.create(seed ^ ENCOUNTER_SALT);
        EntityType<?> selected = null;
        int denominator = 0;
        switch (structure) {
            case "cabin" -> { selected = OutlandsEntities.LURKER.get(); denominator = 22; }
            case "tower" -> { selected = OutlandsEntities.OBSERVER.get(); denominator = 36; }
            case "bunker" -> { selected = OutlandsEntities.CAVE_JAR.get(); denominator = 32; }
            case "shrine" -> { if (distance >= 15_000) { selected = OutlandsEntities.NULL_APOSTLE.get(); denominator = 90; } }
            case "observatory" -> { if (distance >= 22_000) { selected = OutlandsEntities.NULL_APOSTLE.get(); denominator = 70; } }
            case "archive" -> {
                if (distance >= OutlandsConfig.DEATH_FIRST_APPEARANCE_DISTANCE.get() && random.nextInt(240) == 0)
                    selected = OutlandsEntities.DEATH.get();
                else { selected = OutlandsEntities.DREDGE.get(); denominator = 32; }
            }
            case "research_facility" -> { selected = OutlandsEntities.STILLBORN_STATUE.get(); denominator = 52; }
            case "apostle_cathedral" -> { if (distance >= 12_000) { selected = OutlandsEntities.NULL_APOSTLE.get(); denominator = 34; } }
            case "forsaken_settlement" -> { selected = OutlandsEntities.MIMIC.get(); denominator = 26; }
            case "last_outpost" -> { selected = OutlandsEntities.HUNTER.get(); denominator = 24; }
            case "forgotten_station" -> { selected = OutlandsEntities.DREDGE.get(); denominator = 24; }
            case "null_chamber" -> {
                if (distance >= 25_000 && OutlandsConfig.NULL_SIGHTINGS_ENABLED.get() && random.nextInt(320) == 0)
                    selected = OutlandsEntities.NULL.get();
                else { selected = OutlandsEntities.NULL_APOSTLE.get(); denominator = 90; }
            }
            case "hollow_crypt" -> {
                if (distance >= OutlandsConfig.DEATH_FIRST_APPEARANCE_DISTANCE.get() && random.nextInt(280) == 0)
                    selected = OutlandsEntities.DEATH.get();
                else { selected = OutlandsEntities.DREDGE.get(); denominator = 38; }
            }
            case "boundary_monument" -> { if (distance >= 18_000) { selected = OutlandsEntities.NULL_APOSTLE.get(); denominator = 100; } }
            case "ranger_station" -> { selected = OutlandsEntities.OBSERVER.get(); denominator = 48; }
            case "mining_operation" -> { selected = OutlandsEntities.DREDGE.get(); denominator = 44; }
            case "ruined_settlement" -> { selected = OutlandsEntities.MIMIC.get(); denominator = 38; }
            case "observation_tower" -> { selected = OutlandsEntities.OBSERVER.get(); denominator = 52; }
            case "subterranean_complex" -> { selected = OutlandsEntities.STILLBORN_STATUE.get(); denominator = 60; }
            case "nightmare_containment" -> { selected = OutlandsEntities.NIGHTMARE_WARDENED.get(); denominator = 5; }
            case "blood_moon_shrine" -> { selected = OutlandsEntities.NIGHTMARE_UNBOUND.get(); denominator = 5; }
            case "nightmare_outpost" -> { selected = OutlandsEntities.NIGHTMARE_HOUND.get(); denominator = 4; }
            case "black_site" -> {
                selected = random.nextBoolean() ? OutlandsEntities.NIGHTMARE_FALSE_SURVIVOR.get()
                        : OutlandsEntities.NIGHTMARE_WATCHER.get();
                denominator = 4;
            }
            case "nightmare_signal_hollow" -> {
                if (NightmareMode.isEnabled(level)) {
                    selected = OutlandsEntities.NIGHTMARE_WATCHER.get();
                    denominator = 3;
                }
            }
        }
        if (NightmareMode.isEnabled(level) && denominator > 0) denominator = Math.max(3, denominator / 3);
        if (selected == null || (denominator > 0 && random.nextInt(denominator) != 0)) return true;
        int spawnY = switch (structure) {
            case "bunker" -> center.getY() - 12;
            case "archive" -> center.getY() - 25;
            case "research_facility" -> center.getY() - 10;
            case "forgotten_station" -> center.getY() - 8;
            case "null_chamber" -> center.getY() - 18;
            case "hollow_crypt" -> center.getY() - 22;
            default -> center.getY() + 1;
        };
        BlockPos spawn = new BlockPos(center.getX(), spawnY, center.getZ());
        if (!level.hasChunkAt(spawn)) return false;
        float yaw = random.nextFloat() * 360.0F;
        int[][] offsets = {{0,0},{2,0},{-2,0},{0,2},{0,-2},{2,2},{2,-2},{-2,2},{-2,-2}};
        for (int[] offset : offsets) {
            Entity entity = selected.create(level);
            if (entity == null) continue;
            entity.moveTo(spawn.getX() + offset[0] + 0.5D, spawn.getY(),
                    spawn.getZ() + offset[1] + 0.5D, yaw, 0.0F);
            if (entity instanceof Mob mob) {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawn), MobSpawnType.EVENT, null);
            }
            if (level.noCollision(entity) && level.addFreshEntity(entity)) return true;
            entity.discard();
        }
        return false;
    }

}
