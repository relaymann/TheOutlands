package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.CaveJarEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
/** Elongated, forward-hunched cave predator silhouette. */
public final class CaveJarModel extends HumanoidModel<CaveJarEntity> {
 public CaveJarModel(ModelPart root){super(root);}
 @Override public void setupAnim(CaveJarEntity e,float swing,float amount,float age,float yaw,float pitch){
  super.setupAnim(e,swing,amount,age,yaw,pitch);
  head.xRot+=0.12F; body.xRot=0.16F+(float)Math.sin(age*0.12F)*0.025F;
  rightArm.xRot-=0.42F; leftArm.xRot-=0.42F;
  rightArm.zRot=-0.16F; leftArm.zRot=0.16F;
  rightLeg.xRot*=0.85F; leftLeg.xRot*=0.85F;
  rightArm.yScale=1.22F; leftArm.yScale=1.22F;
 }
}