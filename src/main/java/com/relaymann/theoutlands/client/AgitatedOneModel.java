package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.AgitatedOneEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;

public final class AgitatedOneModel extends EntityModel<AgitatedOneEntity> {
 private final ModelPart root,head,body,leftArm,rightArm,leftLeg,rightLeg,jaw,shoulders;
 public AgitatedOneModel(ModelPart root){super(RenderType::entityCutoutNoCull);this.root=root;head=root.getChild("head");body=root.getChild("body");leftArm=root.getChild("left_arm");rightArm=root.getChild("right_arm");leftLeg=root.getChild("left_leg");rightLeg=root.getChild("right_leg");jaw=root.getChild("jaw");shoulders=root.getChild("shoulders");}
 public static LayerDefinition createBodyLayer(){
  MeshDefinition m=new MeshDefinition();PartDefinition r=m.getRoot();
  r.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-3,-8,-3,6,8,6).texOffs(0,15).addBox(-2,-3,-5,4,2,3),PartPose.offset(0,-13,0));
  r.addOrReplaceChild("body",CubeListBuilder.create().texOffs(20,0).addBox(-4,-13,-2,8,13,4).texOffs(20,18).addBox(-5,-10,-2,10,5,4),PartPose.ZERO);
  r.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(0,23).addBox(-2,-2,-2,4,13,4).texOffs(16,23).addBox(-2,8,-4,4,6,6),PartPose.offset(6,-10,0));
  r.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(0,23).addBox(-2,-2,-2,4,13,4).texOffs(16,23).addBox(-2,8,-4,4,6,6),PartPose.offset(-6,-10,0));
  r.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(40,0).addBox(-2,0,-2,4,13,4),PartPose.offset(2,0,0));
  r.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(40,0).addBox(-2,0,-2,4,13,4),PartPose.offset(-2,0,0));
  r.addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(46,22).addBox(-3,-1,-4,6,3,4),PartPose.offset(0,-7,-1));
  r.addOrReplaceChild("shoulders",CubeListBuilder.create().texOffs(44,0).addBox(-7,-2,-2,14,4,4),PartPose.offset(0,-11,0));
  return LayerDefinition.create(m,64,64);
 }
 @Override public void setupAnim(AgitatedOneEntity e,float swing,float amount,float age,float yaw,float pitch){
  float gait=Mth.cos(swing*.72F)*amount;float anger=e.getAgitation()/100F;float twitch=(float)Math.sin(age*(.32F+anger*.18F))*(.025F+anger*.07F);
  head.yRot=yaw*Mth.DEG_TO_RAD+twitch;head.xRot=pitch*Mth.DEG_TO_RAD+anger*.14F;
  body.xRot=.12F+anger*.24F+(e.isCharging()?-.3F:0F);
  leftArm.xRot=-.25F+gait*.8F+anger*.35F;rightArm.xRot=-.25F-gait*.8F-anger*.25F;
  leftArm.zRot=.12F+anger*.25F;rightArm.zRot=-.12F-anger*.3F;
  leftLeg.xRot=-gait*.9F;rightLeg.xRot=gait*.9F;
  jaw.xRot=anger*.42F+(float)Math.sin(age*.18F)*.025F;
  shoulders.zRot=twitch*.6F;
 }
 @Override public void renderToBuffer(PoseStack p,com.mojang.blaze3d.vertex.VertexConsumer v,int light,int overlay,int color){root.render(p,v,light,overlay,color);}
}
