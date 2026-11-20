package com.relaymann.theoutlands.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import com.relaymann.theoutlands.OutlandsConfig;
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
    private static final EntityDataAccessor<Boolean> DATA_ADVANCING = SynchedEntityData.defineId(ObserverEntity.class, EntityDataSerializers.BOOLEAN);
    public ObserverEntity(EntityType<? extends Monster> type, Level level) { super(type, level); xpReward = 8; }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 34.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.29D).add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D).add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ADVANCING, false);
    }
    public boolean isAdvancing() { return entityData.get(DATA_ADVANCING); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new AdvanceUnseenGoal());
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 12, true, false,
                player -> player instanceof Player p && p.isAlive() && !p.isCreative() && !p.isSpectator()));
    }
    private final class AdvanceUnseenGoal extends Goal {
        private int repathDelay;
        private int attackCooldown;
        private AdvanceUnseenGoal() { setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }
        @Override public boolean canUse() {
            return getTarget() instanceof Player player && player.isAlive()
                    && distanceToSqr(player) < 900.0D && !isWatchedByAnyPlayer();
        }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void start() { repathDelay = 0; }
        @Override public void tick() {
            Player player = (Player) getTarget();
            getLookControl().setLookAt(player, 30.0F, 30.0F);
            if (isWatchedByAnyPlayer()) { entityData.set(DATA_ADVANCING, false); getNavigation().stop(); return; }
            entityData.set(DATA_ADVANCING, true);
            if (--repathDelay <= 0) {
                repathDelay = 8 + getRandom().nextInt(8);
                getNavigation().moveTo(player, 1.12D);
            }
            if (attackCooldown > 0) attackCooldown--;
            if (distanceToSqr(player) < 3.2D && hasLineOfSight(player) && attackCooldown == 0) {
                doHurtTarget(player);
                attackCooldown = 24;
            }
        }
        @Override public void stop() { entityData.set(DATA_ADVANCING, false); getNavigation().stop(); }
    }
    private boolean isWatchedByAnyPlayer() {
        return !level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(48.0D),
                p -> p.isAlive() && !p.isSpectator() && p.hasLineOfSight(this)).isEmpty();
    }
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide && getTarget() instanceof Player player) {
            if (distanceToSqr(player) > 900.0D) { setTarget(null); entityData.set(DATA_ADVANCING, false); }
            else if (!isWatchedByAnyPlayer() && distanceToSqr(player) < 256.0D && tickCount % 240 == 0 && getRandom().nextInt(3) == 0)
                if (OutlandsConfig.HORROR_AUDIO_ENABLED.get()) playSound(OutlandsSounds.OBSERVER_CLICK.get(), 0.22F, 0.85F + getRandom().nextFloat() * 0.25F);
        }
    }
    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.OBSERVER_CLICK.get() : null; }
    @Override protected SoundEvent getDeathSound() { return null;
    }
}
