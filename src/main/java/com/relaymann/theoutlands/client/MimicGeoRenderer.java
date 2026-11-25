package com.relaymann.theoutlands.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.relaymann.theoutlands.entity.MimicEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Keeps the Mimic's block disguise intact while using GeckoLib after its reveal. */
public final class MimicGeoRenderer extends GeoEntityRenderer<MimicEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public MimicGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new OutlandsGeoModel<>());
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(MimicEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        if (entity.isRevealed()) {
            super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
            return;
        }
        poseStack.pushPose();
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        blockRenderer.renderSingleBlock(entity.getDisguiseBlockState(), poseStack, buffer,
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
