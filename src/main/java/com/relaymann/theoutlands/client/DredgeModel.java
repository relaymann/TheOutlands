package com.relaymann.theoutlands.client;
import com.relaymann.theoutlands.entity.DredgeEntity;import net.minecraft.client.model.EntityModel;import net.minecraft.client.model.geom.ModelPart;import net.minecraft.client.model.geom.PartPose;import net.minecraft.client.model.geom.builders.*;import net.minecraft.client.renderer.RenderType;import net.minecraft.util.Mth;import com.mojang.blaze3d.vertex.PoseStack;

public final class DredgeModel extends EntityModel<DredgeEntity>{
 private final ModelPart root,head,body,leftFront,rightFront,leftRear,rightRear,spines,tail,mandibles;
 public DredgeModel(ModelPart r){super(RenderType::entityCutoutNoCull);root=r;head=r.getChild("head");body=r.getChild("body");leftFront=r.getChild("left_front");rightFront=r.getChild("right_front");leftRear=r.getChild("left_rear");rightRear=r.getChild("right_rear");spines=r.getChild("spines");tail=r.getChild("tail");mandibles=r.getChild("mandibles");}
 public static LayerDefinition createBodyLayer(){MeshDefinition m=new MeshDefinition();PartDefinition r=m.getRoot();
 r.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,0).addBox(-5,-3,-10,10,7,20).texOffs(0,28).addBox(-4,-5,-7,8,3,14),PartPose.offset(0,7,0));
 r.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,46).addBox(-4,-3,-8,8,5,8).texOffs(32,46).addBox(-3,0,-10,6,2,4),PartPose.offset(0,6,-8));
 r.addOrReplaceChild("left_front",CubeListBuilder.create().texOffs(32,0).addBox(-1,-1,-2,3,3,10).texOffs(44,14).addBox(0,1,-5,2,2,5),PartPose.offset(4,7,-5));
 r.addOrReplaceChild("right_front",CubeListBuilder.create().texOffs(32,0).addBox(-2,-1,-2,3,3,10).texOffs(44,14).addBox(-2,1,-5,2,2,5),PartPose.offset(-4,7,-5));
 r.addOrReplaceChild("left_rear",CubeListBuilder.create().texOffs(32,0).addBox(-1,-1,-2,3,3,10),PartPose.offset(4,7,5));
 r.addOrReplaceChild("right_rear",CubeListBuilder.create().texOffs(32,0).addBox(-2,-1,-2,3,3,10),PartPose.offset(-4,7,5));
 r.addOrReplaceChild("spines",CubeListBuilder.create().texOffs(0,58).addBox(-1,-7,-5,2,4,3).addBox(-1,-6,-1,2,4,3).addBox(-1,-5,3,2,4,3).addBox(-1,-4,7,2,4,3),PartPose.offset(0,7,0));
 r.addOrReplaceChild("tail",CubeListBuilder.create().texOffs(40,48).addBox(-2,-2,0,4,4,9).texOffs(48,42).addBox(-1,-1,8,2,2,8),PartPose.offset(0,8,9));
 r.addOrReplaceChild("mandibles",CubeListBuilder.create().texOffs(32,54).addBox(-4,0,-3,2,2,5).addBox(2,0,-3,2,2,5),PartPose.offset(0,7,-14));
 return LayerDefinition.create(m,64,64);}
 @Override public void setupAnim(DredgeEntity e,float s,float a,float age,float yaw,float pitch){head.yRot=yaw*Mth.DEG_TO_RAD;head.xRot=pitch*Mth.DEG_TO_RAD;float g=Mth.cos(s*.8F)*a;body.xRot=.08F+(float)Math.sin(age*.08F)*.025F;leftFront.xRot=g*.9F;rightFront.xRot=-g*.9F;leftRear.xRot=-g*.9F;rightRear.xRot=g*.9F;spines.xRot=(float)Math.sin(age*.1F)*.03F;tail.yRot=(float)Math.sin(age*.12F)*.18F;tail.xRot=e.isLunging()?-0.38F:.12F;mandibles.xRot=e.isLunging()?-.5F:.08F+(float)Math.sin(age*.16F)*.05F;}
 @Override public void renderToBuffer(PoseStack p,com.mojang.blaze3d.vertex.VertexConsumer v,int l,int o,int c){root.render(p,v,l,o,c);}
}
