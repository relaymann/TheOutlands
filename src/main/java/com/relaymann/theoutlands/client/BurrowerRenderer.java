package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.BurrowerEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
public final class BurrowerRenderer extends MobRenderer<BurrowerEntity,BurrowerModel>{
 public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"burrower"),"main");
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"textures/entity/burrower.png");
 public BurrowerRenderer(EntityRendererProvider.Context c){super(c,new BurrowerModel(c.bakeLayer(LAYER)),0.5F);}
 public static net.minecraft.client.model.geom.builders.LayerDefinition createBodyLayer(){return BurrowerModel.createBodyLayer();}
 @Override public ResourceLocation getTextureLocation(BurrowerEntity e){return TEXTURE;}
}