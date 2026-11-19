package com.relaymann.theoutlands.entity;

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

/** The Stalker pressures a target over long distances and accelerates when it closes the gap. */
public final class StalkerEntity extends Monster {
    public StalkerEntity(EntityType<? extends Monster> type, Level level) { super(type, level); xpReward = 10; }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 44.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.31D).add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.5D).add(Attributes.ARMOR, 5.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new MeleeAttackGoal(this, 1.22D, true));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                player -> player instanceof Player p && p.isAlive() && !p.isCreative() && !p.isSpectator()));
    }
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide && getTarget() instanceof Player player) {
            double distance = distanceToSqr(player);
            setSprinting(distance > 25.0D && distance < 1600.0D);
            if (distance > 4096.0D) setTarget(null);
        }
    }
}
