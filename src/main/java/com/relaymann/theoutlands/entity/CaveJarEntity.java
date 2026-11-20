package com.relaymann.theoutlands.entity;

import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvent;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import com.relaymann.theoutlands.OutlandsConfig;

/**
 * Cave.jar is a committed cave predator. It tracks confirmed sightings,
 * searches the last known area, and uses a telegraphed close-range lunge.
 */
public final class CaveJarEntity extends Monster {
    private double memoryX, memoryY, memoryZ;
    private int lostSightTicks, searchTicks, lungeCooldown;
    private boolean hasMemory;

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
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 12, true, false,
                p -> p instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator()));
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (lungeCooldown > 0) lungeCooldown--;

        if (getTarget() instanceof Player player && player.isAlive()) {
            double distance = distanceToSqr(player);
            if (hasLineOfSight(player)) {
                memoryX = player.getX(); memoryY = player.getY(); memoryZ = player.getZ();
                hasMemory = true; lostSightTicks = 0; searchTicks = 0;
            } else {
                lostSightTicks++;
                if (hasMemory && lostSightTicks < 110 && tickCount % 12 == 0)
                    getNavigation().moveTo(memoryX, memoryY, memoryZ, 1.08D);
                if (lostSightTicks >= 110) {
                    searchTicks++;
                    if (searchTicks < 80 && tickCount % 18 == 0) {
                        double angle = searchTicks * 0.8D;
                        double radius = 2.0D + searchTicks * 0.12D;
                        getNavigation().moveTo(memoryX + Math.cos(angle) * radius, memoryY,
                                memoryZ + Math.sin(angle) * radius, 0.88D);
                    } else if (searchTicks >= 80) {
                        hasMemory = false; searchTicks = 0; lostSightTicks = 0;
                        setTarget(null); getNavigation().stop();
                    }
                }
            }

            if (distance < 16.0D && hasLineOfSight(player)) setSprinting(true);
            else if (distance > 36.0D) setSprinting(false);

            // A short wind-up is communicated by a pause before the lunge.
            // It is dangerous in a tunnel but still avoidable by moving laterally.
            if (lungeCooldown == 0 && distance > 9.0D && distance < 42.0D
                    && hasLineOfSight(player) && onGround() && tickCount % 24 == 0) {
                getNavigation().stop();
                Vec3 direction = player.position().subtract(position()).normalize();
                setDeltaMovement(getDeltaMovement().add(direction.x * 0.88D, 0.34D, direction.z * 0.88D));
                hasImpulse = true;
                lungeCooldown = 100 + getRandom().nextInt(45);
            }
        } else if (hasMemory) {
            searchTicks++;
            if (searchTicks < 80 && tickCount % 18 == 0) {
                double angle = searchTicks * 0.8D;
                double radius = 2.0D + searchTicks * 0.12D;
                getNavigation().moveTo(memoryX + Math.cos(angle) * radius, memoryY,
                        memoryZ + Math.sin(angle) * radius, 0.88D);
            } else if (searchTicks >= 80) {
                hasMemory = false; searchTicks = 0; getNavigation().stop();
            }
        }
    }

    @Override protected SoundEvent getAmbientSound() { return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.CAVEJAR_STALK.get() : null; }
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.CAVEJAR_STALK.get() : null; }
    @Override protected SoundEvent getDeathSound() { return null; }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("cavejar_memory_x", memoryX); tag.putDouble("cavejar_memory_y", memoryY);
        tag.putDouble("cavejar_memory_z", memoryZ); tag.putBoolean("cavejar_has_memory", hasMemory);
        tag.putInt("cavejar_lost_sight", lostSightTicks); tag.putInt("cavejar_search", searchTicks);
        tag.putInt("cavejar_lunge_cd", lungeCooldown);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        memoryX = tag.getDouble("cavejar_memory_x"); memoryY = tag.getDouble("cavejar_memory_y");
        memoryZ = tag.getDouble("cavejar_memory_z"); hasMemory = tag.getBoolean("cavejar_has_memory");
        lostSightTicks = tag.getInt("cavejar_lost_sight"); searchTicks = tag.getInt("cavejar_search");
        lungeCooldown = tag.getInt("cavejar_lunge_cd");
    }
}
