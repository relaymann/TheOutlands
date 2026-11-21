package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.NullApostleEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
/** Null's apostles are entirely black, featureless player-shaped silhouettes. */
public final class NullApostleRenderer extends MobRenderer<NullApostleEntity,NullApostleModel> {
 public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"null_apostle"),"main");
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"textures/entity/null_apostle.png");
 public NullApostleRenderer(EntityRendererProvider.Context context){super(context,new NullApostleModel(context.bakeLayer(LAYER)),0.35F);}
 return NullApostleModel.createBodyLayer();
 @Override public ResourceLocation getTextureLocation(NullApostleEntity entity){return TEXTURE;}
}
