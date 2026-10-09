package com.supertotem.client;

import com.supertotem.SuperTotemMod;
import com.supertotem.entity.FlashbangEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FlashbangRenderer extends MobRenderer<FlashbangEntity, FlashbangRenderState, FlashbangModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SuperTotemMod.MOD_ID, "textures/entity/flashbang.png");

    public FlashbangRenderer(EntityRendererProvider.Context context) {
        super(context, new FlashbangModel(context.bakeLayer(FlashbangModel.LAYER)), 0.4F);
    }

    @Override
    public FlashbangRenderState createRenderState() {
        return new FlashbangRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(FlashbangRenderState state) {
        return TEXTURE;
    }

    @Override
    public void extractRenderState(FlashbangEntity entity, FlashbangRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.armsUp = entity.isArmsUp();
    }
}
