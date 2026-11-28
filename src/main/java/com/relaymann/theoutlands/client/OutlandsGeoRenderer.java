package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.entity.OutlandsGeoMonster;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class OutlandsGeoRenderer<T extends OutlandsGeoMonster> extends GeoEntityRenderer<T> {
    public OutlandsGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new OutlandsGeoModel<>());
    }
}
