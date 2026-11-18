package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.DeathEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Death uses a tall, skeletal silhouette until its bespoke model is authored. */
public final class DeathRenderer extends MobRenderer<DeathEntity, HumanoidModel<DeathEntity>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID, "death"), "main");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png");

    public DeathRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(LAYER)), 0.42F);
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64);
    }

    @Override public ResourceLocation getTextureLocation(DeathEntity entity) { return TEXTURE; }
}
