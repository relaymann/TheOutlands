package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.DeathEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
/** Death's charred hunter form, with a torn mantle and crown of bone spurs. */
public final class DeathModel extends HumanoidModel<DeathEntity> {
 private final ModelPart mantle,crown;
 public DeathModel(ModelPart root){super(root);mantle=root.getChild("body").getChild("mantle");crown=root.getChild("head").getChild("crown");}
 public static LayerDefinition createBodyLayer(){
  MeshDefinition mesh=HumanoidModel.createMesh(new CubeDeformation(.02F),0.0F);PartDefinition root=mesh.getRoot();
  root.getChild("body").addOrReplaceChild("mantle",CubeListBuilder.create().texOffs(0,32)
   .addBox(-6,-2,-1,12,15,2).addBox(-7,7,-1,3,6,2).addBox(4,7,-1,3,6,2),PartPose.offset(0,-1,2));
  root.getChild("head").addOrReplaceChild("crown",CubeListBuilder.create().texOffs(40,32)
   .addBox(-4,-2,-2,2,4,2).addBox(2,-2,-2,2,4,2).addBox(-1,-5,-1,2,5,2),PartPose.offset(0,-6,0));
  return LayerDefinition.create(mesh,64,64);
 }
 @Override public void setupAnim(DeathEntity entity,float limbSwing,float limbSwingAmount,float ageInTicks,float netHeadYaw,float headPitch){
  super.setupAnim(entity,limbSwing,limbSwingAmount,ageInTicks,netHeadYaw,headPitch);
  float movement=Math.min(1.0F,limbSwingAmount),phase=ageInTicks*.075F;
  boolean hunting=entity.getTarget()!=null&&entity.getTarget().isAlive();
  body.xRot=.10F+movement*(hunting?.24F:.10F);body.y+=(float)Math.sin(phase*2.0F)*.035F;
  head.xRot+=hunting?.10F:(float)Math.sin(phase)*.025F;head.yRot+=(float)Math.sin(phase*.63F)*.025F;
  float swing=(float)Math.cos(limbSwing*.62F)*movement;
  rightArm.xRot=-.16F+swing*(hunting?.72F:.42F);leftArm.xRot=-.08F-swing*(hunting?.54F:.42F);
  rightArm.zRot=.08F+(float)Math.sin(phase*.7F)*.025F;leftArm.zRot=-.08F-(float)Math.sin(phase*.7F)*.025F;
  rightLeg.xRot=(float)Math.cos(limbSwing*.62F+Math.PI)*movement*.85F;leftLeg.xRot=(float)Math.cos(limbSwing*.62F)*movement*.85F;
  mantle.xRot=.06F+(float)Math.sin(phase)*.035F+(hunting?.08F:0.0F);crown.zRot=(float)Math.sin(phase*.42F)*.018F;
 }
}