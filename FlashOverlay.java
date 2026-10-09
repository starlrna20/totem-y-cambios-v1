package com.supertotem.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;

/** Pantalla blanca: 2 segundos completa y luego se difumina durante 3 segundos. */
public final class FlashOverlay {
    private static final long HOLD_MS = 2000;
    private static final long FADE_MS = 3000;
    private static long start = -1;

    private FlashOverlay() {}

    public static void trigger() {
        start = System.currentTimeMillis();
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
