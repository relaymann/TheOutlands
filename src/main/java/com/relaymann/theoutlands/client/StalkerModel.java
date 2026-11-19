package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.StalkerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
/** Narrow, forward-leaning hunter with a long, reaching gait. */
public final class StalkerModel extends HumanoidModel<StalkerEntity> {
 public StalkerModel(ModelPart root){super(root);}
 @Override public void setupAnim(StalkerEntity e,float swing,float amount,float age,float yaw,float pitch){
  super.setupAnim(e,swing,amount,age,yaw,pitch);
  body.xRot=0.22F; head.xRot-=0.08F;
  float reach=(float)Math.sin(age*0.19F)*0.08F;
  rightArm.xRot-=0.24F+reach; leftArm.xRot-=0.24F-reach;
  rightArm.zRot=-0.10F; leftArm.zRot=0.10F;
  rightArm.yScale=1.16F; leftArm.yScale=1.16F;
 }
}