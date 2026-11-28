package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.NightmareCreature;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class NightmareCreatureRenderer extends MobRenderer<NightmareCreature, NightmareCreatureModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID, "nightmare_creature"), "main");
    private final ResourceLocation texture;

    public NightmareCreatureRenderer(EntityRendererProvider.Context context, String variant) {
        super(context, new NightmareCreatureModel(context.bakeLayer(LAYER)), 0.42F);
        texture = ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,
                "textures/entity/nightmare/" + variant + ".png");
    }

    public static LayerDefinition createBodyLayer() {
        return NightmareCreatureModel.createBodyLayer();
    }

    @Override
    public ResourceLocation getTextureLocation(NightmareCreature entity) {
        return texture;
    }
}
