package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.NullEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
/** Null's familiar silhouette gains a trailing shadow mantle and a broken crown. */
public final class NullModel extends HumanoidModel<NullEntity> {
 private final ModelPart mantle, crown;
 public NullModel(ModelPart root) { super(root); mantle=root.getChild("body").getChild("mantle"); crown=root.getChild("head").getChild("crown"); }
 public static LayerDefinition createBodyLayer() {
  MeshDefinition mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0.0F); PartDefinition root=mesh.getRoot();
  root.getChild("body").addOrReplaceChild("mantle",CubeListBuilder.create().texOffs(0,32).addBox(-5,-1,-1,10,15,1),PartPose.offset(0,-1,2));
  root.getChild("head").addOrReplaceChild("crown",CubeListBuilder.create().texOffs(44,32).addBox(-1,-5,-1,2,5,2),PartPose.offset(0,-7,0));
  return LayerDefinition.create(mesh,64,64);
 }
 @Override public void setupAnim(NullEntity entity,float limbSwing,float limbSwingAmount,float ageInTicks,float netHeadYaw,float headPitch) {
  super.setupAnim(entity,limbSwing,limbSwingAmount,ageInTicks,netHeadYaw,headPitch);
  float phase=ageInTicks*0.045F, breath=(float)Math.sin(phase);
  body.y+=breath*0.035F; head.yRot+=(float)Math.sin(phase*0.63F)*0.035F;
  head.zRot=(float)Math.sin(phase*0.41F)*0.018F; rightArm.xRot+=breath*0.012F; leftArm.xRot-=breath*0.012F;
  mantle.xRot=0.08F+(float)Math.sin(phase*0.8F)*0.035F; mantle.zRot=(float)Math.sin(phase*.47F)*.025F;
  crown.zRot=(float)Math.sin(phase*.31F)*.03F;
 }
}