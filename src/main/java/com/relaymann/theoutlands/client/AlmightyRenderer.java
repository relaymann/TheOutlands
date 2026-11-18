package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.AlmightyEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** A towering placeholder silhouette; the encounter logic is independent of this presentation. */
public final class AlmightyRenderer extends MobRenderer<AlmightyEntity, HumanoidModel<AlmightyEntity>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID, "almighty"), "main");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/warden/warden.png");

    public AlmightyRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(LAYER)), 0.72F);
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.12F), 0.0F), 64, 64);
    }

    @Override public ResourceLocation getTextureLocation(AlmightyEntity entity) { return TEXTURE; }
}
