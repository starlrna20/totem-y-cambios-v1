package com.supertotem.mixin;

import com.supertotem.PermadeathDays;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Dias 11 a 69: el dano de caida de los jugadores se multiplica x4. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityFallMixin {
    @Inject(method = "calculateFallDamage", at = @At("RETURN"), cancellable = true)
    private void totem$fallDamageX4(float fallDistance, float damageMultiplier,
                                    CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof ServerPlayer && PermadeathDays.inRange()) {
            cir.setReturnValue(cir.getReturnValue() * 4);
        }
    }
}
