package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.entity.FearEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class FearModel extends HumanoidModel<FearEntity> {
    private final ModelPart veil;
    private final ModelPart crown;

    public FearModel(ModelPart root) {
        super(root);
        veil = root.getChild("body").getChild("veil");
        crown = root.getChild("head").getChild("crown");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        root.getChild("body").addOrReplaceChild("veil",
                CubeListBuilder.create().texOffs(0, 32).addBox(-5.0F, -1.0F, -1.0F, 10.0F, 16.0F, 1.0F),
                PartPose.offset(0.0F, -1.0F, 2.0F));
        root.getChild("head").addOrReplaceChild("crown",
                CubeListBuilder.create().texOffs(44, 32).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(0.0F, -7.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(FearEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float phase = ageInTicks * 0.035F;
        body.xRot = 0.025F + (float) Math.sin(phase) * 0.018F;
        body.yRot = (float) Math.sin(phase * 0.63F) * 0.025F;
        head.xRot = headPitch * ((float) Math.PI / 180.0F) + (float) Math.sin(phase * 0.8F) * 0.018F;
        head.yRot = netHeadYaw * ((float) Math.PI / 180.0F) + (float) Math.sin(phase * 0.47F) * 0.035F;
        veil.xRot = 0.06F + (float) Math.sin(phase * 1.3F) * 0.045F;
        veil.zRot = (float) Math.sin(phase * 0.7F) * 0.025F;
        crown.zRot = (float) Math.sin(phase * 0.41F) * 0.035F;
        rightArm.xRot = -0.035F + (float) Math.sin(phase * 0.9F) * 0.012F;
        leftArm.xRot = -0.035F - (float) Math.sin(phase * 0.9F) * 0.012F;
        rightLeg.xRot = 0.0F;
        leftLeg.xRot = 0.0F;
    }
}
