package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.NightmareBalancePolicy;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.phys.Vec3;

/** Five NIGHTMARE-exclusive predators with shared navigation but distinct temperaments. */
public final class NightmareCreature extends Monster {
    private int leapCooldown;
    private int feintCooldown;
    private int flankCooldown;
    private int screechCooldown;
    private double memoryX;
    private double memoryY;
    private double memoryZ;
    private boolean hasMemory;

    public NightmareCreature(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 38.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.FOLLOW_RANGE, 80.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.12D);
    }

    @Override
    protected void registerGoals() {
        if (!isVariant("nightmare_false_survivor")) {
            goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.12D, true));
        }
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10,
                true, false, target -> target instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator()));
    }

    public String getVariantId() {
        return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath();
    }

    public boolean isVariant(String id) {
        return getVariantId().equals(id);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (leapCooldown > 0) leapCooldown--;
        if (feintCooldown > 0) feintCooldown--;
        if (flankCooldown > 0) flankCooldown--;
        if (screechCooldown > 0) screechCooldown--;

        if (!(getTarget() instanceof Player player) || !player.isAlive()
                || player.isCreative() || player.isSpectator()) {
            setSprinting(false);
            return;
        }

        double distance = distanceToSqr(player);
        if (OutlandsConfig.HORROR_AUDIO_ENABLED.get() && screechCooldown == 0
                && distance < 100.0D && tickCount % 35 == 0) {
            playSound(OutlandsSounds.NIGHTMARE_SCREECH.get(), 0.82F,
                    0.76F + getRandom().nextFloat() * 0.30F);
            screechCooldown = 150 + getRandom().nextInt(100);
        }
        if (hasLineOfSight(player)) {
            memoryX = player.getX();
            memoryY = player.getY();
            memoryZ = player.getZ();
            hasMemory = true;
        } else if (hasMemory && tickCount % 12 == 0 && distance > 100.0D) {
            getNavigation().moveTo(memoryX, memoryY, memoryZ, 0.96D);
        }

        if (isVariant("nightmare_hound")) {
            houndBehavior(player, distance);
        } else if (isVariant("nightmare_watcher")) {
            watcherBehavior(player, distance);
        } else if (isVariant("nightmare_wardened")) {
            wardenedBehavior(player, distance);
        } else if (isVariant("nightmare_false_survivor")) {
            falseSurvivorBehavior(player, distance);
        } else {
            unboundBehavior(player, distance);
        }
        if (tickCount % 80 == 0 && (isVariant("nightmare_hound")
                || isVariant("nightmare_watcher") || isVariant("nightmare_wardened"))) {
            coordinateNearbyPredators(player);
        }
    }

    /** Nearby Nightmare predators converge on a shared sighting without global aggro. */
    private void coordinateNearbyPredators(Player player) {
        for (NightmareCreature ally : level().getEntitiesOfClass(NightmareCreature.class,
                getBoundingBox().inflate(26.0D, 12.0D, 26.0D),
                candidate -> candidate != this && candidate.isAlive()
                        && candidate.getTarget() == null
                        && !candidate.isVariant("nightmare_false_survivor"))) {
            ally.setTarget(player);
        }
    }

    private void houndBehavior(Player player, double distance) {
        setSprinting(distance > 20.0D && distance < 2_500.0D);
        if (leapCooldown == 0 && distance > 20.0D && distance < 225.0D
                && onGround() && hasLineOfSight(player)) {
            Vec3 direction = player.position().subtract(position()).normalize();
            setDeltaMovement(getDeltaMovement().add(direction.x * 0.92D, 0.42D, direction.z * 0.92D));
            hasImpulse = true;
            leapCooldown = 65 + getRandom().nextInt(35);
        }
    }

    private void watcherBehavior(Player player, double distance) {
        getLookControl().setLookAt(player, 22.0F, 18.0F);
        Vec3 toWatcher = position().subtract(player.position());
        double lookDot = toWatcher.lengthSqr() < 1.0E-6D
                ? 1.0D : player.getLookAngle().dot(toWatcher.normalize());
        if (NightmareBalancePolicy.watcherShouldFreeze(distance, lookDot, hasLineOfSight(player))) {
            getNavigation().stop();
            setSprinting(false);
            return;
        }

        // The Watcher advances whenever it is not being watched and becomes
        // especially dangerous once it has closed the distance.
        setSprinting(distance > 16.0D);
        if (getNavigation().isDone() || tickCount % 12 == 0) {
            getNavigation().moveTo(player, distance > 400.0D ? 1.04D : 1.20D);
        }
    }

    private void wardenedBehavior(Player player, double distance) {
        setSprinting(distance > 16.0D);
        if (distance > 9.0D && (getNavigation().isDone() || tickCount % 18 == 0)) {
            getNavigation().moveTo(player, 1.18D);
        }
    }

    private void falseSurvivorBehavior(Player player, double distance) {
        Vec3 toCreature = position().subtract(player.position()).normalize();
        boolean beingWatched = distance < 1_024.0D && player.getLookAngle().dot(toCreature) > 0.76D;
        if (beingWatched && hasLineOfSight(player)) {
            getNavigation().stop();
            setSprinting(false);
            setTarget(null);
            getLookControl().setLookAt(player, 12.0F, 12.0F);
            return;
        }
        if (distance < 1_600.0D && feintCooldown == 0 && tickCount % 20 == 0) {
            getNavigation().moveTo(player, 0.68D);
            feintCooldown = 50 + getRandom().nextInt(50);
        }
    }

    private void unboundBehavior(Player player, double distance) {
        setSprinting(distance > 16.0D && distance < 4_000.0D);
        if (flankCooldown == 0 && distance < 3_600.0D && distance > 25.0D
                && getNavigation().isDone()) {
            Vec3 direction = player.position().subtract(position()).normalize();
            Vec3 side = direction.yRot(getRandom().nextBoolean() ? 1.25F : -1.25F);
            Vec3 point = player.position().subtract(side.scale(5.0D + getRandom().nextDouble() * 4.0D));
            getNavigation().moveTo(point.x, point.y, point.z, 1.08D);
            flankCooldown = 35 + getRandom().nextInt(35);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isVariant("nightmare_wardened")) amount *= 0.76F;
        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (!OutlandsConfig.HORROR_AUDIO_ENABLED.get()) return null;
        return switch (getVariantId()) {
            case "nightmare_watcher" -> OutlandsSounds.NIGHTMARE_WATCHER.get();
            case "nightmare_hound" -> OutlandsSounds.NIGHTMARE_HOUND.get();
            case "nightmare_wardened" -> OutlandsSounds.NIGHTMARE_WARDENED.get();
            case "nightmare_false_survivor" -> OutlandsSounds.NIGHTMARE_FALSE_SURVIVOR.get();
            default -> OutlandsSounds.NIGHTMARE_UNBOUND.get();
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return getAmbientSound();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return getAmbientSound();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("nightmare_memory_x", memoryX);
        tag.putDouble("nightmare_memory_y", memoryY);
        tag.putDouble("nightmare_memory_z", memoryZ);
        tag.putBoolean("nightmare_has_memory", hasMemory);
        tag.putInt("nightmare_leap_cooldown", leapCooldown);
        tag.putInt("nightmare_feint_cooldown", feintCooldown);
        tag.putInt("nightmare_flank_cooldown", flankCooldown);
        tag.putInt("nightmare_screech_cooldown", screechCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        memoryX = tag.getDouble("nightmare_memory_x");
        memoryY = tag.getDouble("nightmare_memory_y");
        memoryZ = tag.getDouble("nightmare_memory_z");
        hasMemory = tag.getBoolean("nightmare_has_memory");
        leapCooldown = tag.getInt("nightmare_leap_cooldown");
        feintCooldown = tag.getInt("nightmare_feint_cooldown");
        flankCooldown = tag.getInt("nightmare_flank_cooldown");
        screechCooldown = tag.getInt("nightmare_screech_cooldown");
    }
}
