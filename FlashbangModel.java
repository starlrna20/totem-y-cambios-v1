package com.supertotem.client;

import com.supertotem.SuperTotemMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Figura de palitos: cabeza cuadrada sin cara, cuerpo, brazos y piernas delgados (3 px). */
public class FlashbangModel extends EntityModel<FlashbangRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(SuperTotemMod.MOD_ID, "flashbang"), "main");

    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public FlashbangModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -7.0F, -3.5F, 7.0F, 7.0F, 7.0F),
                PartPose.offset(0.0F, 4.0F, 0.0F));
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 14).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F),
                PartPose.offset(0.0F, 4.0F, 0.0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(0, 14).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F),
                PartPose.offset(2.5F, 5.0F, 0.0F));
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(0, 14).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F),
                PartPose.offset(-2.5F, 5.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 14).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F),
                PartPose.offset(1.5F, 14.0F, 0.0F));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 14).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F),
                PartPose.offset(-1.5F, 14.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(FlashbangRenderState state) {
        super.setupAnim(state);
        float deg = (float) Math.PI / 180.0F;
        this.head.yRot = state.yRot * deg;
        this.head.xRot = state.xRot * deg;

        float pos = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        this.rightLeg.xRot = Mth.cos(pos * 0.6662F) * 1.4F * speed;
        this.leftLeg.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI) * 1.4F * speed;

        if (state.armsUp) {
            // brazos arriba antes de explotar
            this.leftArm.xRot = -2.9F;
            this.rightArm.xRot = -2.9F;
            this.leftArm.zRot = -0.15F;
            this.rightArm.zRot = 0.15F;
        } else {
            // brazos abiertos en diagonal, como en la figura de palitos
            this.leftArm.zRot = -0.6F;
            this.rightArm.zRot = 0.6F;
            this.leftArm.xRot = Mth.cos(pos * 0.6662F) * 0.8F * speed;
            this.rightArm.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI) * 0.8F * speed;
        }
    }
}
