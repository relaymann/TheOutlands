package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryObject;

public final class OutlandsEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, TheOutlands.MOD_ID);
    public static final RegistryObject<EntityType<NullEntity>> NULL =
            ENTITY_TYPES.register("null", () -> EntityType.Builder.of(NullEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(12)
                    .updateInterval(2)
                    .build("theoutlands:null"));

    private OutlandsEntities() {}
}
