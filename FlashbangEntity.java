package com.supertotem.entity;

import com.supertotem.FlashPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Flashbang: delgado, vida 4.0 (2 corazones), Velocidad III infinita, rango de vision x2 (70 vs 35 del zombie),
 * hitbox de creeper. Explota al instante al tocar a su objetivo y deja la pantalla en blanco.
 */
public class FlashbangEntity extends Monster {
    /** Si los bloques se rompen con la explosion (false = solo hace dano, como una granada de aturdimiento). */
    public static final boolean BREAK_BLOCKS = false;
    /** Potencia de explosion = la de un creeper. El dano x2 se aplica en DamageRules. */
    public static final float EXPLOSION_POWER = 3.0F;
    /** Radio (bloques) en el que los jugadores ven el destello blanco. */
    public static final double FLASH_RADIUS = 24.0;

    private static final EntityDataAccessor<Boolean> ARMS_UP =
            SynchedEntityData.defineId(FlashbangEntity.class, EntityDataSerializers.BOOLEAN);

    public FlashbangEntity(EntityType<? extends FlashbangEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 70.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ARMS_UP, false);
    }

    public boolean isArmsUp() {
        return this.entityData.get(ARMS_UP);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || this.isRemoved()) return;

        // Velocidad III infinita
        if (!this.hasEffect(MobEffects.MOVEMENT_SPEED)) {
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
                    MobEffectInstance.INFINITE_DURATION, 2, false, false));
        }

        LivingEntity target = this.getTarget();
        boolean alive = target != null && target.isAlive();

        // Levanta los brazos al acercarse (8 bloques)
        boolean near = alive && this.distanceToSqr(target) < 64.0;
        if (this.entityData.get(ARMS_UP) != near) this.entityData.set(ARMS_UP, near);

        // Explota al instante al tocar al objetivo
        if (alive && this.getBoundingBox().inflate(0.5).intersects(target.getBoundingBox())) {
            this.explode();
        }
    }

    private void explode() {
        if (this.isRemoved() || !(this.level() instanceof ServerLevel server)) return;

        server.explode(this, this.getX(), this.getY(), this.getZ(), EXPLOSION_POWER,
                BREAK_BLOCKS ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);

        for (ServerPlayer p : PlayerLookup.around(server, this.position(), FLASH_RADIUS)) {
            if (ServerPlayNetworking.canSend(p, FlashPayload.TYPE)) {
                ServerPlayNetworking.send(p, FlashPayload.INSTANCE);
            }
        }
        this.discard();
    }
}
