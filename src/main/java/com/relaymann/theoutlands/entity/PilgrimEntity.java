package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** A wary wanderer that watches from a distance, retreats when approached, and only attacks when provoked. */
public final class PilgrimEntity extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_WATCHING =
            SynchedEntityData.defineId(PilgrimEntity.class, EntityDataSerializers.BOOLEAN);
    private int behaviorCooldown;
    private int stepCooldown;
    private int watchTicks;

    public PilgrimEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 2;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WATCHING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.245D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.88D, 0.001F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        if (behaviorCooldown > 0) behaviorCooldown--;
        if (stepCooldown > 0) stepCooldown--;
        if (watchTicks > 0) watchTicks--;
        entityData.set(DATA_WATCHING, watchTicks > 0);

        if (onGround() && getDeltaMovement().horizontalDistanceSqr() > 0.002D && stepCooldown == 0) {
            if (OutlandsConfig.HORROR_AUDIO_ENABLED.get()) {
                playSound(OutlandsSounds.PILGRIM_STEP.get(), 0.28F, 0.88F + getRandom().nextFloat() * 0.18F);
            }
            stepCooldown = 19;
        }

        if (getTarget() != null) {
            setSprinting(true);
            return;
        }
        setSprinting(false);

        Player player = level().getNearestPlayer(this, 24.0D, this::isValidObserver);
        if (player == null) {
            if (watchTicks == 0 && getNavigation().isDone() && getRandom().nextInt(120) == 0) {
                getNavigation().moveTo(getX() + (getRandom().nextDouble() - 0.5D) * 12.0D,
                        getY(), getZ() + (getRandom().nextDouble() - 0.5D) * 12.0D, 0.78D);
            }
            return;
        }

        double distance = distanceToSqr(player);
        if (distance < 36.0D && behaviorCooldown == 0) {
            // It never teleports: it chooses a reachable retreat point and moves away.
            double dx = getX() - player.getX();
            double dz = getZ() - player.getZ();
            double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
            double side = (getRandom().nextDouble() - 0.5D) * 5.0D;
            double retreatX = getX() + dx / length * (9.0D + getRandom().nextDouble() * 5.0D) - dz / length * side;
            double retreatZ = getZ() + dz / length * (9.0D + getRandom().nextDouble() * 5.0D) + dx / length * side;
            getNavigation().moveTo(retreatX, getY(), retreatZ, 1.08D);
            behaviorCooldown = 70 + getRandom().nextInt(50);
            watchTicks = 0;
        } else if (distance < 225.0D) {
            getNavigation().stop();
            getLookControl().setLookAt(player, 25.0F, 25.0F);
            if (watchTicks == 0) watchTicks = 50 + getRandom().nextInt(70);
        }
    }

    private boolean isValidObserver(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    public boolean isWatching() {
        return entityData.get(DATA_WATCHING);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.PILGRIM_BREATH.get() : null;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.PILGRIM_HURT.get() : null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return getHurtSound(null);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("behavior_cooldown", behaviorCooldown);
        tag.putInt("step_cooldown", stepCooldown);
        tag.putInt("watch_ticks", watchTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        behaviorCooldown = tag.getInt("behavior_cooldown");
        stepCooldown = tag.getInt("step_cooldown");
        watchTicks = tag.getInt("watch_ticks");
        entityData.set(DATA_WATCHING, watchTicks > 0);
    }
}