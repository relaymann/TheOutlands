package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.TheOutlands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class OutlandsEntityEvents {
    private OutlandsEntityEvents() {}

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(OutlandsEntities.NULL.get(), NullEntity.createAttributes().build());\n        event.put(OutlandsEntities.FEAR.get(), FearEntity.createAttributes().build());
    }
}
