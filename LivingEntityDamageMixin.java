package com.supertotem.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.supertotem.DamageRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Multiplica el dano que recibe un jugador segun la regla del dia (fuego x4 dia 20+, ambiente x4 dia 30+). */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {
    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
    private float totem$scaleDamage(float amount, @Local(argsOnly = true) DamageSource source) {
        if (!((Object) this instanceof ServerPlayer)) return amount;
        return amount * DamageRules.multiplier(source);
    }
}
