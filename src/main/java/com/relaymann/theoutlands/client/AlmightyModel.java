package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.entity.AlmightyEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

/** The Almighty's slow, deliberate animation changes as the three encounter phases advance. */
public final class AlmightyModel extends HumanoidModel<AlmightyEntity> {
    public AlmightyModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(AlmightyEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float health = entity.getMaxHealth() <= 0.0F ? 1.0F : entity.getHealth() / entity.getMaxHealth();
        float phase = health > 0.66F ? 1.0F : health > 0.33F ? 2.0F : 3.0F;
        float time = ageInTicks * (phase == 3.0F ? 0.11F : 0.055F);
        float breath = (float) Math.sin(time);

        body.y += 0.10F + breath * (phase == 3.0F ? 0.075F : 0.035F);
        body.xRot = 0.025F + (phase - 1.0F) * 0.035F;
        head.xRot += (float) Math.sin(time * 0.47F) * 0.018F;
        head.yRot += (float) Math.sin(time * 0.31F) * 0.022F;

        float spread = phase == 1.0F ? 0.08F : phase == 2.0F ? 0.20F : 0.34F;
        rightArm.zRot = spread + (float) Math.sin(time * 0.8F) * 0.035F;
        leftArm.zRot = -spread - (float) Math.sin(time * 0.8F) * 0.035F;
        rightArm.xRot = -0.18F + breath * 0.045F - (phase - 1.0F) * 0.08F;
        leftArm.xRot = -0.18F - breath * 0.045F - (phase - 1.0F) * 0.08F;
        rightLeg.xRot *= 0.35F;
        leftLeg.xRot *= 0.35F;
    }
}
