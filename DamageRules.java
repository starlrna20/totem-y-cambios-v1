package com.supertotem;

import com.supertotem.entity.FlashbangEntity;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/** Multiplicadores de dano que recibe un jugador segun el dia de Permadeath. */
public final class DamageRules {
    /** Se pone en true mientras el propio mod hace dano (magma, tropiezo) para que no se multiplique. */
    public static boolean suppressed = false;

    private DamageRules() {}

    public static float multiplier(DamageSource src) {
        if (suppressed) return 1.0F;
        // La explosion de un Flashbang hace el doble de dano que un creeper
        if (src.getDirectEntity() instanceof FlashbangEntity) return 2.0F;

        long day = PermadeathDays.day();

        // Dia 20+: cualquier dano de fuego (fuego, lava, blaze, bolas de fuego...) x4
        if (src.is(DamageTypeTags.IS_FIRE)) {
            return day >= 20 ? 4.0F : 1.0F;
        }

        // Dia 30+: dano del ambiente (nada lo causa un ser vivo) x4.
        // La caida y el fuego tienen su propia regla, asi que no se acumulan.
        if (day >= 30
                && !(src.getEntity() instanceof LivingEntity)
                && !(src.getDirectEntity() instanceof LivingEntity)
                && !src.is(DamageTypeTags.IS_FALL)
                && !src.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return 4.0F;
        }
        return 1.0F;
    }
}
