package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.ObserverEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
/** Rigid, unnaturally still figure; only its head makes restrained movements. */
public final class ObserverModel extends HumanoidModel<ObserverEntity> {
 public ObserverModel(ModelPart root){super(root);}
 @Override public void setupAnim(ObserverEntity e,float swing,float amount,float age,float yaw,float pitch){
  super.setupAnim(e,swing,amount,age,yaw,pitch);
  body.xScale=0.88F; body.yScale=1.08F; head.xScale=0.92F;
  float still=(float)Math.sin(age*0.035F)*0.012F;
  body.xRot=0.015F; body.yRot=still;
  rightArm.xRot=-0.035F; leftArm.xRot=-0.035F;
  rightArm.zRot=-0.035F; leftArm.zRot=0.035F;
  float gait=e.isAdvancing()?Mth.cos(swing*.62F)*amount:0.0F;
  rightLeg.xRot=gait*.55F; leftLeg.xRot=-gait*.55F;
  rightArm.xRot=e.isAdvancing()?-.08F: -.035F; leftArm.xRot=e.isAdvancing()?-.08F: -.035F;
  head.zRot=(float)Math.sin(age*0.021F)*0.018F;
 }
}