package com.relaymann.theoutlands.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * A silent, featureless subordinate of Null. It has no eyes: Null's blessing
 * lets it sense the one player it has been sent to observe.
 */
public final class NullApostleEntity extends Monster {
    private static final String BLESSED_PLAYER_TAG = "NullBlessedPlayer";
    private UUID blessedPlayer;

    public NullApostleEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setNoGravity(true);
        xpReward = 0;
        addTag("theoutlands_null_apostle");
    }

    public static AttributeSupplier.Builder createAttributes() {
        // Deliberately far below Null: apostles are observers, not boss-tier enemies.
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.12D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0D);
    }

    /** Binds this apostle to the player whose presence Null has blessed it to sense. */
    public void setBlessedPlayer(UUID playerId) {
        this.blessedPlayer = playerId;
    }

    /** The blessing is its supernatural perception; it does not require eyes or line of sight. */
    public boolean isBlessedTo(UUID playerId) {
        return blessedPlayer != null && blessedPlayer.equals(playerId);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (blessedPlayer != null) {
            tag.putUUID(BLESSED_PLAYER_TAG, blessedPlayer);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        blessedPlayer = tag.hasUUID(BLESSED_PLAYER_TAG) ? tag.getUUID(BLESSED_PLAYER_TAG) : null;
    }

    @Override
    protected void registerGoals() {
        // Their movement and observation are directed by NullApostleManager.
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
        setDeltaMovement(0.0D, 0.0D, 0.0D);
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() { return null; }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return null;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() { return null; }
}
