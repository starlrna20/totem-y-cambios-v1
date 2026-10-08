package com.supertotem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Mensaje servidor -> clientes: "el jugador con esta id uso un totem de este color". */
public record TotemColorPayload(int entityId, int rgb) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TotemColorPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SuperTotemMod.MOD_ID, "totem_color"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TotemColorPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TotemColorPayload::entityId,
            ByteBufCodecs.INT, TotemColorPayload::rgb,
            TotemColorPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
