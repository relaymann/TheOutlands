package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.NullApostleEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
/** Featureless silhouette with slight, asynchronous twitches. */
public final class NullApostleModel extends HumanoidModel<NullApostleEntity> {
 public NullApostleModel(ModelPart root) { super(root); }
 @Override public void setupAnim(NullApostleEntity entity,float limbSwing,float limbSwingAmount,float ageInTicks,float netHeadYaw,float headPitch) {
  super.setupAnim(entity,limbSwing,limbSwingAmount,ageInTicks,netHeadYaw,headPitch);
  float phase=ageInTicks*0.11F+entity.getId()*0.73F, twitch=(float)Math.sin(phase)*0.025F;
  head.yRot+=twitch; head.zRot=(float)Math.sin(phase*0.47F)*0.025F;
  body.zRot=(float)Math.sin(phase*0.31F)*0.012F; rightArm.zRot=0.025F+twitch; leftArm.zRot=-0.025F-twitch;
 }
}
