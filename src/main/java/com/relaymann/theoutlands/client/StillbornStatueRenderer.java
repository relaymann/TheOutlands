package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.StillbornStatueEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
public final class StillbornStatueRenderer extends MobRenderer<StillbornStatueEntity,StillbornStatueModel>{
 public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"stillborn_statue"),"main");
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,"textures/entity/stillborn_statue.png");
 public StillbornStatueRenderer(EntityRendererProvider.Context c){super(c,new StillbornStatueModel(c.bakeLayer(LAYER)),0.5F);}
 public static net.minecraft.client.model.geom.builders.LayerDefinition createBodyLayer(){return StillbornStatueModel.createBodyLayer();}
 @Override public ResourceLocation getTextureLocation(StillbornStatueEntity e){return TEXTURE;}
}
