package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.AlmightyEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
/** The Almighty's three phases visibly unfold its void halo and trailing mantle. */
public final class AlmightyModel extends HumanoidModel<AlmightyEntity> {
 private final ModelPart halo,mantle;
 public AlmightyModel(ModelPart root){super(root);halo=root.getChild("head").getChild("halo");mantle=root.getChild("body").getChild("mantle");}
 public static LayerDefinition createBodyLayer(){
  MeshDefinition mesh=HumanoidModel.createMesh(new CubeDeformation(.12F),0.0F);PartDefinition root=mesh.getRoot();
  root.getChild("head").addOrReplaceChild("halo",CubeListBuilder.create().texOffs(40,32)
   .addBox(-1,-8,-1,2,5,2).addBox(-8,-2,-1,5,2,2).addBox(3,-2,-1,5,2,2),PartPose.offset(0,-5,0));
  root.getChild("body").addOrReplaceChild("mantle",CubeListBuilder.create().texOffs(0,32)
   .addBox(-7,-1,-1,14,16,2),PartPose.offset(0,-1,2));
  return LayerDefinition.create(mesh,64,64);
 }
 @Override public void setupAnim(AlmightyEntity entity,float limbSwing,float limbSwingAmount,float ageInTicks,float netHeadYaw,float headPitch){
  super.setupAnim(entity,limbSwing,limbSwingAmount,ageInTicks,netHeadYaw,headPitch);
  float health=entity.getMaxHealth()<=0.0F?1.0F:entity.getHealth()/entity.getMaxHealth();
  float phase=health>.66F?1.0F:health>.33F?2.0F:3.0F;
  float time=ageInTicks*(phase==3.0F?.11F:.055F),breath=(float)Math.sin(time);
  body.y=.10F+breath*(phase==3.0F?.075F:.035F);
  body.xRot=.025F+(phase-1.0F)*.035F;
  head.xRot+=(float)Math.sin(time*.47F)*.018F;head.yRot+=(float)Math.sin(time*.31F)*.022F;
  float spread=phase==1.0F?.08F:phase==2.0F?.20F:.34F;
  rightArm.zRot=spread+(float)Math.sin(time*.8F)*.035F;leftArm.zRot=-spread-(float)Math.sin(time*.8F)*.035F;
  rightArm.xRot=-.18F+breath*.045F-(phase-1.0F)*.08F;leftArm.xRot=-.18F-breath*.045F-(phase-1.0F)*.08F;
  rightLeg.xRot*=.35F;leftLeg.xRot*=.35F;
  halo.yRot=(float)Math.sin(time*.27F)*.08F;halo.zRot=(phase-1.0F)*.12F+(float)Math.sin(time*.51F)*.035F;
  mantle.xRot=.04F+(float)Math.sin(time*.6F)*.025F+(phase-1.0F)*.06F;
 }
}