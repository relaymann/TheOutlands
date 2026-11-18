package com.relaymann.theoutlands.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import com.relaymann.theoutlands.horror.AlmightyTerritory;

/** Death is a persistent hunter: it stalks, closes distance, and commits only when ready. */
public final class DeathEntity extends Monster {
    public DeathEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        xpReward = 0;
        addTag("theoutlands_death");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 160.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.29D)
                .add(Attributes.ATTACK_DAMAGE, 11.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.2D)
                .add(Attributes.FOLLOW_RANGE, 112.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.65D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.18D, true));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.72D));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 48.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (getTarget() instanceof ServerPlayer player
                && AlmightyTerritory.contains(player.serverLevel(), player.getX(), player.getZ())) {
            setTarget(null);
            getNavigation().stop();
        }
        // Death occasionally stops to study a nearby player instead of running a predictable chase loop.
        if (getTarget() instanceof Player player && distanceToSqr(player) < 22.0D * 22.0D
                && tickCount % 240 < 28) {
            getNavigation().stop();
            getLookControl().setLookAt(player, 24.0F, 24.0F);
        } else if (getTarget() instanceof Player player && distanceToSqr(player) < 18.0D * 18.0D) {
            setSprinting(true);
        } else {
            setSprinting(false);
        }
    }

    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return null; }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return null; }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return null; }
}
