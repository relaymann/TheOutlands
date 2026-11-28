package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.HunterEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
public final class HunterRenderer extends MobRenderer<HunterEntity,HunterModel>{
 public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"hunter"),"main");
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"textures/entity/hunter.png");
 public HunterRenderer(EntityRendererProvider.Context c){super(c,new HunterModel(c.bakeLayer(LAYER)),0.48F);}
 public static net.minecraft.client.model.geom.builders.LayerDefinition createBodyLayer(){return HunterModel.createBodyLayer();}
 @Override public ResourceLocation getTextureLocation(HunterEntity e){return TEXTURE;}
}
