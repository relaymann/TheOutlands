package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.NightmareMode;
import com.relaymann.theoutlands.event.NightmareModeManager;
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
        registerGround(event, OutlandsEntities.PILGRIM.get(), OutlandsSpawnEvents::pilgrimSpawn);
        registerGround(event, OutlandsEntities.CEILING_DWELLER.get(), OutlandsSpawnEvents::ceilingSpawn);
        registerGround(event, OutlandsEntities.COLOSSUS.get(), OutlandsSpawnEvents::colossusSpawn);
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

        boolean nightmare = NightmareMode.isEnabled(serverLevel);
        double minimumDistance = profile.minimumDistance();
        int denominator = profile.rarityMultiplier();
        if (nightmare) {
            double distanceScale = type == OutlandsEntities.DEATH.get() ? 0.25D
                    : type == OutlandsEntities.FEAR.get() ? 0.22D
                    : type == OutlandsEntities.COLOSSUS.get() ? 0.15D : 0.10D;
            minimumDistance *= distanceScale;
            denominator = Math.max(1, (int) Math.ceil(denominator / 3.0D));
        }
        if (distance < minimumDistance) return false;

        // Distance is a progression gate, not a second rarity penalty. Once an
        // entity reaches its intended territory, only its own rarity applies.
        if (denominator > 1 && random.nextInt(denominator) != 0) return false;

        // Budget around the candidate spawn location rather than the nearest
        // player. This also constrains chunk-generation spawns, which can run
        // before a player is close enough to be found.
        boolean bloodMoon = nightmare && NightmareModeManager.isBloodMoonActive(serverLevel);
        double encounterRadius = nightmare ? 112.0D : 72.0D;
        List<Mob> nearby = serverLevel.getEntitiesOfClass(Mob.class,
                new AABB(pos).inflate(encounterRadius),
                OutlandsSpawnEvents::isOutlandsCreature);
        int threatPoints = 0;
        for (Mob mob : nearby) {
            SpawnProfile existing = profile(mob.getType());
            threatPoints += existing == null ? 1 : existing.threatCost();
        }

        int maxEntities = nightmare
                ? (distance < 1_000.0D ? 12 : distance < 2_000.0D ? 16
                : distance < 4_000.0D ? 20 : distance < 7_000.0D ? 22 : 24)
                : (distance < 1_000.0D ? 4 : distance < 2_000.0D ? 5
                : distance < 4_000.0D ? 7 : distance < 7_000.0D ? 8 : 9);
        int threatBudget = nightmare
                ? (distance < 1_000.0D ? 22 : distance < 2_000.0D ? 30
                : distance < 4_000.0D ? 40 : distance < 7_000.0D ? 48 : 56)
                : (distance < 1_000.0D ? 5 : distance < 2_000.0D ? 7
                : distance < 4_000.0D ? 10 : distance < 7_000.0D ? 13 : 16);
        if (bloodMoon) {
            maxEntities += 5;
            threatBudget += 12;
        }

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
                || type == OutlandsEntities.HOWLER.get() || type == OutlandsEntities.DREDGE.get()
                || type == OutlandsEntities.PILGRIM.get() || type == OutlandsEntities.CEILING_DWELLER.get() || type == OutlandsEntities.COLOSSUS.get();
    }

    private static SpawnProfile profile(EntityType<?> type) {
        // Common territorial hunters begin close enough that a normal first
        // session reliably encounters the mod, without flooding the spawn area.
        if (type == OutlandsEntities.LURKER.get() || type == OutlandsEntities.DREDGE.get()
                || type == OutlandsEntities.STALKER.get()) return new SpawnProfile(250.0D, 1, 1);
        if (type == OutlandsEntities.HUNTER.get() || type == OutlandsEntities.OBSERVER.get()
                || type == OutlandsEntities.BURROWER.get() || type == OutlandsEntities.MIMIC.get()) {
            return new SpawnProfile(500.0D, 2, 1);
        }
        if (type == OutlandsEntities.HOWLER.get() || type == OutlandsEntities.PILGRIM.get()) {
            return new SpawnProfile(600.0D, 2, 1);
        }
        if (type == OutlandsEntities.CAVE_JAR.get()) return new SpawnProfile(750.0D, 3, 1);
        if (type == OutlandsEntities.CEILING_DWELLER.get()) return new SpawnProfile(900.0D, 3, 2);
        if (type == OutlandsEntities.STILLBORN_STATUE.get()
                || type == OutlandsEntities.AGITATED_ONE.get()) return new SpawnProfile(900.0D, 3, 2);
        if (type == OutlandsEntities.COLOSSUS.get()) return new SpawnProfile(3_000.0D, 6, 5);
        if (type == OutlandsEntities.NULL.get() || type == OutlandsEntities.NULL_APOSTLE.get()) {
            return new SpawnProfile(2_000.0D, 4, 3);
        }
        if (type == OutlandsEntities.FEAR.get()) {
            return new SpawnProfile(OutlandsConfig.FEAR_FIRST_APPEARANCE_DISTANCE.get(), 8, 6);
        }
        if (type == OutlandsEntities.DEATH.get()) {
            return new SpawnProfile(OutlandsConfig.DEATH_FIRST_APPEARANCE_DISTANCE.get(), 8, 10);
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


    private static boolean pilgrimSpawn(EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                && level.getBrightness(LightLayer.BLOCK, pos) <= 10
                && level.getBiome(pos).is(net.minecraft.tags.BiomeTags.IS_FOREST);
    }
    private static boolean ceilingSpawn(EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        if(!caveSpawn(type,level,reason,pos,random))return false;
        for(int dy=3;dy<=8;dy++){BlockPos ceiling=pos.above(dy);if(level.getBlockState(ceiling).isSolidRender(level,ceiling))return true;}return false;
    }
    private static boolean colossusSpawn(EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type,level,reason,pos,random)&&level.getBrightness(LightLayer.BLOCK,pos)<=8&&(level.getBiome(pos).is(net.minecraft.tags.BiomeTags.IS_FOREST)||level.getBiome(pos).is(net.minecraft.tags.BiomeTags.IS_MOUNTAIN));
    }
    private static boolean almightySpawn(EntityType<? extends Monster> type,
            ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                && random.nextInt(80) == 0;
    }
}
