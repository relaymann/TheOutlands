package com.relaymann.theoutlands.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import com.relaymann.theoutlands.TheOutlands;

/** Server-side spawn safety and habitat rules for every Outlands entity. */
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
    }

    private static <T extends net.minecraft.world.entity.Mob> void registerGround(
            RegisterSpawnPlacementsEvent event,
            net.minecraft.world.entity.EntityType<T> type,
            net.minecraft.world.entity.SpawnPlacements.SpawnPredicate<T> predicate) {
        event.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static boolean caveSpawn(net.minecraft.world.entity.EntityType<? extends Monster> type,
            ServerLevelAccessor level, net.minecraft.world.entity.MobSpawnType reason,
            BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                && pos.getY() <= 48
                && level.getBrightness(LightLayer.BLOCK, pos) <= 7;
    }

    private static boolean deepSpawn(net.minecraft.world.entity.EntityType<? extends Monster> type,
            ServerLevelAccessor level, net.minecraft.world.entity.MobSpawnType reason,
            BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                && pos.getY() <= 24
                && level.getBrightness(LightLayer.BLOCK, pos) <= 4;
    }

    private static boolean statueSpawn(net.minecraft.world.entity.EntityType<? extends Monster> type,
            ServerLevelAccessor level, net.minecraft.world.entity.MobSpawnType reason,
            BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                && pos.getY() <= 64
                && level.getBrightness(LightLayer.BLOCK, pos) <= 5;
    }


    private static boolean woodlandSpawn(net.minecraft.world.entity.EntityType<? extends Monster> type,
            ServerLevelAccessor level, net.minecraft.world.entity.MobSpawnType reason,
            BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                && level.getBrightness(LightLayer.BLOCK, pos) <= 9
                && level.getBiome(pos).is(net.minecraft.tags.BiomeTags.IS_FOREST);
    }

    private static boolean dredgeSpawn(net.minecraft.world.entity.EntityType<? extends Monster> type,
            ServerLevelAccessor level, net.minecraft.world.entity.MobSpawnType reason,
            BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                && level.getBrightness(LightLayer.BLOCK, pos) <= 8
                && pos.getY() <= 80;
    }

    private static boolean almightySpawn(net.minecraft.world.entity.EntityType<? extends Monster> type,
            ServerLevelAccessor level, net.minecraft.world.entity.MobSpawnType reason,
            BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                && random.nextInt(160) == 0;
    }
}
