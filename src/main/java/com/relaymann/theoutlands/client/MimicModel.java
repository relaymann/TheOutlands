package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.MimicEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;

public final class MimicModel extends EntityModel<MimicEntity> {
 private final ModelPart root,head,body,leftArm,rightArm,leftLeg,rightLeg,jaw;
 public MimicModel(ModelPart root){super(RenderType::entityCutoutNoCull);this.root=root;head=root.getChild("head");body=root.getChild("body");leftArm=root.getChild("left_arm");rightArm=root.getChild("right_arm");leftLeg=root.getChild("left_leg");rightLeg=root.getChild("right_leg");jaw=root.getChild("jaw");}
 public static LayerDefinition createBodyLayer(){
  MeshDefinition m=new MeshDefinition();PartDefinition r=m.getRoot();
  r.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-3,-8,-3,6,8,6).texOffs(0,14).addBox(-1,-2,-5,2,3,2),PartPose.offset(0,-13,0));
  r.addOrReplaceChild("body",CubeListBuilder.create().texOffs(20,0).addBox(-4,-13,-2,8,13,4).texOffs(20,18).addBox(-5,-10,-3,10,8,1),PartPose.ZERO);
  r.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(0,24).addBox(-1,-2,-2,3,15,3),PartPose.offset(5,-10,0));
  r.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(0,24).addBox(-2,-2,-2,3,15,3),PartPose.offset(-5,-10,0));
  r.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(42,0).addBox(-1,0,-2,3,13,4),PartPose.offset(2,0,0));
  r.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(42,0).addBox(-2,0,-2,3,13,4),PartPose.offset(-2,0,0));
  r.addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(48,24).addBox(-3,-1,-4,6,2,4),PartPose.offset(0,-7,-1));
  return LayerDefinition.create(m,64,64);
 }
 @Override public void setupAnim(MimicEntity e,float swing,float amount,float age,float yaw,float pitch){
  head.yRot=yaw*Mth.DEG_TO_RAD;head.xRot=pitch*Mth.DEG_TO_RAD;
  float gait=Mth.cos(swing*.66F)*amount;
  float reveal=e.isRevealed()?1F:0F;
  body.xRot=reveal*.20F+(float)Math.sin(age*.025F)*.012F;
  leftArm.xRot=-.06F+reveal*(-1.1F)+gait*.55F;rightArm.xRot=-.06F+reveal*(-1.1F)-gait*.55F;
  leftArm.zRot=.04F+reveal*.35F;rightArm.zRot=-.04F-reveal*.35F;
  leftLeg.xRot=-gait*.72F;rightLeg.xRot=gait*.72F;
  jaw.xRot=reveal*.5F+(float)Math.sin(age*.08F)*.015F;
 }
 @Override public void renderToBuffer(PoseStack p,com.mojang.blaze3d.vertex.VertexConsumer v,int light,int overlay,int color){root.render(p,v,light,overlay,color);}
}
