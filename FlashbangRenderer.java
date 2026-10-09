package com.supertotem.client;

import com.supertotem.entity.FlashbangEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FlashbangRenderer extends MobRenderer<FlashbangEntity, FlashbangRenderState, FlashbangModel> {
    // Textura propia de Minecraft (concreto negro): es un negro liso, asi nunca puede salir rosa/negro por falta de archivo.
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/block/black_concrete.png");

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
