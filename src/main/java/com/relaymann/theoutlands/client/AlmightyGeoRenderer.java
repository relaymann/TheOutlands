package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.entity.AlmightyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class AlmightyGeoRenderer extends GeoEntityRenderer<AlmightyEntity> {
    public AlmightyGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new OutlandsGeoModel<>());
        this.scaleWidth = 1.55F;
        this.scaleHeight = 1.75F;
    }
}
