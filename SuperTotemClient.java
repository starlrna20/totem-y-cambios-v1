package com.supertotem.client;

import com.supertotem.FlashPayload;
import com.supertotem.ModEntities;
import com.supertotem.TotemColorPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class SuperTotemClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(TotemColorPayload.TYPE,
                (payload, context) -> TotemColors.add(payload.entityId(), payload.rgb()));

        ClientPlayNetworking.registerGlobalReceiver(FlashPayload.TYPE,
                (payload, context) -> FlashOverlay.trigger());
        FlashOverlay.register();

        EntityModelLayerRegistry.registerModelLayer(FlashbangModel.LAYER, FlashbangModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntities.FLASHBANG, FlashbangRenderer::new);
    }
}
