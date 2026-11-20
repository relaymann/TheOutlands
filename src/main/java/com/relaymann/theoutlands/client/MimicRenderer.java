package com.relaymann.theoutlands.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.MimicEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public final class MimicRenderer extends MobRenderer<MimicEntity, MimicModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID, "mimic"), "main");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            TheOutlands.MOD_ID, "textures/entity/mimic.png");
    private final BlockRenderDispatcher blockRenderer;

    public MimicRenderer(EntityRendererProvider.Context context) {
        super(context, new MimicModel(context.bakeLayer(LAYER)), 0.42F);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    public static net.minecraft.client.model.geom.builders.LayerDefinition createBodyLayer() {
        return MimicModel.createBodyLayer();
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

    @Override
    public ResourceLocation getTextureLocation(MimicEntity entity) {
        return TEXTURE;
    }
}
