package com.supertotem.mixin;

import com.supertotem.client.TotemColors;
import net.minecraft.client.particle.TotemParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Al crearse una particula de totem, si hay un color pendiente cerca, se pinta de ese color. */
@Mixin(TotemParticle.class)
public abstract class TotemParticleMixin {
    @Shadow protected float rCol;
    @Shadow protected float gCol;
    @Shadow protected float bCol;
    @Shadow protected double x;
    @Shadow protected double y;
    @Shadow protected double z;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void totem$recolor(CallbackInfo ci) {
        int rgb = TotemColors.lookup(this.x, this.y, this.z);
        if (rgb == -1) return;
        float shade = 0.75F + 0.25F * (float) Math.random();   // pequena variacion de brillo
        this.rCol = ((rgb >> 16) & 0xFF) / 255.0F * shade;
        this.gCol = ((rgb >> 8) & 0xFF) / 255.0F * shade;
        this.bCol = (rgb & 0xFF) / 255.0F * shade;
    }
}
