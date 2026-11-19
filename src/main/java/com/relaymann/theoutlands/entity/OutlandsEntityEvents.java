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
        event.put(OutlandsEntities.NULL.get(), NullEntity.createAttributes().build());
        event.put(OutlandsEntities.FEAR.get(), FearEntity.createAttributes().build());
        event.put(OutlandsEntities.DEATH.get(), DeathEntity.createAttributes().build());
        event.put(OutlandsEntities.ALMIGHTY.get(), AlmightyEntity.createAttributes().build());
        event.put(OutlandsEntities.NULL_APOSTLE.get(), NullApostleEntity.createAttributes().build());
        event.put(OutlandsEntities.CAVE_JAR.get(), CaveJarEntity.createAttributes().build());
        event.put(OutlandsEntities.STALKER.get(), StalkerEntity.createAttributes().build());
        event.put(OutlandsEntities.OBSERVER.get(), ObserverEntity.createAttributes().build());
        event.put(OutlandsEntities.HUNTER.get(), HunterEntity.createAttributes().build());
        event.put(OutlandsEntities.BURROWER.get(), BurrowerEntity.createAttributes().build());
        event.put(OutlandsEntities.MIMIC.get(), MimicEntity.createAttributes().build());
        event.put(OutlandsEntities.STILLBORN_STATUE.get(), StillbornStatueEntity.createAttributes().build());
        event.put(OutlandsEntities.AGITATED_ONE.get(), AgitatedOneEntity.createAttributes().build());
    }
}
