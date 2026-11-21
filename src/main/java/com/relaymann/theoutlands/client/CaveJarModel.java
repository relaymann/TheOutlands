package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.CaveJarEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
/** Cave.jar's hunched frame is reinforced by exposed ribs and a hinged predator jaw. */
public final class CaveJarModel extends HumanoidModel<CaveJarEntity> {
 private final ModelPart ribs, jaw;
 public CaveJarModel(ModelPart root){super(root);ribs=root.getChild("body").getChild("ribs");jaw=root.getChild("head").getChild("jaw");}
 public static LayerDefinition createBodyLayer(){
  MeshDefinition mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0.0F);PartDefinition root=mesh.getRoot();
  root.getChild("body").addOrReplaceChild("ribs",CubeListBuilder.create().texOffs(32,32)
   .addBox(-5,-9,-3,10,2,6).addBox(-5,-5,-3,10,2,6).addBox(-4,-1,-3,8,2,6),PartPose.ZERO);
  root.getChild("head").addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(44,32).addBox(-3,-1,-4,6,2,4),PartPose.offset(0,5,-1));
  return LayerDefinition.create(mesh,64,64);
 }
 @Override public void setupAnim(CaveJarEntity e,float swing,float amount,float age,float yaw,float pitch){
  super.setupAnim(e,swing,amount,age,yaw,pitch);
  body.yScale=1.12F;body.xScale=.94F;head.xScale=.86F;head.yScale=.92F;
  float windup=e.getLungeWindup()/8.0F;
  head.xRot+=.12F+windup*.18F;body.xRot=.16F+windup*.24F+(float)Math.sin(age*.12F)*.025F;
  rightArm.xRot-=.42F+amount*.12F+windup*.38F;leftArm.xRot-=.42F-amount*.12F+windup*.38F;
  rightLeg.xRot+=Mth.cos(swing*.7F)*amount*.32F;leftLeg.xRot-=Mth.cos(swing*.7F)*amount*.32F;
  rightArm.zRot=-.16F;leftArm.zRot=.16F;rightLeg.xRot*=.85F;leftLeg.xRot*=.85F;
  rightArm.yScale=1.22F;leftArm.yScale=1.22F;
  ribs.xRot=.08F+(float)Math.sin(age*.13F)*.025F; jaw.xRot=-.08F-windup*.55F;
 }
}