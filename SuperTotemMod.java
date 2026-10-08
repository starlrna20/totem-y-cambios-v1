package com.supertotem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.DeathProtection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuperTotemMod implements ModInitializer {
    public static final String MOD_ID = "totem_astraeus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Probabilidad de drop al matar un evoker de una raid: 7 %. */
    public static final double DROP_CHANCE = 0.07;

    /** MANO SECUNDARIA: +4.0 de vida maxima (2 corazones) y Velocidad II infinita. */
    public static final double OFFHAND_EXTRA_HEALTH = 4.0;
    public static final int SPEED_AMPLIFIER = 1;

    /** MANO PRINCIPAL: vida maxima limitada a 10.0 (5 corazones) e Invisibilidad infinita. */
    public static final double MAINHAND_MAX_HEALTH = 10.0;

    private static final ResourceLocation BONUS_ID =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "offhand_hearts");
    private static final ResourceLocation LIMIT_ID =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "mainhand_limit");

    public static Item SUPREME_TOTEM;
    private int tick = 0;

    @Override
    public void onInitialize() {
        // --- Objeto: se comporta como el totem normal (death_protection). Sin brillo de encantamiento. ---
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "totem_astraeus"));
        SUPREME_TOTEM = Registry.register(BuiltInRegistries.ITEM, key,
                new Item(new Item.Properties()
                        .setId(key)
                        .stacksTo(1)
                        .rarity(Rarity.EPIC)
                        .component(DataComponents.DEATH_PROTECTION, DeathProtection.TOTEM_OF_UNDYING)));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT)
                .register(entries -> entries.accept(SUPREME_TOTEM));

        // --- Drop: 7 % al matar (un jugador) un evoker mientras hay una raid activa cerca ---
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof Evoker evoker)) return;
            if (!(evoker.level() instanceof ServerLevel level)) return;
            boolean byPlayer = source.getEntity() instanceof Player;
            boolean inRaid = level.getRaids().getNearbyRaid(evoker.blockPosition(), 9216) != null;
            LOGGER.info("Evoker muerto (por jugador: {}, raid cerca: {})", byPlayer, inRaid);
            if (!byPlayer || !inRaid) return;
            if (level.random.nextDouble() >= DROP_CHANCE) return;
            level.addFreshEntity(new ItemEntity(level,
                    evoker.getX(), evoker.getY(), evoker.getZ(), new ItemStack(SUPREME_TOTEM)));
            LOGGER.info("El evoker solto un Totem Astraeus");
        });

        // --- Mensaje servidor -> cliente para el color de las particulas del totem ---
        PayloadTypeRegistry.playS2C().register(TotemColorPayload.TYPE, TotemColorPayload.CODEC);

        // --- Reglas extra por dia de Permadeath (fuego, magma, techo del nether, phantoms, tropiezos) ---
        ExtraRules.register();

        // --- Efectos segun la mano: se revisan 1 vez por segundo (muy ligero) ---
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++tick % 20 != 0) return;
            for (ServerPlayer p : server.getPlayerList().getPlayers()) updateBuffs(p);
        });
    }

    private static void updateBuffs(ServerPlayer p) {
        boolean main = p.getMainHandItem().is(SUPREME_TOTEM);
        boolean off = p.getOffhandItem().is(SUPREME_TOTEM);

        updateHealth(p, main, off);
        updateEffect(p, MobEffects.MOVEMENT_SPEED, SPEED_AMPLIFIER, off);
        updateEffect(p, MobEffects.INVISIBILITY, 0, main);
    }

    private static void updateHealth(ServerPlayer p, boolean main, boolean off) {
        AttributeInstance hp = p.getAttribute(Attributes.MAX_HEALTH);
        if (hp == null) return;

        // Mano secundaria: +4.0 de vida maxima
        boolean hasBonus = hp.hasModifier(BONUS_ID);
        if (off && !hasBonus) {
            hp.addTransientModifier(new AttributeModifier(
                    BONUS_ID, OFFHAND_EXTRA_HEALTH, AttributeModifier.Operation.ADD_VALUE));
        } else if (!off && hasBonus) {
            hp.removeModifier(BONUS_ID);
        }

        // Mano principal: vida maxima limitada a 10.0 (se calcula para quedar exactamente en 10)
        if (main) {
            boolean ok = hp.hasModifier(LIMIT_ID) && Math.abs(hp.getValue() - MAINHAND_MAX_HEALTH) < 0.01;
            if (!ok) {
                hp.removeModifier(LIMIT_ID);
                double amount = MAINHAND_MAX_HEALTH - hp.getValue();
                if (amount < 0) {
                    hp.addTransientModifier(new AttributeModifier(
                            LIMIT_ID, amount, AttributeModifier.Operation.ADD_VALUE));
                }
            }
        } else if (hp.hasModifier(LIMIT_ID)) {
            hp.removeModifier(LIMIT_ID);
        }

        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
    }

    /** Pone el efecto infinito mientras `want` sea true y lo quita despues (solo si lo puso este mod). */
    private static void updateEffect(ServerPlayer p, Holder<MobEffect> effect, int amplifier, boolean want) {
        MobEffectInstance cur = p.getEffect(effect);
        if (want) {
            if (cur == null || cur.getAmplifier() < amplifier) {
                p.addEffect(new MobEffectInstance(effect,
                        MobEffectInstance.INFINITE_DURATION, amplifier, true, false, true));
            }
        } else if (cur != null && cur.isInfiniteDuration() && cur.isAmbient()
                && cur.getAmplifier() == amplifier) {
            p.removeEffect(effect);
        }
    }
}
