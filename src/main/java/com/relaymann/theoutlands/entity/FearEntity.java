package com.relaymann.theoutlands.entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
/** Fear's first manifestation: a silent, hovering witness rather than a combat mob. */
public final class FearEntity extends Monster {
 private long temporaryExpiresAt = -1L;
 public void setTemporaryExpiresAt(long gameTime) { temporaryExpiresAt = gameTime; }
 public FearEntity(EntityType<? extends Monster> type, Level level) { super(type, level); xpReward=0; setNoGravity(true); }
 public static AttributeSupplier.Builder createAttributes() {
  return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,100.0D).add(Attributes.MOVEMENT_SPEED,0.0D)
   .add(Attributes.FOLLOW_RANGE,96.0D).add(Attributes.ARMOR,20.0D).add(Attributes.KNOCKBACK_RESISTANCE,1.0D);
 }
 @Override protected void registerGoals() { }
 @Override public void tick() { super.tick(); if (!level().isClientSide && temporaryExpiresAt >= 0L && level().getGameTime() >= temporaryExpiresAt) { discard(); return; } setNoGravity(true); setDeltaMovement(0.0D,0.0D,0.0D); }
 @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); if (temporaryExpiresAt >= 0L) tag.putLong("outlands_temporary_expires_at", temporaryExpiresAt); }
 @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); temporaryExpiresAt = tag.contains("outlands_temporary_expires_at") ? tag.getLong("outlands_temporary_expires_at") : level().getGameTime() + 160L; }
 @Override protected net.minecraft.sounds.SoundEvent getAmbientSound(){return null;}
 @Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source){return null;}
 @Override protected net.minecraft.sounds.SoundEvent getDeathSound(){return null;}
}
