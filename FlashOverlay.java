package com.supertotem.client;

import com.supertotem.SuperTotemMod;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/** Pantalla blanca (2 s completa + 3 s difuminandose) y pitido de oido, a la vez y solo para los afectados. */
public final class FlashOverlay {
    private static final long HOLD_MS = 2000;
    private static final long FADE_MS = 3000;
    private static long start = -1;

    /** Pitido (assets/totem_astraeus/sounds/flashbang_ring.ogg). Se reproduce dentro de la cabeza, sin direccion. */
    private static final SoundEvent RING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(SuperTotemMod.MOD_ID, "flashbang_ring"));

    private FlashOverlay() {}

    public static void trigger() {
        start = System.currentTimeMillis();
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RING, 1.0F, 1.0F));
    }

    public static void register() {
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            if (start < 0) return;
            long t = System.currentTimeMillis() - start;
            int alpha;
            if (t < HOLD_MS) {
                alpha = 255;
            } else if (t < HOLD_MS + FADE_MS) {
                alpha = (int) (255.0 * (1.0 - (t - HOLD_MS) / (double) FADE_MS));
            } else {
                start = -1;
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            graphics.fill(0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(),
                    (alpha << 24) | 0xFFFFFF);
        });
    }
}
