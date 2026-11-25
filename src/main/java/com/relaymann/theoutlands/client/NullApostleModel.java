package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.NullApostleEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class NullApostleModel extends HumanoidModel<NullApostleEntity> {
 private final ModelPart shroud, hood;
 public NullApostleModel(ModelPart root) { super(root); shroud=root.getChild("body").getChild("shroud"); hood=root.getChild("head").getChild("hood"); }
 public static LayerDefinition createBodyLayer() {
  MeshDefinition mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0.0F); PartDefinition root=mesh.getRoot();
  root.getChild("body").addOrReplaceChild("shroud",CubeListBuilder.create().texOffs(0,32).addBox(-5,-1,-1,10,15,1),PartPose.offset(0,-1,2));
  root.getChild("head").addOrReplaceChild("hood",CubeListBuilder.create().texOffs(40,32).addBox(-4,-2,-4,8,3,8),PartPose.offset(0,-6,0));
  return LayerDefinition.create(mesh,64,64);
 }
 @Override public void setupAnim(NullApostleEntity entity,float limbSwing,float limbSwingAmount,float ageInTicks,float netHeadYaw,float headPitch) {
  super.setupAnim(entity,limbSwing,limbSwingAmount,ageInTicks,netHeadYaw,headPitch);
  float phase=ageInTicks*0.11F+entity.getId()*0.73F, twitch=(float)Math.sin(phase)*0.025F;
  head.yRot+=twitch; head.zRot=(float)Math.sin(phase*0.47F)*0.025F;
  body.zRot=(float)Math.sin(phase*0.31F)*0.012F;
  rightArm.zRot=0.025F+twitch; leftArm.zRot=-0.025F-twitch;
  shroud.xRot=.05F+(float)Math.sin(phase*.8F)*.04F; hood.zRot=twitch*.7F;
 }
}
