package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.HunterEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
/** Custom narrow predator silhouette with a ribbed torso, hooked forearms and angular skull. */
public final class HunterModel extends EntityModel<HunterEntity> {
 private final ModelPart root,head,torso,leftArm,rightArm,leftLeg,rightLeg,spine;
 public HunterModel(ModelPart root){this.root=root;head=root.getChild("head");torso=root.getChild("torso");leftArm=root.getChild("left_arm");rightArm=root.getChild("right_arm");leftLeg=root.getChild("left_leg");rightLeg=root.getChild("right_leg");spine=root.getChild("spine");}
 public static LayerDefinition createBodyLayer(){
  MeshDefinition m=new MeshDefinition();PartDefinition r=m.getRoot();
  r.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-3,-8,-3,6,7,6).texOffs(0,14).addBox(-2,-3,-5,4,2,3),PartPose.offset(0,-12,0));
  r.addOrReplaceChild("torso",CubeListBuilder.create().texOffs(24,0).addBox(-4,-13,-2,8,13,4).texOffs(24,18).addBox(-5,-10,-1,10,5,3),PartPose.ZERO);
  r.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(0,22).addBox(-1,-2,-2,3,13,3).texOffs(12,22).addBox(-1,9,-3,3,5,5),PartPose.offset(5,-10,0));
  r.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(0,22).addBox(-2,-2,-2,3,13,3).texOffs(12,22).addBox(-2,9,-3,3,5,5),PartPose.offset(-5,-10,0));
  r.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(40,0).addBox(-2,0,-2,3,12,4),PartPose.offset(2,0,0));
  r.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(40,0).addBox(-1,0,-2,3,12,4),PartPose.offset(-2,0,0));
  r.addOrReplaceChild("spine",CubeListBuilder.create().texOffs(48,18).addBox(-1,-14,-3,2,16,2),PartPose.offset(0,0,2));
  return LayerDefinition.create(m,64,64);
 }
 @Override public void setupAnim(HunterEntity e,float swing,float amount,float age,float yaw,float pitch){
  head.yRot=yaw*Mth.DEG_TO_RAD;head.xRot=pitch*Mth.DEG_TO_RAD+0.08F;
  float gait=Mth.cos(swing*0.72F)*amount;
  torso.xRot=0.18F+amount*0.12F+(float)Math.sin(age*.08F)*.025F;
  leftArm.xRot=-.45F+gait*.72F;rightArm.xRot=-.45F-gait*.72F;
  leftArm.zRot=.16F;rightArm.zRot=-.16F;
  leftLeg.xRot=-gait*.85F;rightLeg.xRot=gait*.85F;
  spine.xRot=(float)Math.sin(age*.12F)*.035F;
 }
 @Override public void renderToBuffer(PoseStack p,com.mojang.blaze3d.vertex.VertexConsumer v,int light,int overlay,int color){root.render(p,v,light,overlay,color);}
}