package com.relaymann.theoutlands.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.relaymann.theoutlands.entity.AlmightyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Preserves the Almighty's towering silhouette while using its GeckoLib rig. */
public final class AlmightyGeoRenderer extends GeoEntityRenderer<AlmightyEntity> {
    public AlmightyGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new OutlandsGeoModel<>());
    }

    @Override
    protected void scale(AlmightyEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.55F, 1.75F, 1.55F);
    }
}
