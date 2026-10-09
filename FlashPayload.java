package com.supertotem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Servidor -> cliente: "ponte la pantalla blanca" (explosion de un Flashbang cerca). */
public record FlashPayload() implements CustomPacketPayload {
    public static final FlashPayload INSTANCE = new FlashPayload();

    public static final CustomPacketPayload.Type<FlashPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SuperTotemMod.MOD_ID, "flash"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlashPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
