package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.entity.NightmareCreature;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** Shared rig with strongly differentiated NIGHTMARE silhouettes and deliberately unstable motion. */
public final class NightmareCreatureModel extends HumanoidModel<NightmareCreature> {
    private final ModelPart crown, ribCage, mantle, snout, jaw, tail, dorsalSpines;
    private final ModelPart leftHorn, rightHorn, leftHook, rightHook, leftTendril, rightTendril;

    public NightmareCreatureModel(ModelPart root) {
        super(root);
        crown = root.getChild("head").getChild("crown");
        snout = root.getChild("head").getChild("snout");
        jaw = root.getChild("head").getChild("jaw");
        leftHorn = root.getChild("head").getChild("left_horn");
        rightHorn = root.getChild("head").getChild("right_horn");
        ribCage = root.getChild("body").getChild("rib_cage");
        mantle = root.getChild("body").getChild("mantle");
        tail = root.getChild("body").getChild("tail");
        dorsalSpines = root.getChild("body").getChild("dorsal_spines");
        leftTendril = root.getChild("body").getChild("left_tendril");
        rightTendril = root.getChild("body").getChild("right_tendril");
        leftHook = root.getChild("left_arm").getChild("left_hook");
        rightHook = root.getChild("right_arm").getChild("right_hook");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        root.getChild("head").addOrReplaceChild("crown",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-4.5F, -10.0F, -2.0F, 9.0F, 3.0F, 4.0F), PartPose.ZERO);
        root.getChild("head").addOrReplaceChild("snout",
                CubeListBuilder.create().texOffs(0, 48)
                        .addBox(-2.0F, -2.0F, -5.0F, 4.0F, 3.0F, 4.0F), PartPose.ZERO);
        root.getChild("head").addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(16, 48)
                        .addBox(-2.5F, -1.0F, -5.0F, 5.0F, 2.0F, 5.0F),
                PartPose.offset(0.0F, 2.0F, -0.5F));
        root.getChild("head").addOrReplaceChild("left_horn",
                CubeListBuilder.create().texOffs(32, 48)
                        .addBox(-4.0F, -8.0F, -1.0F, 2.0F, 7.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.getChild("head").addOrReplaceChild("right_horn",
                CubeListBuilder.create().texOffs(40, 48)
                        .addBox(2.0F, -7.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.getChild("body").addOrReplaceChild("rib_cage",
                CubeListBuilder.create().texOffs(32, 32)
                        .addBox(-3.5F, -1.0F, -2.6F, 7.0F, 7.0F, 1.0F),
                PartPose.ZERO);
        root.getChild("body").addOrReplaceChild("mantle",
                CubeListBuilder.create().texOffs(0, 40)
                        .addBox(-4.5F, -1.0F, 1.5F, 9.0F, 10.0F, 1.0F), PartPose.ZERO);
        root.getChild("body").addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(16, 48)
                        .addBox(-1.5F, 7.0F, 1.0F, 3.0F, 7.0F, 3.0F), PartPose.ZERO);
        root.getChild("body").addOrReplaceChild("dorsal_spines",
                CubeListBuilder.create().texOffs(32, 48)
                        .addBox(-3.0F, -4.0F, 1.5F, 6.0F, 4.0F, 2.0F), PartPose.ZERO);
        root.getChild("body").addOrReplaceChild("left_tendril",
                CubeListBuilder.create().texOffs(48, 0)
                        .addBox(-5.0F, 3.0F, 1.0F, 1.0F, 11.0F, 1.0F)
                        .addBox(-6.0F, 12.0F, 0.0F, 2.0F, 5.0F, 1.0F), PartPose.ZERO);
        root.getChild("body").addOrReplaceChild("right_tendril",
                CubeListBuilder.create().texOffs(48, 16)
                        .addBox(4.0F, 1.0F, 1.0F, 1.0F, 12.0F, 1.0F)
                        .addBox(4.0F, 12.0F, 0.0F, 2.0F, 5.0F, 1.0F), PartPose.ZERO);
        root.getChild("left_arm").addOrReplaceChild("left_hook",
                CubeListBuilder.create().texOffs(48, 32)
                        .addBox(-2.0F, 8.0F, -4.0F, 3.0F, 5.0F, 3.0F), PartPose.ZERO);
        root.getChild("right_arm").addOrReplaceChild("right_hook",
                CubeListBuilder.create().texOffs(48, 40)
                        .addBox(-1.0F, 8.0F, -4.0F, 3.0F, 5.0F, 3.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(NightmareCreature entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float twitch = Mth.sin(ageInTicks * 0.29F) * 0.045F;
        boolean hunting = entity.getTarget() != null;
        crown.yRot = Mth.sin(ageInTicks * 0.035F) * 0.06F;
        ribCage.xRot = 0.04F + Mth.sin(ageInTicks * 0.12F) * 0.04F;
        mantle.xRot = 0.08F + Mth.sin(ageInTicks * 0.055F) * 0.06F;
        jaw.xRot = hunting ? 0.28F + Mth.sin(ageInTicks * 0.21F) * 0.16F : 0.025F;
        leftHorn.zRot = 0.06F + twitch;
        rightHorn.zRot = -0.10F - twitch * 0.7F;
        leftTendril.zRot = Mth.sin(ageInTicks * 0.13F) * 0.18F;
        rightTendril.zRot = Mth.sin(ageInTicks * 0.17F + 1.7F) * 0.22F;
        snout.visible = entity.isVariant("nightmare_hound");
        jaw.visible = entity.isVariant("nightmare_hound") || entity.isVariant("nightmare_false_survivor")
                || entity.isVariant("nightmare_unbound");
        tail.visible = entity.isVariant("nightmare_hound");
        dorsalSpines.visible = entity.isVariant("nightmare_wardened") || entity.isVariant("nightmare_unbound");
        leftHorn.visible = entity.isVariant("nightmare_wardened") || entity.isVariant("nightmare_unbound")
                || entity.isVariant("nightmare_hound");
        rightHorn.visible = leftHorn.visible;
        leftTendril.visible = entity.isVariant("nightmare_unbound") || entity.isVariant("nightmare_watcher");
        rightTendril.visible = leftTendril.visible;
        leftHook.visible = true;
        rightHook.visible = true;

        if (entity.isVariant("nightmare_hound")) {
            body.xRot = 0.78F + limbSwingAmount * 0.18F;
            body.yScale = 0.88F;
            head.xScale = 1.16F;
            head.yScale = 1.10F;
            head.xRot -= 0.28F;
            jaw.xRot = hunting ? 0.48F + Mth.sin(ageInTicks * 0.32F) * 0.20F : 0.12F;
            leftArm.xRot = -1.08F + Mth.cos(limbSwing * 0.72F) * limbSwingAmount * 0.78F;
            rightArm.xRot = -1.08F - Mth.cos(limbSwing * 0.72F) * limbSwingAmount * 0.78F;
            leftLeg.xRot += Mth.cos(limbSwing * 0.72F) * limbSwingAmount * 0.82F;
            rightLeg.xRot -= Mth.cos(limbSwing * 0.72F) * limbSwingAmount * 0.82F;
            tail.xRot = 0.32F + Mth.sin(ageInTicks * 0.28F) * 0.22F;
            leftHook.xRot = -0.30F; rightHook.xRot = -0.30F;
        } else if (entity.isVariant("nightmare_wardened")) {
            body.xScale = 1.36F; body.yScale = 1.22F;
            head.xScale = 1.12F; head.yScale = 1.08F;
            body.xRot = 0.16F + Mth.sin(ageInTicks * 0.06F) * 0.035F;
            leftArm.xScale = rightArm.xScale = 1.25F;
            leftArm.xRot -= 0.18F; rightArm.xRot -= 0.12F;
            dorsalSpines.xRot = Mth.sin(ageInTicks * 0.16F) * 0.10F;
            leftHorn.zRot = 0.18F; rightHorn.zRot = -0.24F;
        } else if (entity.isVariant("nightmare_watcher")) {
            body.xScale = 0.78F; body.yScale = 1.22F;
            head.xScale = 0.90F; head.yScale = 1.16F;
            body.xRot = 0.12F;
            head.xRot += twitch * 2.0F;
            head.yRot += Mth.sin(ageInTicks * 0.10F) * 0.17F;
            head.zRot = Mth.sin(ageInTicks * 0.07F) * 0.07F;
            leftArm.xRot = -0.10F + Mth.sin(ageInTicks * 0.12F) * 0.08F;
            rightArm.xRot = -0.42F - Mth.sin(ageInTicks * 0.16F) * 0.08F;
            leftTendril.xRot = -0.15F; rightTendril.xRot = 0.24F;
        } else if (entity.isVariant("nightmare_false_survivor")) {
            body.xRot = 0.08F + Mth.sin(ageInTicks * 0.045F) * 0.025F;
            body.yRot = Mth.sin(ageInTicks * 0.031F) * 0.08F;
            head.xRot += 0.035F + Mth.sin(ageInTicks * 0.17F) * 0.09F;
            head.yRot += Mth.sin(ageInTicks * 0.073F) * 0.055F;
            rightArm.zRot = -0.32F; leftArm.zRot = 0.12F;
            rightArm.xRot -= 0.20F; leftArm.xRot += 0.08F;
            jaw.xRot = hunting ? 0.40F : 0.025F;
        } else {
            body.xRot = 0.24F + Mth.sin(ageInTicks * 0.21F) * 0.09F;
            body.yRot = Mth.sin(ageInTicks * 0.12F) * 0.10F;
            head.xRot += twitch * 2.2F;
            leftArm.xRot -= 0.34F; rightArm.xRot += 0.18F;
            leftArm.zRot = -0.34F; rightArm.zRot = 0.26F;
            leftTendril.xRot = Mth.sin(ageInTicks * 0.19F) * 0.30F;
            rightTendril.xRot = Mth.sin(ageInTicks * 0.23F + 1.2F) * 0.34F;
        }
    }
}
