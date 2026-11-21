package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.StalkerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
/** A narrow, forward-leaning hunter with dorsal spines and hooked foreclaws. */
public final class StalkerModel extends HumanoidModel<StalkerEntity> {
 private final ModelPart spines,leftClaw,rightClaw;
 public StalkerModel(ModelPart root){super(root);spines=root.getChild("body").getChild("spines");leftClaw=root.getChild("left_arm").getChild("claw");rightClaw=root.getChild("right_arm").getChild("claw");}
 public static LayerDefinition createBodyLayer(){
  MeshDefinition mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0.0F);PartDefinition root=mesh.getRoot();
  root.getChild("body").addOrReplaceChild("spines",CubeListBuilder.create().texOffs(40,32)
   .addBox(-1,-12,1,2,4,3).addBox(-1,-7,1,2,4,3).addBox(-1,-2,1,2,4,3),PartPose.ZERO);
  CubeListBuilder claw=CubeListBuilder.create().texOffs(48,32).addBox(-1,8,-3,2,5,3);
  root.getChild("left_arm").addOrReplaceChild("claw",claw,PartPose.ZERO);
  root.getChild("right_arm").addOrReplaceChild("claw",CubeListBuilder.create().texOffs(48,32).addBox(-1,8,-3,2,5,3),PartPose.ZERO);
  return LayerDefinition.create(mesh,64,64);
 }
 @Override public void setupAnim(StalkerEntity e,float swing,float amount,float age,float yaw,float pitch){
  super.setupAnim(e,swing,amount,age,yaw,pitch);
  body.xScale=.82F;body.yScale=1.12F;head.xScale=.88F;rightLeg.yScale=1.08F;leftLeg.yScale=1.08F;
  float tension=e.getTarget()!=null?1.0F:0.0F;
  body.xRot=.22F+amount*.10F+(e.isSprinting()?-.14F:0.0F)+(float)Math.sin(age*.09F)*.018F;
  head.xRot-=.08F+tension*.07F;float reach=(float)Math.sin(age*.19F)*.08F;
  rightArm.xRot-=.24F+reach+amount*.16F;leftArm.xRot-=.24F-reach-amount*.16F;
  rightLeg.xRot+=Mth.cos(swing*.7F)*amount*.22F;leftLeg.xRot-=Mth.cos(swing*.7F)*amount*.22F;
  rightArm.zRot=-.10F;leftArm.zRot=.10F;rightArm.yScale=1.16F;leftArm.yScale=1.16F;
  spines.xRot=.08F+(float)Math.sin(age*.15F)*.025F+tension*.06F;
  leftClaw.xRot=-.18F+reach*.5F;rightClaw.xRot=-.18F-reach*.5F;
 }
}