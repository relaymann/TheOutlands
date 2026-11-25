package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Null's dedicated humanoid entity. His renderer gives him an ordinary player silhouette. */
public final class NullEntity extends OutlandsGeoMonster {
    private long temporaryExpiresAt = -1L;
    public void setTemporaryExpiresAt(long gameTime) { temporaryExpiresAt = gameTime; }
    public NullEntity(EntityType<? extends OutlandsGeoMonster> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D).add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ARMOR, 20.0D).add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }
    @Override protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 64.0F, 1.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide && temporaryExpiresAt >= 0L && level().getGameTime() >= temporaryExpiresAt) discard();
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (temporaryExpiresAt >= 0L) tag.putLong("outlands_temporary_expires_at", temporaryExpiresAt);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        temporaryExpiresAt = tag.contains("outlands_temporary_expires_at") ? tag.getLong("outlands_temporary_expires_at") : -1L;
    }
    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.NULL_PRESENCE.get() : null; }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.NULL_PRESENCE.get() : null; }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.NULL_PRESENCE.get() : null; }
}
