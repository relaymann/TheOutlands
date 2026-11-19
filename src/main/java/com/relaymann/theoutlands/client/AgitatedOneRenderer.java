package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.AgitatedOneEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
public final class AgitatedOneRenderer extends MobRenderer<AgitatedOneEntity,AgitatedOneModel>{
 public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"agitated_one"),"main");
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"textures/entity/agitated_one.png");
 public AgitatedOneRenderer(EntityRendererProvider.Context c){super(c,new AgitatedOneModel(c.bakeLayer(LAYER)),0.42F);}
 public static net.minecraft.client.model.geom.builders.LayerDefinition createBodyLayer(){return AgitatedOneModel.createBodyLayer();}
 @Override public ResourceLocation getTextureLocation(AgitatedOneEntity e){return TEXTURE;}
}
