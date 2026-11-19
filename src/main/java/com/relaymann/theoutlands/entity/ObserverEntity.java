package com.relaymann.theoutlands.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import java.util.EnumSet;

/** The Observer advances by pathfinding only while its target cannot see it. No turning-trigger teleportation. */
public final class ObserverEntity extends Monster {
    public ObserverEntity(EntityType<? extends Monster> type, Level level) { super(type, level); xpReward = 8; }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 34.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.29D).add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D).add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new AdvanceUnseenGoal());
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 12, true, false,
                player -> player.isAlive() && !player.isCreative() && !player.isSpectator()));
    }
    private final class AdvanceUnseenGoal extends Goal {
        private int repathDelay;
        private AdvanceUnseenGoal() { setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }
        @Override public boolean canUse() {
            return getTarget() instanceof Player player && player.isAlive()
                    && distanceToSqr(player) < 900.0D && !player.hasLineOfSight(ObserverEntity.this);
        }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void start() { repathDelay = 0; }
        @Override public void tick() {
            Player player = (Player) getTarget();
            getLookControl().setLookAt(player, 30.0F, 30.0F);
            if (player.hasLineOfSight(ObserverEntity.this)) { getNavigation().stop(); return; }
            if (--repathDelay <= 0) {
                repathDelay = 8 + getRandom().nextInt(8);
                getNavigation().moveTo(player, 1.12D);
            }
            if (distanceToSqr(player) < 3.2D) doHurtTarget(player);
        }
        @Override public void stop() { getNavigation().stop(); }
    }
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide && getTarget() instanceof Player player && distanceToSqr(player) > 900.0D)
            setTarget(null);
    }
}
