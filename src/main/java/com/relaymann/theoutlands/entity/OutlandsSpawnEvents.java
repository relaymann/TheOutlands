package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

import java.util.List;

/** Server-side habitat rules and distance/threat-budget balancing for natural spawns. */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class OutlandsSpawnEvents {
    private static final double ENCOUNTER_RADIUS = 96.0D;

    private OutlandsSpawnEvents() {}

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        registerGround(event, OutlandsEntities.HUNTER.get(), Monster::checkMonsterSpawnRules);
        registerGround(event, OutlandsEntities.MIMIC.get(), Monster::checkMonsterSpawnRules);
        registerGround(event, OutlandsEntities.STALKER.get(), Monster::checkMonsterSpawnRules);
        registerGround(event, OutlandsEntities.CAVE_JAR.get(), OutlandsSpawnEvents::caveSpawn);
        registerGround(event, OutlandsEntities.BURROWER.get(), OutlandsSpawnEvents::caveSpawn);
        registerGround(event, OutlandsEntities.DEATH.get(), OutlandsSpawnEvents::deepSpawn);
        registerGround(event, OutlandsEntities.ALMIGHTY.get(), OutlandsSpawnEvents::almightySpawn);
        registerGround(event, OutlandsEntities.NULL.get(), Monster::checkMonsterSpawnRules);
        registerGround(event, OutlandsEntities.FEAR.get(), Monster::checkMonsterSpawnRules);
        registerGround(event, OutlandsEntities.NULL_APOSTLE.get(), Monster::checkMonsterSpawnRules);
        registerGround(event, OutlandsEntities.OBSERVER.get(), Monster::checkMonsterSpawnRules);
        registerGround(event, OutlandsEntities.STILLBORN_STATUE.get(), OutlandsSpawnEvents::statueSpawn);
        registerGround(event, OutlandsEntities.AGITATED_ONE.get(), Monster::checkMonsterSpawnRules);
        registerGround(event, OutlandsEntities.LURKER.get(), OutlandsSpawnEvents::woodlandSpawn);
        registerGround(event, OutlandsEntities.HOWLER.get(), Monster::checkMonsterSpawnRules);
        registerGround(event, OutlandsEntities.DREDGE.get(), OutlandsSpawnEvents::dredgeSpawn);
    }

    private static <T extends Mob> void registerGround(
            RegisterSpawnPlacementsEvent event,
            EntityType<T> type,
            SpawnPlacements.SpawnPredicate<T> habitatPredicate) {
        SpawnPlacements.SpawnPredicate<T> balancedPredicate = (entityType, level, reason, pos, random) -> {
            if (!habitatPredicate.test(entityType, level, reason, pos, random)) return false;
            return passesNaturalSpawnBalance(entityType, level, reason, pos, random);
        };
        event.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                balancedPredicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static <T extends Mob> boolean passesNaturalSpawnBalance(
            EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason,
            BlockPos pos, RandomSource random) {
        if (!OutlandsConfig.ENTITY_SPAWN_BALANCE_ENABLED.get()) return true;
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) return true;

        ServerLevel serverLevel = level.getLevel();
        // Almighty has its own End-only rarity rule and is deliberately outside
        // the Overworld distance progression and encounter budget.
        if (!serverLevel.dimension().equals(Level.OVERWORLD) || type == OutlandsEntities.ALMIGHTY.get()) return true;

        SpawnProfile profile = profile(type);
        if (profile == null) return true;

        BlockPos worldSpawn = serverLevel.getSharedSpawnPos();
        double distance = Math.hypot(
                pos.getX() - worldSpawn.getX(),
                pos.getZ() - worldSpawn.getZ());

        if (distance < profile.minimumDistance()) return false;

        int distanceDenominator = distance < 1_000.0D ? 32
                : distance < 2_000.0D ? 12
                : distance < 4_000.0D ? 4
                : distance < 7_000.0D ? 2 : 1;
        int denominator = distanceDenominator * profile.rarityMultiplier();
        if (denominator > 1 && random.nextInt(denominator) != 0) return false;

        // Budget around the candidate spawn location rather than the nearest
        // player. This also constrains chunk-generation spawns, which can run
        // before a player is close enough to be found.
        List<Mob> nearby = serverLevel.getEntitiesOfClass(Mob.class,
                new AABB(pos).inflate(ENCOUNTER_RADIUS),
                OutlandsSpawnEvents::isOutlandsCreature);
        int threatPoints = 0;
        for (Mob mob : nearby) {
            SpawnProfile existing = profile(mob.getType());
            threatPoints += existing == null ? 1 : existing.threatCost();
        }

        int maxEntities = distance < 1_000.0D ? 1
                : distance < 2_000.0D ? 2
                : distance < 4_000.0D ? 3
                : distance < 7_000.0D ? 4 : 5;
        int threatBudget = distance < 1_000.0D ? 1
                : distance < 2_000.0D ? 2
                : distance < 4_000.0D ? 4
                : distance < 7_000.0D ? 6 : 9;

        return nearby.size() < maxEntities
                && threatPoints + profile.threatCost() <= threatBudget;
    }

    private static boolean isOutlandsCreature(Mob mob) {
        EntityType<?> type = mob.getType();
        return type == OutlandsEntities.NULL.get() || type == OutlandsEntities.FEAR.get()
                || type == OutlandsEntities.DEATH.get() || type == OutlandsEntities.ALMIGHTY.get()
                || type == OutlandsEntities.NULL_APOSTLE.get() || type == OutlandsEntities.CAVE_JAR.get()
                || type == OutlandsEntities.STALKER.get() || type == OutlandsEntities.OBSERVER.get()
                || type == OutlandsEntities.HUNTER.get() || type == OutlandsEntities.BURROWER.get()
                || type == OutlandsEntities.MIMIC.get() || type == OutlandsEntities.STILLBORN_STATUE.get()
                || type == OutlandsEntities.AGITATED_ONE.get() || type == OutlandsEntities.LURKER.get()
                || type == OutlandsEntities.HOWLER.get() || type == OutlandsEntities.DREDGE.get();
    }

    private static SpawnProfile profile(EntityType<?> type) {
        if (type == OutlandsEntities.LURKER.get() || type == OutlandsEntities.DREDGE.get()
                || type == OutlandsEntities.STALKER.get()) return new SpawnProfile(500.0D, 1, 1);
        if (type == OutlandsEntities.HOWLER.get()) return new SpawnProfile(1_500.0D, 2, 2);
        if (type == OutlandsEntities.HUNTER.get() || type == OutlandsEntities.OBSERVER.get()
                || type == OutlandsEntities.BURROWER.get() || type == OutlandsEntities.MIMIC.get()) {
            return new SpawnProfile(1_800.0D, 2, 2);
        }
        if (type == OutlandsEntities.CAVE_JAR.get()) return new SpawnProfile(2_500.0D, 4, 3);
        if (type == OutlandsEntities.STILLBORN_STATUE.get()
                || type == OutlandsEntities.AGITATED_ONE.get()) return new SpawnProfile(3_500.0D, 4, 4);
        if (type == OutlandsEntities.NULL.get() || type == OutlandsEntities.NULL_APOSTLE.get()) {
            return new SpawnProfile(7_000.0D, 7, 8);
        }
        if (type == OutlandsEntities.FEAR.get()) {
            return new SpawnProfile(OutlandsConfig.FEAR_FIRST_APPEARANCE_DISTANCE.get(), 10, 16);
        }
        if (type == OutlandsEntities.DEATH.get()) {
            return new SpawnProfile(OutlandsConfig.DEATH_FIRST_APPEARANCE_DISTANCE.get(), 10, 32);
        }
        return null;
    }

    private record SpawnProfile(double minimumDistance, int threatCost, int rarityMultiplier) {}

    private static boolean caveSpawn(EntityType<? extends Monster> type,
            ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                && pos.getY() <= 48
                && level.getBrightness(LightLayer.BLOCK, pos) <= 7;
    }

    private static boolean deepSpawn(EntityType<? extends Monster> type,
            ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                && pos.getY() <= 24
                && level.getBrightness(LightLayer.BLOCK, pos) <= 4;
    }

    private static boolean statueSpawn(EntityType<? extends Monster> type,
            ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                && pos.getY() <= 64
                && level.getBrightness(LightLayer.BLOCK, pos) <= 5;
    }

    private static boolean woodlandSpawn(EntityType<? extends Monster> type,
            ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                && level.getBrightness(LightLayer.BLOCK, pos) <= 9
                && level.getBiome(pos).is(net.minecraft.tags.BiomeTags.IS_FOREST);
    }

    private static boolean dredgeSpawn(EntityType<? extends Monster> type,
            ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                && level.getBrightness(LightLayer.BLOCK, pos) <= 8
                && pos.getY() <= 80;
    }

    private static boolean almightySpawn(EntityType<? extends Monster> type,
            ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                && random.nextInt(160) == 0;
    }
}
