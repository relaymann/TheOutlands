package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.OutlandsEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class OutlandsClientEvents {
    private OutlandsClientEvents() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(OutlandsEntities.NULL.get(), NullRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.FEAR.get(), FearRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.DEATH.get(), DeathRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.ALMIGHTY.get(), AlmightyRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.NULL_APOSTLE.get(), NullApostleRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.CAVE_JAR.get(), CaveJarRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.STALKER.get(), StalkerRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.OBSERVER.get(), ObserverRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.HUNTER.get(), HunterRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.BURROWER.get(), BurrowerRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.MIMIC.get(), MimicRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.STILLBORN_STATUE.get(), StillbornStatueRenderer::new);
        event.registerEntityRenderer(OutlandsEntities.AGITATED_ONE.get(), AgitatedOneRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(NullRenderer.LAYER, NullRenderer::createBodyLayer);
        event.registerLayerDefinition(FearRenderer.LAYER, FearRenderer::createBodyLayer);
        event.registerLayerDefinition(DeathRenderer.LAYER, DeathRenderer::createBodyLayer);
        event.registerLayerDefinition(AlmightyRenderer.LAYER, AlmightyRenderer::createBodyLayer);
        event.registerLayerDefinition(NullApostleRenderer.LAYER, NullApostleRenderer::createBodyLayer);
        event.registerLayerDefinition(CaveJarRenderer.LAYER, CaveJarRenderer::createBodyLayer);
        event.registerLayerDefinition(StalkerRenderer.LAYER, StalkerRenderer::createBodyLayer);
        event.registerLayerDefinition(ObserverRenderer.LAYER, ObserverRenderer::createBodyLayer);
        event.registerLayerDefinition(HunterRenderer.LAYER, HunterRenderer::createBodyLayer);
        event.registerLayerDefinition(BurrowerRenderer.LAYER, BurrowerRenderer::createBodyLayer);
        event.registerLayerDefinition(MimicRenderer.LAYER, MimicRenderer::createBodyLayer);
        event.registerLayerDefinition(StillbornStatueRenderer.LAYER, StillbornStatueRenderer::createBodyLayer);
        event.registerLayerDefinition(AgitatedOneRenderer.LAYER, AgitatedOneRenderer::createBodyLayer);
    }
}
