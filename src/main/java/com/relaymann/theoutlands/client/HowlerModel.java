package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.HowlerEntity;import net.minecraft.client.model.EntityModel;import net.minecraft.client.model.geom.ModelPart;import net.minecraft.client.model.geom.PartPose;import net.minecraft.client.model.geom.builders.*;import net.minecraft.client.renderer.RenderType;import net.minecraft.util.Mth;import com.mojang.blaze3d.vertex.PoseStack;
/** Heavy sentinel with a hinged jaw, broad shoulders and long fingers. */
public final class HowlerModel extends EntityModel<HowlerEntity>{
 private final ModelPart root,head,jaw,body,leftArm,rightArm,leftLeg,rightLeg,throat,shoulders;
 public HowlerModel(ModelPart r){super(RenderType::entityCutoutNoCull);root=r;head=r.getChild("head");jaw=r.getChild("jaw");body=r.getChild("body");leftArm=r.getChild("left_arm");rightArm=r.getChild("right_arm");leftLeg=r.getChild("left_leg");rightLeg=r.getChild("right_leg");throat=r.getChild("throat");shoulders=r.getChild("shoulders");}
 public static LayerDefinition createBodyLayer(){MeshDefinition m=new MeshDefinition();PartDefinition r=m.getRoot();
 r.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4,-8,-4,8,8,7).texOffs(0,16).addBox(-3,-5,-6,6,3,3),PartPose.offset(0,-12,0));
 r.addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(28,0).addBox(-3,0,-5,6,3,6),PartPose.offset(0,-9,0));
 r.addOrReplaceChild("body",CubeListBuilder.create().texOffs(28,12).addBox(-5,-13,-3,10,14,6).texOffs(28,33).addBox(-6,-11,-2,12,7,4),PartPose.ZERO);
 r.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(0,28).addBox(-2,-2,-2,4,14,4).texOffs(16,28).addBox(-2,11,-3,4,5,5),PartPose.offset(7,-10,0));
 r.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(0,28).addBox(-2,-2,-2,4,14,4).texOffs(16,28).addBox(-2,11,-3,4,5,5),PartPose.offset(-7,-10,0));
 r.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(48,0).addBox(-2,0,-2,4,12,4),PartPose.offset(3,0,0));
 r.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(48,0).addBox(-2,0,-2,4,12,4),PartPose.offset(-3,0,0));
 r.addOrReplaceChild("throat",CubeListBuilder.create().texOffs(0,48).addBox(-3,-2,-3,6,6,5),PartPose.offset(0,-10,-2));
 r.addOrReplaceChild("shoulders",CubeListBuilder.create().texOffs(32,48).addBox(-8,-2,-2,16,4,5).texOffs(44,32).addBox(-9,-4,-1,3,4,3).addBox(6,-4,-1,3,4,3),PartPose.offset(0,-10,0));
 return LayerDefinition.create(m,64,64);}
 @Override public void setupAnim(HowlerEntity e,float s,float a,float age,float yaw,float pitch){head.yRot=yaw*Mth.DEG_TO_RAD;head.xRot=pitch*Mth.DEG_TO_RAD;jaw.xRot=e.isCalling()?-.92F:.08F+(float)Math.sin(age*.08F)*.04F;throat.xScale=e.isCalling()?1.22F:1.0F;throat.yScale=e.isCalling()?1.28F:1.0F;float g=Mth.cos(s*.68F)*a;body.xRot=e.isCalling()?-.22F:.06F;leftArm.xRot=e.isCalling()?-1.15F:g*.55F;rightArm.xRot=e.isCalling()?-1.15F:-g*.55F;leftLeg.xRot=-g*.8F;rightLeg.xRot=g*.8F;shoulders.xRot=e.isCalling()?-.16F:.02F;}
 @Override public void renderToBuffer(PoseStack p,com.mojang.blaze3d.vertex.VertexConsumer v,int l,int o,int c){root.render(p,v,l,o,c);}
}
