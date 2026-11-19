package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.BurrowerEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;
/** Low-slung digging beast with a plated back, wedge skull and oversized digging claws. */
public final class BurrowerModel extends EntityModel<BurrowerEntity> {
 private final ModelPart root,head,body,leftClaw,rightClaw,leftRear,rightRear,plates;
 public BurrowerModel(ModelPart root){super(RenderType::entityCutoutNoCull);this.root=root;head=root.getChild("head");body=root.getChild("body");leftClaw=root.getChild("left_claw");rightClaw=root.getChild("right_claw");leftRear=root.getChild("left_rear");rightRear=root.getChild("right_rear");plates=root.getChild("plates");}
 public static LayerDefinition createBodyLayer(){
  MeshDefinition m=new MeshDefinition();PartDefinition r=m.getRoot();
  r.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,0).addBox(-6,-5,-9,12,9,18).texOffs(0,28).addBox(-5,-7,-6,10,3,12),PartPose.offset(0,4,0));
  r.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,44).addBox(-4,-4,-8,8,7,8).texOffs(32,44).addBox(-5,0,-9,10,2,5),PartPose.offset(0,3,-7));
  r.addOrReplaceChild("left_claw",CubeListBuilder.create().texOffs(38,0).addBox(-1,-1,-2,4,5,8).texOffs(42,14).addBox(1,2,-7,2,2,7),PartPose.offset(5,5,-5));
  r.addOrReplaceChild("right_claw",CubeListBuilder.create().texOffs(38,0).addBox(-3,-1,-2,4,5,8).texOffs(42,14).addBox(-3,2,-7,2,2,7),PartPose.offset(-5,5,-5));
  r.addOrReplaceChild("left_rear",CubeListBuilder.create().texOffs(24,32).addBox(-1,0,-2,4,4,7),PartPose.offset(4,5,5));
  r.addOrReplaceChild("right_rear",CubeListBuilder.create().texOffs(24,32).addBox(-3,0,-2,4,4,7),PartPose.offset(-4,5,5));
  r.addOrReplaceChild("plates",CubeListBuilder.create().texOffs(0,56).addBox(-5,-9,-4,10,3,3).addBox(-4,-8,0,8,3,3).addBox(-3,-7,4,6,3,3),PartPose.offset(0,4,0));
  return LayerDefinition.create(m,64,64);
 }
 @Override public void setupAnim(BurrowerEntity e,float swing,float amount,float age,float yaw,float pitch){
  head.yRot=yaw*Mth.DEG_TO_RAD;head.xRot=pitch*Mth.DEG_TO_RAD;
  float gait=Mth.cos(swing*.8F)*amount;
  body.xRot=.12F+(float)Math.sin(age*.06F)*.025F;
  leftClaw.xRot=-.25F+gait*.45F;rightClaw.xRot=-.25F-gait*.45F;
  leftRear.xRot=-gait*.4F;rightRear.xRot=gait*.4F;
  plates.xRot=(float)Math.sin(age*.09F)*.018F;
 }
 @Override public void renderToBuffer(PoseStack p,com.mojang.blaze3d.vertex.VertexConsumer v,int light,int overlay,int color){root.render(p,v,light,overlay,color);}
}