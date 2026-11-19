package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.entity.DeathEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

/** Death's predatory animation: restrained stalking, then a low, driving hunting gait. */
public final class DeathModel extends HumanoidModel<DeathEntity> {
    public DeathModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(DeathEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float movement = Math.min(1.0F, limbSwingAmount);
        float phase = ageInTicks * 0.075F;
        boolean hunting = entity.getTarget() != null && entity.getTarget().isAlive();

        body.xRot = 0.10F + movement * (hunting ? 0.24F : 0.10F);
        body.y += (float) Math.sin(phase * 2.0F) * 0.035F;
        head.xRot += hunting ? 0.10F : (float) Math.sin(phase) * 0.025F;
        head.yRot += (float) Math.sin(phase * 0.63F) * 0.025F;

        float swing = (float) Math.cos(limbSwing * 0.62F) * movement;
        rightArm.xRot = -0.16F + swing * (hunting ? 0.72F : 0.42F);
        leftArm.xRot = -0.08F - swing * (hunting ? 0.54F : 0.42F);
        rightArm.zRot = 0.08F + (float) Math.sin(phase * 0.7F) * 0.025F;
        leftArm.zRot = -0.08F - (float) Math.sin(phase * 0.7F) * 0.025F;
        rightLeg.xRot = (float) Math.cos(limbSwing * 0.62F + Math.PI) * movement * 0.85F;
        leftLeg.xRot = (float) Math.cos(limbSwing * 0.62F) * movement * 0.85F;
    }
}
