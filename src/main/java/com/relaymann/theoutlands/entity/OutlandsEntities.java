package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.FearEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class OutlandsEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, TheOutlands.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<NullEntity>> NULL =
            ENTITY_TYPES.register("null", () -> EntityType.Builder.of(NullEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(12)
                    .updateInterval(2)
                    .build("theoutlands:null"));

    public static final DeferredHolder<EntityType<?>, EntityType<FearEntity>> FEAR =
            ENTITY_TYPES.register("fear", () -> EntityType.Builder.of(FearEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(16).updateInterval(2)
                    .build("theoutlands:fear"));

    private OutlandsEntities() {}
}
