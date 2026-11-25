package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.OutlandsEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class OutlandsClientEvents {
    private OutlandsClientEvents() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(OutlandsEntities.NULL.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.FEAR.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.DEATH.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.ALMIGHTY.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.NULL_APOSTLE.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.CAVE_JAR.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.STALKER.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.OBSERVER.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.HUNTER.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.BURROWER.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.MIMIC.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.STILLBORN_STATUE.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.AGITATED_ONE.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.LURKER.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.HOWLER.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.DREDGE.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.PILGRIM.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.CEILING_DWELLER.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.COLOSSUS.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.NIGHTMARE_WATCHER.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.NIGHTMARE_HOUND.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.NIGHTMARE_WARDENED.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.NIGHTMARE_FALSE_SURVIVOR.get(), OutlandsGeoRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.NIGHTMARE_UNBOUND.get(), OutlandsGeoRenderer::new);
    }
}
