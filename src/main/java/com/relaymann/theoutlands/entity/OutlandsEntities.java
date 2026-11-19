package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class OutlandsEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, TheOutlands.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<NullEntity>> NULL =
            ENTITY_TYPES.register("null", () -> EntityType.Builder.of(NullEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F).clientTrackingRange(12).updateInterval(2).build("theoutlands:null"));

    public static final DeferredHolder<EntityType<?>, EntityType<FearEntity>> FEAR =
            ENTITY_TYPES.register("fear", () -> EntityType.Builder.of(FearEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(16).updateInterval(2).build("theoutlands:fear"));

    public static final DeferredHolder<EntityType<?>, EntityType<DeathEntity>> DEATH =
            ENTITY_TYPES.register("death", () -> EntityType.Builder.of(DeathEntity::new, MobCategory.MONSTER)
                    .sized(0.72F, 2.15F).clientTrackingRange(24).updateInterval(2).build("theoutlands:death"));

    public static final DeferredHolder<EntityType<?>, EntityType<AlmightyEntity>> ALMIGHTY =
            ENTITY_TYPES.register("almighty", () -> EntityType.Builder.of(AlmightyEntity::new, MobCategory.MONSTER)
                    .sized(1.1F, 3.2F).clientTrackingRange(32).updateInterval(2).build("theoutlands:almighty"));

    public static final DeferredHolder<EntityType<?>, EntityType<NullApostleEntity>> NULL_APOSTLE =
            ENTITY_TYPES.register("null_apostle", () -> EntityType.Builder.of(NullApostleEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F).clientTrackingRange(16).updateInterval(2).build("theoutlands:null_apostle"));

    public static final DeferredHolder<EntityType<?>, EntityType<CaveJarEntity>> CAVE_JAR =
            ENTITY_TYPES.register("cave_jar", () -> EntityType.Builder.of(CaveJarEntity::new, MobCategory.MONSTER)
                    .sized(0.95F, 2.65F).clientTrackingRange(16).updateInterval(2).build("theoutlands:cave_jar"));

    public static final DeferredHolder<EntityType<?>, EntityType<StalkerEntity>> STALKER =
            ENTITY_TYPES.register("stalker", () -> EntityType.Builder.of(StalkerEntity::new, MobCategory.MONSTER)
                    .sized(0.72F, 2.35F).clientTrackingRange(24).updateInterval(2).build("theoutlands:stalker"));

    public static final DeferredHolder<EntityType<?>, EntityType<ObserverEntity>> OBSERVER =
            ENTITY_TYPES.register("observer", () -> EntityType.Builder.of(ObserverEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.25F).clientTrackingRange(20).updateInterval(2).build("theoutlands:observer"));

    private OutlandsEntities() {}
}
