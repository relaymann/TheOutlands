package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.ObserverEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class ObserverModel extends HumanoidModel<ObserverEntity> {
 private final ModelPart eye,veil;
 public ObserverModel(ModelPart root){super(root);eye=root.getChild("head").getChild("eye");veil=root.getChild("body").getChild("veil");}
 public static LayerDefinition createBodyLayer(){
  MeshDefinition mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0.0F);PartDefinition root=mesh.getRoot();
  root.getChild("head").addOrReplaceChild("eye",CubeListBuilder.create().texOffs(44,32).addBox(-2,-1,-4,4,2,1),PartPose.offset(0,-3,0));
  root.getChild("body").addOrReplaceChild("veil",CubeListBuilder.create().texOffs(0,32).addBox(-4,0,-1,8,14,1),PartPose.offset(0,-1,2));
  return LayerDefinition.create(mesh,64,64);
 }
 @Override public void setupAnim(ObserverEntity e,float swing,float amount,float age,float yaw,float pitch){
  super.setupAnim(e,swing,amount,age,yaw,pitch);
  body.xScale=.88F;body.yScale=1.08F;head.xScale=.92F;
  float still=(float)Math.sin(age*.035F)*.012F;
  body.xRot=.015F;body.yRot=still;rightArm.xRot=-.035F;leftArm.xRot=-.035F;
  rightArm.zRot=-.035F;leftArm.zRot=.035F;
  float gait=e.isAdvancing()?Mth.cos(swing*.62F)*amount:0.0F;
  rightLeg.xRot=gait*.55F;leftLeg.xRot=-gait*.55F;
  rightArm.xRot=e.isAdvancing()?-.08F:-.035F;leftArm.xRot=e.isAdvancing()?-.08F:-.035F;
  head.zRot=(float)Math.sin(age*.021F)*.018F;
  eye.yRot=(float)Math.sin(age*.013F)*.025F;veil.xRot=.03F+(float)Math.sin(age*.027F)*.018F;
 }
}
