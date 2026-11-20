package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.StalkerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
/** Narrow, forward-leaning hunter with a long, reaching gait. */
public final class StalkerModel extends HumanoidModel<StalkerEntity> {
 public StalkerModel(ModelPart root){super(root);}
 @Override public void setupAnim(StalkerEntity e,float swing,float amount,float age,float yaw,float pitch){
  super.setupAnim(e,swing,amount,age,yaw,pitch);
  body.xScale=0.82F; body.yScale=1.12F; head.xScale=0.88F; rightLeg.yScale=1.08F; leftLeg.yScale=1.08F;
  float tension=e.getTarget()!=null?1.0F:0.0F;
  body.xRot=0.22F+amount*.10F+(e.isSprinting()?-.14F:0.0F)+(float)Math.sin(age*.09F)*.018F;
  head.xRot-=0.08F+tension*.07F;
  float reach=(float)Math.sin(age*0.19F)*0.08F;
  rightArm.xRot-=0.24F+reach+amount*.16F; leftArm.xRot-=0.24F-reach-amount*.16F;
  rightLeg.xRot+=Mth.cos(swing*.7F)*amount*.22F;
  leftLeg.xRot-=Mth.cos(swing*.7F)*amount*.22F;
  rightArm.zRot=-0.10F; leftArm.zRot=0.10F;
  rightArm.yScale=1.16F; leftArm.yScale=1.16F;
 }
}