package com.relaymann.theoutlands.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import com.relaymann.theoutlands.OutlandsConfig;

/** A volatile predator that escalates from twitching vigilance to sustained, telegraphed charges. */
public final class AgitatedOneEntity extends Monster {
    private static final EntityDataAccessor<Integer> DATA_AGITATION = SynchedEntityData.defineId(AgitatedOneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CHARGING = SynchedEntityData.defineId(AgitatedOneEntity.class, EntityDataSerializers.BOOLEAN);
    private int agitation;
    private int chargeCooldown;
    private int attackWindup;
    private int chargeTicks;
    private boolean charging;
    private boolean chargeHit;
    public AgitatedOneEntity(EntityType<? extends Monster> type, Level level) { super(type, level); xpReward = 9; }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D).add(Attributes.FOLLOW_RANGE, 38.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D).add(Attributes.ARMOR, 3.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_AGITATION, 0);
        builder.define(DATA_CHARGING, false);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.12D, true));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 12, true, false,
                p -> p instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator()));
    }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (chargeCooldown > 0) chargeCooldown--;
        if (getTarget() instanceof Player p) {
            double d = distanceToSqr(p);
            if (d < 256.0D) agitation = Math.min(100, agitation + (d < 64.0D ? 2 : 1));
            else agitation = Math.max(0, agitation - 2);
            if (agitation >= 35 && attackWindup == 0 && chargeCooldown == 0 && d > 16.0D && d < 196.0D
                    && hasLineOfSight(p)) {
                attackWindup = 16;
                charging = false;
                chargeHit = false;
                if (OutlandsConfig.HORROR_AUDIO_ENABLED.get()) playSound(OutlandsSounds.AGITATED_WARNING.get(), 1.0F, 0.75F + getRandom().nextFloat() * 0.12F);
            }
            if (attackWindup > 0) { getNavigation().stop(); setDeltaMovement(0, getDeltaMovement().y, 0); }
            if (attackWindup > 0 && --attackWindup == 0) {
                if (p.isAlive() && hasLineOfSight(p) && d > 16.0D && d < 196.0D) {
                    net.minecraft.world.phys.Vec3 dir = p.position().add(p.getDeltaMovement().scale(5.0D)).subtract(position()).normalize();
                    setDeltaMovement(getDeltaMovement().add(dir.x * 0.92D, 0.32D, dir.z * 0.92D));
                    hasImpulse = true; charging = true; chargeTicks = 14; chargeCooldown = 100; agitation = Math.max(20, agitation - 25);
                    if (OutlandsConfig.HORROR_AUDIO_ENABLED.get()) playSound(OutlandsSounds.AGITATED_CHARGE.get(), 1.0F, 0.9F + getRandom().nextFloat() * 0.1F);
                } else {
                    charging = false;
                    chargeTicks = 0;
                    chargeCooldown = 20;
                }
            }
            if (charging) {
                // Keep the melee goal from replacing the charge's launch velocity.
                getNavigation().stop();
                if (!chargeHit && d < 4.0D) { doHurtTarget(p); chargeHit = true; }
                if (--chargeTicks <= 0) charging = false;
            }
            entityData.set(DATA_AGITATION, agitation);
            entityData.set(DATA_CHARGING, charging);
            setSprinting(agitation > 45 || charging);
        } else {
            agitation = Math.max(0, agitation - 1); charging = false; chargeTicks = 0; attackWindup = 0;
            entityData.set(DATA_AGITATION, agitation);
            entityData.set(DATA_CHARGING, false);
        }
    }
    public int getAgitation() { return entityData.get(DATA_AGITATION); }
    public boolean isCharging() { return entityData.get(DATA_CHARGING); }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putInt("agitation", agitation); tag.putInt("charge_cd", chargeCooldown); tag.putInt("windup", attackWindup); tag.putInt("charge_ticks", chargeTicks); tag.putBoolean("charging", charging); tag.putBoolean("charge_hit", chargeHit); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); agitation=tag.getInt("agitation"); chargeCooldown=tag.getInt("charge_cd"); attackWindup=tag.getInt("windup"); chargeTicks=tag.getInt("charge_ticks"); charging=tag.getBoolean("charging"); chargeHit=tag.getBoolean("charge_hit"); entityData.set(DATA_AGITATION, agitation); entityData.set(DATA_CHARGING, charging); }
    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.AGITATED_WARNING.get() : null; }
    @Override protected SoundEvent getDeathSound() { return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.AGITATED_CHARGE.get() : null; }
}
