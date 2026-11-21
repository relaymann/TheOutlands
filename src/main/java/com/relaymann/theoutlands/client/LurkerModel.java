package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.LurkerEntity;
import net.minecraft.client.model.EntityModel;import net.minecraft.client.model.geom.ModelPart;import net.minecraft.client.model.geom.PartPose;import net.minecraft.client.model.geom.builders.*;import net.minecraft.client.renderer.RenderType;import net.minecraft.util.Mth;import com.mojang.blaze3d.vertex.PoseStack;
/** Tall, narrow ambusher with an underslung skull and elongated hooked arms. */
public final class LurkerModel extends EntityModel<LurkerEntity>{
 private final ModelPart root,head,torso,leftArm,rightArm,leftLeg,rightLeg,backSpines;
 public LurkerModel(ModelPart r){super(RenderType::entityCutoutNoCull);root=r;head=r.getChild("head");torso=r.getChild("torso");leftArm=r.getChild("left_arm");rightArm=r.getChild("right_arm");leftLeg=r.getChild("left_leg");rightLeg=r.getChild("right_leg");backSpines=r.getChild("back_spines");}
 public static LayerDefinition createBodyLayer(){MeshDefinition m=new MeshDefinition();PartDefinition r=m.getRoot();
 r.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-3,-7,-3,6,7,6).texOffs(0,14).addBox(-2,-3,-5,4,2,3),PartPose.offset(0,-13,-1));
 r.addOrReplaceChild("torso",CubeListBuilder.create().texOffs(24,0).addBox(-4,-12,-2,8,14,4).texOffs(24,18).addBox(-3,-8,-3,6,8,1),PartPose.offset(0,-1,0));
 r.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(0,22).addBox(-1,-2,-2,3,15,3).texOffs(12,22).addBox(-1,11,-3,3,5,5),PartPose.offset(5,-9,0));
 r.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(0,22).addBox(-2,-2,-2,3,15,3).texOffs(12,22).addBox(-2,11,-3,3,5,5),PartPose.offset(-5,-9,0));
 r.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(40,0).addBox(-2,0,-2,3,12,4),PartPose.offset(2,11,0));
 r.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(40,0).addBox(-1,0,-2,3,12,4),PartPose.offset(-2,11,0));
 r.addOrReplaceChild("back_spines",CubeListBuilder.create().texOffs(48,32).addBox(-1,-10,-1,2,5,2).addBox(-1,-6,-1,2,4,2).addBox(-1,-2,-1,2,3,2),PartPose.offset(0,-4,2));
 return LayerDefinition.create(m,64,64);}
 @Override public void setupAnim(LurkerEntity e,float s,float a,float age,float yaw,float pitch){head.yRot=yaw*Mth.DEG_TO_RAD;head.xRot=pitch*Mth.DEG_TO_RAD;float g=Mth.cos(s*.7F)*a;torso.xRot=.16F+(e.getPose()==net.minecraft.world.entity.Pose.CROUCHING?.5F:0);leftArm.xRot=-.35F+g*.7F;rightArm.xRot=-.35F-g*.7F;leftLeg.xRot=-g;rightLeg.xRot=g;backSpines.xRot=.12F+(e.getPose()==net.minecraft.world.entity.Pose.CROUCHING?.42F:0);leftArm.zRot=.08F;rightArm.zRot=-.08F;}
 @Override public void renderToBuffer(PoseStack p,com.mojang.blaze3d.vertex.VertexConsumer v,int l,int o,int c){root.render(p,v,l,o,c);}
}
