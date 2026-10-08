package com.supertotem.client;

import com.supertotem.TotemColorPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class SuperTotemClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(TotemColorPayload.TYPE,
                (payload, context) -> TotemColors.add(payload.entityId(), payload.rgb()));
    }
}
