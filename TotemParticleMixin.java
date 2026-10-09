package com.supertotem.mixin;

import com.supertotem.client.TotemColors;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Al crearse una particula de totem, si hay un color pendiente cerca, se pinta de ese color. */
@Mixin(TotemParticle.class)
public abstract class TotemParticleMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void totem$recolor(CallbackInfo ci) {
        // En ejecucion este objeto ES la particula, asi que se usan sus metodos publicos (sin @Shadow).
        Particle self = (Particle) (Object) this;
        AABB box = self.getBoundingBox();
        int rgb = TotemColors.lookup((box.minX + box.maxX) / 2.0, box.minY, (box.minZ + box.maxZ) / 2.0);
        if (rgb == -1) return;

        float shade = 0.75F + 0.25F * (float) Math.random();   // pequena variacion de brillo
        self.setColor(((rgb >> 16) & 0xFF) / 255.0F * shade,
                      ((rgb >> 8) & 0xFF) / 255.0F * shade,
                      (rgb & 0xFF) / 255.0F * shade);
    }
}
