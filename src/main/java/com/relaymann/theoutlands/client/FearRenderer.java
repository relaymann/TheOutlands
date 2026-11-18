package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.FearEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
/** Fear's dedicated humanoid renderer and unique texture. */
public final class FearRenderer extends MobRenderer<FearEntity,HumanoidModel<FearEntity>> {
 public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"fear"),"main");
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"textures/entity/fear.png");
 public FearRenderer(EntityRendererProvider.Context context){super(context,new HumanoidModel<>(context.bakeLayer(LAYER)),0.4F);}
 public static LayerDefinition createBodyLayer(){return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE,0.0F),64,64);}
 @Override public ResourceLocation getTextureLocation(FearEntity entity){return TEXTURE;}
}
