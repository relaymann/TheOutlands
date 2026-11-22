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

/** Shared articulated silhouette; each Nightmare identity has its own skin and gait. */
public final class NightmareCreatureModel extends HumanoidModel<NightmareCreature> {
    private final ModelPart crown;
    private final ModelPart ribCage;
    private final ModelPart mantle;

    public NightmareCreatureModel(ModelPart root) {
        super(root);
        crown = root.getChild("head").getChild("crown");
        ribCage = root.getChild("body").getChild("rib_cage");
        mantle = root.getChild("body").getChild("mantle");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        root.getChild("head").addOrReplaceChild("crown",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-4.5F, -10.0F, -2.0F, 9.0F, 3.0F, 4.0F),
                PartPose.ZERO);
        root.getChild("body").addOrReplaceChild("rib_cage",
                CubeListBuilder.create().texOffs(32, 32)
                        .addBox(-3.5F, -1.0F, -2.6F, 7.0F, 7.0F, 1.0F),
                PartPose.ZERO);
        root.getChild("body").addOrReplaceChild("mantle",
                CubeListBuilder.create().texOffs(0, 40)
                        .addBox(-4.5F, -1.0F, 1.5F, 9.0F, 10.0F, 1.0F),
                PartPose.ZERO);
        root.getChild("right_arm").addOrReplaceChild("right_claw",
                CubeListBuilder.create().texOffs(48, 40).addBox(-1.0F, 8.0F, -3.0F, 2.0F, 4.0F, 3.0F),
                PartPose.ZERO);
        root.getChild("left_arm").addOrReplaceChild("left_claw",
                CubeListBuilder.create().texOffs(48, 40).addBox(-1.0F, 8.0F, -3.0F, 2.0F, 4.0F, 3.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(NightmareCreature entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        body.xScale = 1.0F;
        body.yScale = 1.0F;
        body.zScale = 1.0F;
        head.xScale = 1.0F;
        head.yScale = 1.0F;
        rightArm.xScale = leftArm.xScale = 1.0F;
        rightLeg.xScale = leftLeg.xScale = 1.0F;
        crown.yRot = Mth.sin(ageInTicks * 0.035F) * 0.025F;
        ribCage.xRot = 0.02F + Mth.sin(ageInTicks * 0.08F) * 0.018F;
        mantle.xRot = 0.04F + Mth.sin(ageInTicks * 0.045F) * 0.025F;

        if (entity.isVariant("nightmare_hound")) {
            body.xRot = 0.62F + limbSwingAmount * 0.12F;
            head.xRot -= 0.24F;
            rightArm.xRot = -0.95F + Mth.cos(limbSwing * 0.7F) * limbSwingAmount * 0.7F;
            leftArm.xRot = -0.95F - Mth.cos(limbSwing * 0.7F) * limbSwingAmount * 0.7F;
            rightLeg.xRot += Mth.cos(limbSwing * 0.7F) * limbSwingAmount * 0.65F;
            leftLeg.xRot -= Mth.cos(limbSwing * 0.7F) * limbSwingAmount * 0.65F;
            rightArm.zRot = -0.18F;
            leftArm.zRot = 0.18F;
        } else if (entity.isVariant("nightmare_wardened")) {
            body.xScale = 1.28F;
            body.yScale = 1.18F;
            head.xScale = 1.08F;
            rightArm.xScale = leftArm.xScale = 1.18F;
            body.xRot = 0.10F;
            rightArm.xRot -= 0.10F;
            leftArm.xRot -= 0.10F;
        } else if (entity.isVariant("nightmare_watcher")) {
            body.xScale = 0.82F;
            body.yScale = 1.16F;
            head.xScale = 0.90F;
            body.xRot = 0.08F;
            rightArm.zRot = -0.06F;
            leftArm.zRot = 0.06F;
            head.zRot = Mth.sin(ageInTicks * 0.022F) * 0.035F;\n            head.yRot += Mth.sin(ageInTicks * 0.11F) * 0.12F;\n            rightArm.xRot = -0.18F + Mth.sin(ageInTicks * 0.08F) * 0.035F;\n            leftArm.xRot = -0.18F - Mth.sin(ageInTicks * 0.08F) * 0.035F;
        } else if (entity.isVariant("nightmare_false_survivor")) {
            body.xRot = 0.06F + Mth.sin(ageInTicks * 0.04F) * 0.012F;
            head.xRot += 0.025F + Mth.sin(ageInTicks * 0.16F) * 0.045F;\n            head.yRot += Mth.sin(ageInTicks * 0.07F) * 0.035F;\n            body.yRot = Mth.sin(ageInTicks * 0.035F) * 0.025F;
            rightArm.zRot = -0.035F;
            leftArm.zRot = 0.035F;
        } else {
            body.xRot = 0.20F + Mth.sin(ageInTicks * 0.19F) * 0.06F;
            rightArm.xRot -= 0.22F;
            leftArm.xRot += 0.22F;
            rightArm.zRot = -0.28F;
            leftArm.zRot = 0.28F;
        }
    }
}
