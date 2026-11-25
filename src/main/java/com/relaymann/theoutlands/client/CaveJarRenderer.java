package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.CaveJarEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
public final class CaveJarRenderer extends MobRenderer<CaveJarEntity,CaveJarModel> {
 public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"cave_jar"),"main");
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"textures/entity/cave_jar.png");
 public CaveJarRenderer(EntityRendererProvider.Context c){super(c,new CaveJarModel(c.bakeLayer(LAYER)),0.55F);}
 public static LayerDefinition createBodyLayer(){return CaveJarModel.createBodyLayer();}
 @Override public ResourceLocation getTextureLocation(CaveJarEntity e){return TEXTURE;}
}
