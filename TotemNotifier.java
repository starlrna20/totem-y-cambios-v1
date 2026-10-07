package com.supertotem;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

/** Avisa en el chat (a todos) cuando un jugador usa un totem. */
public final class TotemNotifier {
    /** Clave que usa el mod Permadeath para marcar la "Medalla de Superviviente". */
    private static final String MEDAL_KEY = "PermadeathSurvivorMedal";

    private TotemNotifier() {}

    public static void notifyUse(ServerPlayer player, ItemStack used) {
        String what;
        ChatFormatting color = ChatFormatting.YELLOW;

        if (used.is(SuperTotemMod.SUPREME_TOTEM)) {
            what = "un Tótem Astraeus";
        } else if (isSurvivorMedal(used)) {
            what = "la Medalla de Superviviente";
            color = ChatFormatting.RED;
        } else {
            what = "un Tótem";
        }

        Component msg = Component.literal("★ " + player.getGameProfile().getName() + " usó " + what)
                .withStyle(color);
        MinecraftServer server = player.getServer();
        if (server != null) server.getPlayerList().broadcastSystemMessage(msg, false);
    }

    private static boolean isSurvivorMedal(ItemStack stack) {
        if (!stack.is(Items.TOTEM_OF_UNDYING)) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains(MEDAL_KEY);
    }
}
