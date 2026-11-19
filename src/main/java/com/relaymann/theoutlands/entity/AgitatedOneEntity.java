package com.relaymann.theoutlands.entity;

import net.minecraft.nbt.CompoundTag;
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

/** A volatile predator that escalates from twitching vigilance to sustained, telegraphed charges. */
public final class AgitatedOneEntity extends Monster {
    private int agitation;
    private int chargeCooldown;
    private int attackWindup;
    private boolean charging;
    public AgitatedOneEntity(EntityType<? extends Monster> type, Level level) { super(type, level); xpReward = 9; }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D).add(Attributes.FOLLOW_RANGE, 38.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D).add(Attributes.ARMOR, 3.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D);
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
            if (agitation >= 35 && attackWindup == 0 && chargeCooldown == 0 && d > 16.0D && d < 196.0D) {
                attackWindup = 16;
                charging = false;
                playSound(OutlandsSounds.AGITATED_WARNING.get(), 1.0F, 0.75F + getRandom().nextFloat() * 0.12F);
            }
            if (attackWindup > 0 && --attackWindup == 0) {
                net.minecraft.world.phys.Vec3 dir = p.position().subtract(position()).normalize();
                setDeltaMovement(getDeltaMovement().add(dir.x * 0.92D, 0.32D, dir.z * 0.92D));
                hasImpulse = true; charging = true; chargeCooldown = 100; agitation = Math.max(20, agitation - 25);
                playSound(OutlandsSounds.AGITATED_CHARGE.get(), 1.0F, 0.9F + getRandom().nextFloat() * 0.1F);
            }
            setSprinting(agitation > 45 || charging);
            if (charging && onGround() && chargeCooldown < 78) charging = false;
        } else {
            agitation = Math.max(0, agitation - 1); charging = false; attackWindup = 0;
        }
    }
    public int getAgitation() { return agitation; }
    public boolean isCharging() { return charging; }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putInt("agitation", agitation); tag.putInt("charge_cd", chargeCooldown); tag.putInt("windup", attackWindup); tag.putBoolean("charging", charging); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); agitation=tag.getInt("agitation"); chargeCooldown=tag.getInt("charge_cd"); attackWindup=tag.getInt("windup"); charging=tag.getBoolean("charging"); }
    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return OutlandsSounds.AGITATED_WARNING.get(); }
    @Override protected SoundEvent getDeathSound() { return OutlandsSounds.AGITATED_CHARGE.get(); }
}
