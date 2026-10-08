package com.supertotem.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/** Lado cliente: recuerda 4 segundos de que color son las particulas del totem de cada jugador. */
public final class TotemColors {
    private record Entry(int entityId, int rgb, long expiresAt) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private TotemColors() {}

    public static void add(int entityId, int rgb) {
        long now = System.currentTimeMillis();
        ENTRIES.removeIf(e -> e.expiresAt() < now || e.entityId() == entityId);
        ENTRIES.add(new Entry(entityId, rgb, now + 4000));
    }

    /** Devuelve el color (0xRRGGBB) del totem cuyo jugador esta cerca de este punto, o -1 si no hay. */
    public static int lookup(double x, double y, double z) {
        if (ENTRIES.isEmpty()) return -1;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return -1;
        long now = System.currentTimeMillis();
        for (Entry e : ENTRIES) {
            if (e.expiresAt() < now) continue;
            Entity entity = level.getEntity(e.entityId());
            if (entity == null) continue;
            double dx = entity.getX() - x, dy = entity.getY() - y, dz = entity.getZ() - z;
            if (dx * dx + dy * dy + dz * dz < 16.0) return e.rgb();
        }
        return -1;
    }
}
