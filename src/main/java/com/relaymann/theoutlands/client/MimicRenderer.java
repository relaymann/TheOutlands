package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.MimicEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
public final class MimicRenderer extends MobRenderer<MimicEntity,MimicModel>{
 public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"mimic"),"main");
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"textures/entity/mimic.png");
 public MimicRenderer(EntityRendererProvider.Context c){super(c,new MimicModel(c.bakeLayer(LAYER)),0.42F);}
 public static net.minecraft.client.model.geom.builders.LayerDefinition createBodyLayer(){return MimicModel.createBodyLayer();}
 @Override public ResourceLocation getTextureLocation(MimicEntity e){return TEXTURE;}
}