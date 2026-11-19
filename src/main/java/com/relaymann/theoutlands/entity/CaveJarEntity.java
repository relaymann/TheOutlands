package com.relaymann.theoutlands.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Cave.jar is a close-range cave predator: it commits to a hunt instead of staging harmless appearances. */
public final class CaveJarEntity extends Monster {
    public CaveJarEntity(EntityType<? extends Monster> type, Level level) { super(type, level); xpReward = 12; }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 58.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D).add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D).add(Attributes.ATTACK_KNOCKBACK, 1.4D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.45D);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new MeleeAttackGoal(this, 1.38D, true));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 18.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide && getTarget() instanceof Player player) {
            double distance = distanceToSqr(player);
            if (distance < 16.0D && hasLineOfSight(player)) setSprinting(true);
            else if (distance > 36.0D) setSprinting(false);
        }
    }
}
