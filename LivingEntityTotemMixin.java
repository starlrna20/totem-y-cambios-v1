package com.supertotem.mixin;

import com.supertotem.TotemNotifier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Engancha el metodo de Minecraft que gasta el totem al salvarte de la muerte.
 * Antes de que se gaste guardamos cual es; si el totem funciona, avisamos en el chat.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityTotemMixin {
    @Unique
    private ItemStack supertotem$candidate = ItemStack.EMPTY;

    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"))
    private void supertotem$before(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        supertotem$candidate = ItemStack.EMPTY;
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer)) return;
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = self.getItemInHand(hand);
            if (stack.get(DataComponents.DEATH_PROTECTION) != null) {
                supertotem$candidate = stack.copy();
                return;
            }
        }
    }

    @Inject(method = "checkTotemDeathProtection", at = @At("RETURN"))
    private void supertotem$after(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        ItemStack used = supertotem$candidate;
        supertotem$candidate = ItemStack.EMPTY;
        if (!cir.getReturnValueZ() || used.isEmpty()) return;
        if ((Object) this instanceof ServerPlayer player) {
            TotemNotifier.notifyUse(player, used);
        }
    }
}
