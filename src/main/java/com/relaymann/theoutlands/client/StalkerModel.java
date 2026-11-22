package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.entity.StalkerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** An emaciated, asymmetrical pursuit predator with a split jaw and exposed ribs. */
public final class StalkerModel extends HumanoidModel<StalkerEntity> {
    private final ModelPart jaw, neck, ribs, spines, leftHook, rightHook, leftClaw, rightClaw;

    public StalkerModel(ModelPart root) {
        super(root);
        jaw = root.getChild("head").getChild("jaw");
        neck = root.getChild("body").getChild("neck");
        ribs = root.getChild("body").getChild("ribs");
        spines = root.getChild("body").getChild("spines");
        leftHook = root.getChild("left_arm").getChild("hook");
        rightHook = root.getChild("right_arm").getChild("hook");
        leftClaw = root.getChild("left_arm").getChild("claw");
        rightClaw = root.getChild("right_arm").getChild("claw");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        root.getChild("head").addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(0, 48)
                        .addBox(-3.0F, -1.0F, -5.0F, 6.0F, 3.0F, 5.0F),
                PartPose.offset(0.0F, 2.0F, -0.5F));
        root.getChild("body").addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(32, 48)
                        .addBox(-2.0F, -8.0F, -2.0F, 4.0F, 9.0F, 4.0F),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        root.getChild("body").addOrReplaceChild("ribs",
                CubeListBuilder.create().texOffs(40, 32)
                        .addBox(-4.0F, -2.0F, -3.0F, 8.0F, 7.0F, 1.0F)
                        .addBox(-3.0F, 0.0F, -3.8F, 1.0F, 5.0F, 1.0F)
                        .addBox(2.0F, -1.0F, -3.8F, 1.0F, 6.0F, 1.0F),
                PartPose.ZERO);
        root.getChild("body").addOrReplaceChild("spines",
                CubeListBuilder.create().texOffs(40, 16)
                        .addBox(-1.0F, -12.0F, 1.0F, 2.0F, 5.0F, 4.0F)
                        .addBox(-1.0F, -6.0F, 1.0F, 2.0F, 5.0F, 4.0F)
                        .addBox(-1.0F, 0.0F, 1.0F, 2.0F, 4.0F, 3.0F),
                PartPose.ZERO);
        root.getChild("left_arm").addOrReplaceChild("hook",
                CubeListBuilder.create().texOffs(48, 32)
                        .addBox(-2.0F, -3.0F, 0.0F, 3.0F, 5.0F, 3.0F),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        root.getChild("right_arm").addOrReplaceChild("hook",
                CubeListBuilder.create().texOffs(48, 32)
                        .addBox(-1.0F, -4.0F, 0.0F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        root.getChild("left_arm").addOrReplaceChild("claw",
                CubeListBuilder.create().texOffs(48, 40)
                        .addBox(-1.0F, 8.0F, -3.0F, 2.0F, 5.0F, 3.0F)
                        .addBox(-2.0F, 11.0F, -4.0F, 1.0F, 3.0F, 1.0F),
                PartPose.ZERO);
        root.getChild("right_arm").addOrReplaceChild("claw",
                CubeListBuilder.create().texOffs(48, 40)
                        .addBox(-1.0F, 8.0F, -3.0F, 2.0F, 5.0F, 3.0F)
                        .addBox(1.0F, 11.0F, -4.0F, 1.0F, 3.0F, 1.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(StalkerEntity entity, float swing, float amount, float age,
                          float yaw, float pitch) {
        super.setupAnim(entity, swing, amount, age, yaw, pitch);
        float watched = entity.getTarget() != null ? 1.0F : 0.0F;
        float step = Mth.cos(swing * 0.72F) * amount;
        float twitch = Mth.sin(age * 0.31F) * 0.045F;
        body.xScale = 0.76F;
        body.yScale = 1.18F;
        body.zScale = 0.92F;
        head.xScale = 0.88F;
        head.yScale = 1.08F;
        body.xRot = 0.30F + amount * 0.18F + (entity.isSprinting() ? -0.30F : 0.0F);
        body.yRot = Mth.sin(age * 0.045F) * 0.055F;
        head.xRot += -0.13F + twitch - watched * 0.10F;
        head.yRot += Mth.sin(age * 0.075F) * 0.035F;
        head.zRot = Mth.sin(age * 0.12F) * 0.025F;
        jaw.xRot = 0.10F + watched * (0.24F + Mth.sin(age * 0.18F) * 0.12F);
        neck.xRot = -0.10F + Mth.sin(age * 0.11F) * 0.035F;
        ribs.xRot = -0.05F + Mth.sin(age * 0.16F) * 0.035F;
        spines.xRot = 0.12F + watched * 0.10F + Mth.sin(age * 0.19F) * 0.04F;
        rightArm.xRot -= 0.34F + step * 0.42F;
        leftArm.xRot -= 0.18F - step * 0.34F;
        rightArm.zRot = -0.19F;
        leftArm.zRot = 0.28F;
        rightArm.yScale = 1.22F;
        leftArm.yScale = 1.12F;
        rightLeg.xRot += step * 0.55F;
        leftLeg.xRot -= step * 0.55F;
        leftHook.xRot = 0.12F + Mth.sin(age * 0.13F) * 0.10F;
        rightHook.xRot = -0.18F + Mth.sin(age * 0.17F + 1.4F) * 0.08F;
        leftClaw.xRot = -0.22F - watched * 0.22F + Mth.sin(age * 0.22F) * 0.08F;
        rightClaw.xRot = -0.32F - watched * 0.28F + Mth.sin(age * 0.20F + 1.0F) * 0.08F;
    }
}
