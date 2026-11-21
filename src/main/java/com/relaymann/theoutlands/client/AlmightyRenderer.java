package com.relaymann.theoutlands.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.AlmightyEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** The Almighty's void-and-violet texture on a towering, phase-reactive model. */
public final class AlmightyRenderer extends MobRenderer<AlmightyEntity, AlmightyModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID, "almighty"), "main");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            TheOutlands.MOD_ID, "textures/entity/almighty.png");

    public AlmightyRenderer(EntityRendererProvider.Context context) {
        super(context, new AlmightyModel(context.bakeLayer(LAYER)), 0.72F);
    }

    public static LayerDefinition createBodyLayer() {
        return AlmightyModel.createBodyLayer();
    }

    @Override
    protected void scale(AlmightyEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.55F, 1.75F, 1.55F);
    }

    @Override
    public ResourceLocation getTextureLocation(AlmightyEntity entity) {
        return TEXTURE;
    }
}
