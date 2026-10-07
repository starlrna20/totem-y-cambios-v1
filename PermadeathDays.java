package com.supertotem;

import java.lang.reflect.Method;

/**
 * Lee el dia actual del mod Permadeath (DateManager.getCurrentRealDay) por reflexion,
 * asi este mod no depende de el para compilar. Si Permadeath no esta instalado, devuelve -1
 * y ninguna regla por dias se activa. El valor se guarda 1 segundo para no gastar rendimiento.
 */
public final class PermadeathDays {
    private static Method dayMethod;
    private static boolean failed = false;
    private static long cached = -1;
    private static long cachedAt = 0;

    private PermadeathDays() {}

    public static long day() {
        long now = System.currentTimeMillis();
        if (now - cachedAt > 1000) {
            cached = read();
            cachedAt = now;
        }
        return cached;
    }

    /** Mismo rango que usa Permadeath para el spawn x2: dias 11 a 69. */
    public static boolean inRange() {
        long d = day();
        return d > 10 && d < 70;
    }

    private static long read() {
        if (failed) return -1;
        try {
            if (dayMethod == null) {
                Class<?> c = Class.forName("com.serthekiller.permadeath.utils.DateManager");
                dayMethod = c.getMethod("getCurrentRealDay");
            }
            return (Long) dayMethod.invoke(null);
        } catch (Throwable t) {
            failed = true;
            SuperTotemMod.LOGGER.warn("No se pudo leer el dia de Permadeath; las reglas por dias quedan desactivadas", t);
            return -1;
        }
    }
}
