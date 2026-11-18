package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.NullEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
/** Standard player proportions with unnaturally restrained idle motion. */
public final class NullModel extends HumanoidModel<NullEntity> {
 public NullModel(ModelPart root) { super(root); }
 @Override public void setupAnim(NullEntity entity,float limbSwing,float limbSwingAmount,float ageInTicks,float netHeadYaw,float headPitch) {
  super.setupAnim(entity,limbSwing,limbSwingAmount,ageInTicks,netHeadYaw,headPitch);
  float phase=ageInTicks*0.045F, breath=(float)Math.sin(phase);
  body.y+=breath*0.035F; head.yRot+=(float)Math.sin(phase*0.63F)*0.035F;
  head.zRot=(float)Math.sin(phase*0.41F)*0.018F; rightArm.xRot+=breath*0.012F; leftArm.xRot-=breath*0.012F;
 }
}
