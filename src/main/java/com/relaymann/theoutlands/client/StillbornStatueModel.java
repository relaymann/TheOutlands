package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.StillbornStatueEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;

public final class StillbornStatueModel extends EntityModel<StillbornStatueEntity> {
 private final ModelPart root,head,body,leftArm,rightArm,leftLeg,rightLeg,crackCrown;
 public StillbornStatueModel(ModelPart root){super(RenderType::entityCutoutNoCull);this.root=root;head=root.getChild("head");body=root.getChild("body");leftArm=root.getChild("left_arm");rightArm=root.getChild("right_arm");leftLeg=root.getChild("left_leg");rightLeg=root.getChild("right_leg");crackCrown=root.getChild("crown");}
 public static LayerDefinition createBodyLayer(){
  MeshDefinition m=new MeshDefinition();PartDefinition r=m.getRoot();
  r.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4,-8,-3,8,8,6).texOffs(0,15).addBox(-2,-4,-4,1,1,1).addBox(1,-4,-4,1,1,1),PartPose.offset(0,-15,0));
  r.addOrReplaceChild("body",CubeListBuilder.create().texOffs(24,0).addBox(-5,-14,-3,10,14,6).texOffs(24,22).addBox(-7,-12,-2,14,5,4),PartPose.ZERO);
  r.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(0,26).addBox(-2,-2,-2,4,13,4).texOffs(16,26).addBox(-2,9,-3,4,5,5),PartPose.offset(7,-11,0));
  r.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(0,26).addBox(-2,-2,-2,4,13,4).texOffs(16,26).addBox(-2,9,-3,4,5,5),PartPose.offset(-7,-11,0));
  r.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(42,0).addBox(-2,0,-2,4,13,4),PartPose.offset(3,0,0));
  r.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(42,0).addBox(-2,0,-2,4,13,4),PartPose.offset(-3,0,0));
  r.addOrReplaceChild("crown",CubeListBuilder.create().texOffs(44,22).addBox(-1,-5,-1,2,6,2),PartPose.offset(0,-20,0));
  return LayerDefinition.create(m,64,64);
 }
 @Override public void setupAnim(StillbornStatueEntity e,float swing,float amount,float age,float yaw,float pitch){
  float gait=Mth.cos(swing*.62F)*amount;float moving=e.isStalking()?1F:0F;
  head.yRot=yaw*Mth.DEG_TO_RAD;head.xRot=pitch*Mth.DEG_TO_RAD+moving*.12F;
  body.xRot=moving*.16F+(float)Math.sin(age*.035F)*.012F;
  leftArm.xRot=moving*(-.25F+gait*.5F);rightArm.xRot=moving*(-.25F-gait*.5F);
  leftArm.zRot=.08F+moving*.1F;rightArm.zRot=-.08F-moving*.1F;
  leftLeg.xRot=-gait*.72F* moving;rightLeg.xRot=gait*.72F*moving;
  crackCrown.zRot=(float)Math.sin(age*.025F)*.012F;
 }
 @Override public void renderToBuffer(PoseStack p,com.mojang.blaze3d.vertex.VertexConsumer v,int light,int overlay,int color){root.render(p,v,light,overlay,color);}
}
