package com.relaymann.theoutlands.worldgen;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.entity.OutlandsEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;

/** Very rare, one-time structure encounters; no encounter is guaranteed by finding a site. */
public final class StructureEncounterManager {
    private StructureEncounterManager() {}

    public static void trySpawn(String structure,WorldGenLevel level,BlockPos center,
                                double distance,long seed) {
        if(!OutlandsConfig.ENTITY_SPAWN_BALANCE_ENABLED.get()
                || !OutlandsConfig.ENTITY_ECOSYSTEM_ENABLED.get()) return;
        RandomSource random=RandomSource.create(seed ^ 0x3C6EF372FE94F82BL);
        EntityType<?> selected=null;
        int denominator=0;
        switch(structure) {
            case "cabin" -> { selected=OutlandsEntities.LURKER.get(); denominator=22; }
            case "tower" -> { selected=OutlandsEntities.OBSERVER.get(); denominator=36; }
            case "bunker" -> { selected=OutlandsEntities.CAVE_JAR.get(); denominator=32; }
            case "shrine" -> { if(distance>=15_000) { selected=OutlandsEntities.NULL_APOSTLE.get(); denominator=90; } }
            case "observatory" -> { if(distance>=22_000) { selected=OutlandsEntities.NULL_APOSTLE.get(); denominator=70; } }
            case "archive" -> {
                if(distance>=50_000 && random.nextInt(240)==0) selected=OutlandsEntities.DEATH.get();
                else { selected=OutlandsEntities.DREDGE.get(); denominator=32; }
            }
            case "research_facility" -> { selected=OutlandsEntities.STILLBORN_STATUE.get(); denominator=52; }
            case "apostle_cathedral" -> { if(distance>=12_000) { selected=OutlandsEntities.NULL_APOSTLE.get(); denominator=34; } }
            case "forsaken_settlement" -> { selected=OutlandsEntities.MIMIC.get(); denominator=26; }
            case "last_outpost" -> { selected=OutlandsEntities.HUNTER.get(); denominator=24; }
            case "forgotten_station" -> { selected=OutlandsEntities.DREDGE.get(); denominator=24; }
            case "null_chamber" -> {
                if(distance>=25_000 && random.nextInt(320)==0) selected=OutlandsEntities.NULL.get();
                else { selected=OutlandsEntities.NULL_APOSTLE.get(); denominator=90; }
            }
            case "hollow_crypt" -> {
                if(distance>=50_000 && random.nextInt(280)==0) selected=OutlandsEntities.DEATH.get();
                else { selected=OutlandsEntities.DREDGE.get(); denominator=38; }
            }
            case "boundary_monument" -> {
                if(distance>=18_000) { selected=OutlandsEntities.NULL_APOSTLE.get(); denominator=100; }
            }
        }
        if(selected==null || (denominator>0 && random.nextInt(denominator)!=0)) return;
        Entity entity=selected.create(level.getLevel());
        if(entity==null) return;
        entity.moveTo(center.getX()+0.5D,center.getY(),center.getZ()+0.5D,
                random.nextFloat()*360.0F,0.0F);
        level.getLevel().addFreshEntity(entity);
    }
}
