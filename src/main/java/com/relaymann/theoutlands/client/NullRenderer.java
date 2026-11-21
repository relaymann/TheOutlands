package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.NullEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
/** Null's custom skin on a normal player-shaped humanoid model. */
public final class NullRenderer extends MobRenderer<NullEntity,NullModel> {
 public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"null"),"main");
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"textures/entity/null.png");
 public NullRenderer(EntityRendererProvider.Context context){super(context,new NullModel(context.bakeLayer(LAYER)),0.35F);}
 public static LayerDefinition createBodyLayer(){return NullModel.createBodyLayer();}
 @Override public ResourceLocation getTextureLocation(NullEntity entity){return TEXTURE;}
}
