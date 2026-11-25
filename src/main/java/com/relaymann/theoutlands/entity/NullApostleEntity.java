package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import java.util.UUID;

/** A featureless subordinate of Null. It senses its assigned player through Null's Blessing. */
public final class NullApostleEntity extends OutlandsGeoMonster {
    private static final String BLESSED_PLAYER_TAG = "NullBlessedPlayer";
    private UUID blessedPlayer;
    private long temporaryExpiresAt = -1L;
    public void setTemporaryExpiresAt(long gameTime) { temporaryExpiresAt = gameTime; }
    public NullApostleEntity(EntityType<? extends OutlandsGeoMonster> type, Level level) {
        super(type, level); setNoGravity(true); xpReward = 0;
        addTag("theoutlands_null_apostle");
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.12D).add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ARMOR, 0.0D).add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0D);
    }
    public void setBlessedPlayer(UUID playerId) { blessedPlayer = playerId; }
    public boolean isBlessedTo(UUID playerId) { return blessedPlayer != null && blessedPlayer.equals(playerId); }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (blessedPlayer != null) tag.putUUID(BLESSED_PLAYER_TAG, blessedPlayer);
        if (temporaryExpiresAt >= 0L) tag.putLong("outlands_temporary_expires_at", temporaryExpiresAt);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        blessedPlayer = tag.hasUUID(BLESSED_PLAYER_TAG) ? tag.getUUID(BLESSED_PLAYER_TAG) : null;
        temporaryExpiresAt = tag.contains("outlands_temporary_expires_at") ? tag.getLong("outlands_temporary_expires_at") : -1L;
    }
    @Override protected void registerGoals() {}
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide && temporaryExpiresAt >= 0L && level().getGameTime() >= temporaryExpiresAt) { discard(); return; }
        setNoGravity(true);
        Player watched = level().getNearestPlayer(this, 40.0D);
        if (watched != null && watched.isAlive() && !watched.isCreative() && !watched.isSpectator()) {
            getLookControl().setLookAt(watched, 18.0F, 18.0F);
            Vec3 offset = watched.position().subtract(position());
            double distance = offset.length();
            Vec3 drift = Vec3.ZERO;
            if (distance > 14.0D && distance < 40.0D) drift = offset.normalize().scale(0.025D);
            else if (distance < 6.0D && distance > 0.001D) drift = offset.normalize().scale(-0.018D);
            setDeltaMovement(drift.x, Math.sin(tickCount * 0.075D) * 0.004D, drift.z);
        } else {
            setDeltaMovement(0.0D, Math.sin(tickCount * 0.075D) * 0.004D, 0.0D);
        }
    }
    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.NULL_APOSTLE_WHISPER.get() : null; }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.NULL_APOSTLE_WHISPER.get() : null; }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.NULL_APOSTLE_DEPARTURE.get() : null; }
}
