package com.relaymann.theoutlands.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import com.relaymann.theoutlands.OutlandsConfig;
import java.util.EnumSet;

public final class StillbornStatueEntity extends OutlandsGeoMonster {
    private static final EntityDataAccessor<Boolean> DATA_STALKING = SynchedEntityData.defineId(StillbornStatueEntity.class, EntityDataSerializers.BOOLEAN);
    private int stillnessTicks;
    private int strikeCooldown;
    private boolean stalking;
    public StillbornStatueEntity(EntityType<? extends OutlandsGeoMonster> type, Level level) { super(type, level); xpReward = 10; }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 58.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D).add(Attributes.FOLLOW_RANGE, 42.0D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D).add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STALKING, false);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new WatcherAdvanceGoal());
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                p -> p instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator()));
    }
    private final class WatcherAdvanceGoal extends Goal {
        private int repath;
        WatcherAdvanceGoal() { setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }
        @Override public boolean canUse() {
            return getTarget() instanceof Player p && p.isAlive() && distanceToSqr(p) < 900.0D;
        }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void tick() {
            Player p = (Player)getTarget();
            getLookControl().setLookAt(p, 45.0F, 45.0F);
            if (isWatchedByAnyPlayer()) {
                stalking = false; entityData.set(DATA_STALKING, false); stillnessTicks++; getNavigation().stop(); setDeltaMovement(0, getDeltaMovement().y, 0);

                return;
            }
            stillnessTicks = 0; stalking = true; entityData.set(DATA_STALKING, true);
            if (--repath <= 0) { repath = 6 + getRandom().nextInt(7); getNavigation().moveTo(p, 1.22D); }
            if (strikeCooldown > 0) strikeCooldown--;
            if (distanceToSqr(p) < 3.4D && hasLineOfSight(p) && strikeCooldown == 0) {
                doHurtTarget(p); strikeCooldown = 28;
                if (OutlandsConfig.HORROR_AUDIO_ENABLED.get()) playSound(OutlandsSounds.STILLBORN_STRIKE.get(), 0.9F, 0.8F + getRandom().nextFloat() * 0.15F);
            }
        }
        @Override public void stop() { stalking = false; entityData.set(DATA_STALKING, false); getNavigation().stop(); }
    }
    private boolean isWatchedByAnyPlayer() {
        return level().players().stream()
                .anyMatch(player -> player.isAlive() && !player.isCreative() && !player.isSpectator() && player.hasLineOfSight(this));
    }
    public boolean isStalking() { return entityData.get(DATA_STALKING); }
    public float getStiffness(float partialTick) { return stalking ? 0.0F : 1.0F; }
    @Override public void tick() { super.tick(); if (!level().isClientSide && getTarget() instanceof Player p && distanceToSqr(p) > 1600.0D) setTarget(null); }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putInt("stillness", stillnessTicks); tag.putInt("strike_cd", strikeCooldown); tag.putBoolean("stalking", stalking); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); stillnessTicks=tag.getInt("stillness"); strikeCooldown=tag.getInt("strike_cd"); stalking=tag.getBoolean("stalking"); entityData.set(DATA_STALKING, stalking); }
    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.STILLBORN_STONE.get() : null; }
    @Override protected SoundEvent getDeathSound() { return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.STILLBORN_STONE.get() : null; }
}
