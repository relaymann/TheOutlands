package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** A distant wilderness giant that gives a clear physical warning before its ground strike. */
public final class ColossusEntity extends OutlandsGeoMonster {
    private static final EntityDataAccessor<Boolean> DATA_WINDING_UP =
            SynchedEntityData.defineId(ColossusEntity.class, EntityDataSerializers.BOOLEAN);
    private int stepCooldown;
    private int attackCooldown;
    private int warningTicks;

    public ColossusEntity(EntityType<? extends OutlandsGeoMonster> type, Level level) {
        super(type, level);
        xpReward = 16;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WINDING_UP, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.12D)
                .add(Attributes.FOLLOW_RANGE, 34.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.48D));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 24, true, false,
                player -> player instanceof Player p && p.isAlive() && !p.isCreative() && !p.isSpectator()));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        if (stepCooldown > 0) stepCooldown--;
        if (attackCooldown > 0) attackCooldown--;

        Player prey = getTarget() instanceof Player player ? player : null;
        if (prey == null || !prey.isAlive() || prey.isCreative() || prey.isSpectator()) {
            warningTicks = 0;
            entityData.set(DATA_WINDING_UP, false);
            return;
        }

        double distance = distanceToSqr(prey);
        if (distance > 625.0D) {
            setTarget(null);
            getNavigation().stop();
            warningTicks = 0;
            entityData.set(DATA_WINDING_UP, false);
            return;
        }

        if (warningTicks > 0) {
            getNavigation().stop();
            warningTicks--;
            if (warningTicks == 0) performGroundStrike();
        } else if (distance > 30.25D) {
            getNavigation().moveTo(prey, 0.72D);
        } else {
            getNavigation().stop();
            if (attackCooldown == 0) {
                warningTicks = 28;
                playHorrorAnimation("ground_slam");
                entityData.set(DATA_WINDING_UP, true);
                if (OutlandsConfig.HORROR_AUDIO_ENABLED.get()) {
                    level().playSound(null, blockPosition(), OutlandsSounds.COLOSSUS_WARNING.get(),
                            SoundSource.HOSTILE, 1.35F, 0.62F);
                }
            }
        }

        if (onGround() && getDeltaMovement().horizontalDistanceSqr() > 0.003D && stepCooldown == 0) {
            if (OutlandsConfig.HORROR_AUDIO_ENABLED.get()) {
                playSound(OutlandsSounds.COLOSSUS_STEP.get(), 1.6F, 0.62F + getRandom().nextFloat() * 0.08F);
            }
            stepCooldown = 30;
        }
    }

    private void performGroundStrike() {
        attackCooldown = 110;
        entityData.set(DATA_WINDING_UP, false);
        playHorrorAnimation("attack_colossus");
        if (OutlandsConfig.HORROR_AUDIO_ENABLED.get()) {
            level().playSound(null, blockPosition(), OutlandsSounds.COLOSSUS_STOMP.get(),
                    SoundSource.HOSTILE, 1.8F, 0.72F);
        }
        for (Player player : level().getEntitiesOfClass(Player.class,
                getBoundingBox().inflate(4.8D, 2.0D, 4.8D),
                candidate -> candidate.isAlive() && !candidate.isCreative() && !candidate.isSpectator())) {
            if (player.hurt(damageSources().mobAttack(this), 11.0F)) {
                player.knockback(1.35D, player.getX() - getX(), player.getZ() - getZ());
            }
        }
    }

    public boolean isWindingUp() {
        return entityData.get(DATA_WINDING_UP);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.COLOSSUS_RUMBLE.get() : null;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.COLOSSUS_HURT.get() : null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return getHurtSound(null);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("step_cooldown", stepCooldown);
        tag.putInt("attack_cooldown", attackCooldown);
        tag.putInt("warning_ticks", warningTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        stepCooldown = tag.getInt("step_cooldown");
        attackCooldown = tag.getInt("attack_cooldown");
        warningTicks = tag.getInt("warning_ticks");
        entityData.set(DATA_WINDING_UP, warningTicks > 0);
    }
}