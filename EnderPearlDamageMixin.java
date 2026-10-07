package com.supertotem.mixin;

import com.supertotem.PermadeathDays;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Dias 11 a 69: el dano al teletransportarte con una ender pearl (5.0) se multiplica x10. */
@Mixin(ThrownEnderpearl.class)
public abstract class EnderPearlDamageMixin {
    @ModifyConstant(method = "onHit", constant = @Constant(floatValue = 5.0F))
    private float totem$pearlDamageX10(float original) {
        return PermadeathDays.inRange() ? original * 10.0F : original;
    }
}
