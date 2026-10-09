package com.supertotem;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Reglas extra por dia de Permadeath. */
public final class ExtraRules {
    /** Probabilidad de tropezar, comprobada 1 vez por segundo mientras corres: 0,5 %. */
    public static final double TRIP_CHANCE = 0.005;
    /** Dano del tropiezo (1.0 = medio corazon). */
    public static final float TRIP_DAMAGE = 1.0F;
    /** Altura a partir de la cual estas "encima del techo" del Nether. */
    public static final double NETHER_ROOF_Y = 128.0;

    private static int tick = 0;

    private ExtraRules() {}

    public static void register() {
        // --- Reglas que se revisan en el tick del servidor ---
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tick++;
            long day = PermadeathDays.day();
            boolean everySecond = tick % 20 == 0;
            boolean everyTwo = tick % 2 == 0;

            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (!p.isAlive() || p.isSpectator()) continue;

                // Dia 10+: fuego eterno (solo se apaga con agua / pocion splash de agua)
                if (day >= 10) keepFireBurning(p);
                // Dia 20+: si estas envuelto en fuego (o en lava), Fire Resistance se elimina
                if (day >= 20 && (p.isOnFire() || p.isInLava())) p.removeEffect(MobEffects.FIRE_RESISTANCE);

                if (everyTwo && !p.isCreative()) {
                    if (day >= 10 && touchesMagma(p)) killThroughTotem(p);
                    else if (day >= 40 && standsOnPressurePlate(p)) killThroughTotem(p);
                }

                if (everySecond) {
                    netherRoofDarkness(p, day);
                    maybeTrip(p);
                }
            }
        });

        // --- Dia 30+: picar un bloque de magma tambien mata ---
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (state.is(Blocks.MAGMA_BLOCK) && player instanceof ServerPlayer sp
                    && PermadeathDays.day() >= 30 && !sp.isCreative() && !sp.isSpectator()) {
                killThroughTotem(sp);
            }
            return true;
        });

        // --- Dia 20+: romper una telarana con espada o tijeras suelta una araña de cueva (no en agua) ---
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (!(level instanceof ServerLevel server) || !state.is(Blocks.COBWEB)) return;
            if (PermadeathDays.day() < 20) return;
            ItemStack tool = player.getMainHandItem();
            if (!(tool.is(ItemTags.SWORDS) || tool.is(Items.SHEARS))) return;
            if (player.isInWater() || waterNear(server, pos)) return;

            CaveSpider spider = EntityType.CAVE_SPIDER.create(server, EntitySpawnReason.TRIGGERED);
            if (spider != null) {
                spider.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, server.random.nextFloat() * 360.0F, 0.0F);
                server.addFreshEntity(spider);
            }
        });

        // --- Dia 40+: usar puertas, trampillas, puertas de valla, palancas y botones mata (antes gasta totem) ---
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (level.isClientSide || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp) || sp.isCreative() || sp.isSpectator()) return InteractionResult.PASS;
            if (PermadeathDays.day() < 40) return InteractionResult.PASS;

            // Igual que el juego: agachado con algo en las manos NO interactua con el bloque
            boolean hasItem = !sp.getMainHandItem().isEmpty() || !sp.getOffhandItem().isEmpty();
            if (sp.isSecondaryUseActive() && hasItem) return InteractionResult.PASS;

            BlockState state = level.getBlockState(hitResult.getBlockPos());
            if (state.is(BlockTags.DOORS) || state.is(BlockTags.TRAPDOORS) || state.is(BlockTags.FENCE_GATES)
                    || state.is(BlockTags.BUTTONS) || state.is(Blocks.LEVER)) {
                killThroughTotem(sp);
            }
            return InteractionResult.PASS;
        });

        // --- Cuando un jugador recibe dano de un ser vivo concreto ---
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (blocked || !(entity instanceof ServerPlayer p)) return;
            long day = PermadeathDays.day();

            // Dia 10+: un phantom que te golpea te revuelve el inventario
            if (day >= 10 && source.getEntity() instanceof Phantom) shuffleInventory(p);

            // Dia 20+: cualquier arana que te golpea deja una telarana (no en agua)
            if (day >= 20 && source.getEntity() instanceof Spider) placeCobweb(p);
        });
    }

    // ---------- Fuego eterno ----------
    private static void keepFireBurning(ServerPlayer p) {
        int f = p.getRemainingFireTicks();
        if (f > 0 && f <= 5) p.setRemainingFireTicks(205);
    }

    // ---------- Magma: tocarlo (arriba, abajo o a los lados) mata, pasando antes por el totem ----------
    private static boolean touchesMagma(ServerPlayer p) {
        AABB box = p.getBoundingBox().inflate(0.002);
        ServerLevel level = p.serverLevel();
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockState(pos).is(Blocks.MAGMA_BLOCK)) return true;
        }
        return false;
    }

    // ---------- Placas de presion (dia 40+): pisar cualquier placa mata ----------
    private static boolean standsOnPressurePlate(ServerPlayer p) {
        return p.serverLevel().getBlockState(p.blockPosition()).is(BlockTags.PRESSURE_PLATES);
    }

    /** Dano enorme, pero normal (no se salta el totem): si tienes totem te salva, si no, mueres. */
    private static void killThroughTotem(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        DamageRules.suppressed = true;
        try {
            p.hurtServer(level, level.damageSources().hotFloor(), 1000.0F);
        } finally {
            DamageRules.suppressed = false;
        }
    }

    // ---------- Telaranas ----------
    private static void placeCobweb(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        BlockPos pos = p.blockPosition();
        if (p.isInWater() || level.getFluidState(pos).is(FluidTags.WATER)) return;   // excepcion: agua
        BlockState current = level.getBlockState(pos);
        if (current.isAir() || current.canBeReplaced()) {
            level.setBlockAndUpdate(pos, Blocks.COBWEB.defaultBlockState());
        }
    }

    private static boolean waterNear(ServerLevel level, BlockPos pos) {
        if (level.getFluidState(pos).is(FluidTags.WATER)) return true;
        for (Direction d : Direction.values()) {
            if (level.getFluidState(pos.relative(d)).is(FluidTags.WATER)) return true;
        }
        return false;
    }

    // ---------- Techo del Nether (dia 20+): Darkness mientras estes encima ----------
    private static void netherRoofDarkness(ServerPlayer p, long day) {
        boolean onRoof = day >= 20
                && p.level().dimension() == Level.NETHER
                && p.getY() >= NETHER_ROOF_Y;
        MobEffectInstance cur = p.getEffect(MobEffects.DARKNESS);
        if (onRoof) {
            // ambient = true sirve de marca para saber que este efecto lo puso el mod
            p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 260, 0, true, false, false));
        } else if (cur != null && cur.isAmbient()) {
            p.removeEffect(MobEffects.DARKNESS);
        }
    }

    // ---------- Tropiezo: 0,5 % por segundo corriendo ----------
    private static void maybeTrip(ServerPlayer p) {
        if (!p.isSprinting() || p.isCreative()) return;
        if (p.getRandom().nextDouble() >= TRIP_CHANCE) return;

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = p.getItemInHand(hand);
            if (!stack.isEmpty()) {
                p.drop(stack.copy(), false);
                p.setItemInHand(hand, ItemStack.EMPTY);
            }
        }
        ServerLevel level = p.serverLevel();
        DamageRules.suppressed = true;
        try {
            p.hurtServer(level, level.damageSources().generic(), TRIP_DAMAGE);
        } finally {
            DamageRules.suppressed = false;
        }
        p.displayClientMessage(Component.literal("¡Has tropezado!"), true);
    }

    // ---------- Phantom: revolver inventario (36 casillas: hotbar + mochila) ----------
    private static void shuffleInventory(ServerPlayer p) {
        Inventory inv = p.getInventory();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 36; i++) items.add(inv.getItem(i));
        Collections.shuffle(items);
        for (int i = 0; i < 36; i++) inv.setItem(i, items.get(i));
        p.inventoryMenu.broadcastChanges();
        if (p.containerMenu != p.inventoryMenu) p.containerMenu.broadcastChanges();
    }
}
