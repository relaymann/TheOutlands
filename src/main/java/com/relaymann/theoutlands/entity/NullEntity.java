package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Null's dedicated humanoid entity. His renderer gives him an ordinary player silhouette. */
public final class NullEntity extends Monster {
    public NullEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
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
    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return OutlandsSounds.NULL_PRESENCE.get(); }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return OutlandsSounds.NULL_PRESENCE.get(); }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return OutlandsSounds.NULL_PRESENCE.get(); }
}
