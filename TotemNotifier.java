package com.supertotem;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

/** Avisa en el chat (a todos) cuando un jugador usa un totem. */
public final class TotemNotifier {
    /** Clave que usa el mod Permadeath para marcar la "Medalla de Superviviente". */
    private static final String MEDAL_KEY = "PermadeathSurvivorMedal";

    /** Colores de las particulas del totem (0xRRGGBB). */
    private static final int YELLOW = 0xF5D31A;
    private static final int PURPLE = 0xB31FE6;
    private static final int RED = 0xE01B1B;

    private TotemNotifier() {}

    public static void notifyUse(ServerPlayer player, ItemStack used) {
        String what;
        Style style = Style.EMPTY.withColor(ChatFormatting.YELLOW);   // totem normal: amarillo
        int particleColor = YELLOW;

        if (used.is(SuperTotemMod.SUPREME_TOTEM)) {
            what = "un Tótem Astraeus";
            style = Style.EMPTY.withColor(TextColor.fromRgb(PURPLE));  // Astraeus: morado/magenta
            particleColor = PURPLE;
        } else if (isSurvivorMedal(used)) {
            what = "una Medalla de Superviviente";
            style = Style.EMPTY.withColor(ChatFormatting.RED);         // medalla: rojo
            particleColor = RED;
        } else {
            what = "un Tótem";
        }

        Component msg = Component.literal("★ " + player.getGameProfile().getName() + " ha usado " + what)
                .withStyle(style);
        MinecraftServer server = player.getServer();
        if (server != null) server.getPlayerList().broadcastSystemMessage(msg, false);

        // Color de las particulas: se avisa al propio jugador y a quienes lo estan viendo
        TotemColorPayload payload = new TotemColorPayload(player.getId(), particleColor);
        if (ServerPlayNetworking.canSend(player, TotemColorPayload.TYPE)) ServerPlayNetworking.send(player, payload);
        for (ServerPlayer other : PlayerLookup.tracking(player)) {
            if (ServerPlayNetworking.canSend(other, TotemColorPayload.TYPE)) ServerPlayNetworking.send(other, payload);
        }
    }

    private static boolean isSurvivorMedal(ItemStack stack) {
        if (!stack.is(Items.TOTEM_OF_UNDYING)) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains(MEDAL_KEY);
    }
}
