package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.ObserverEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
public final class ObserverRenderer extends MobRenderer<ObserverEntity,ObserverModel> {
 public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"observer"),"main");
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"textures/entity/observer.png");
 public ObserverRenderer(EntityRendererProvider.Context c){super(c,new ObserverModel(c.bakeLayer(LAYER)),0.4F);}
 public static LayerDefinition createBodyLayer(){return ObserverModel.createBodyLayer();}
 @Override public ResourceLocation getTextureLocation(ObserverEntity e){return TEXTURE;}
}
