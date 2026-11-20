package com.relaymann.theoutlands.entity;

import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.sounds.SoundEvent;
import com.relaymann.theoutlands.sound.OutlandsSounds;

/**
 * The Stalker is a patient pursuit specialist. It remembers confirmed sightings,
 * searches around the last known position, and tries to cut off moving prey.
 */
public final class StalkerEntity extends Monster {
    private double memoryX, memoryY, memoryZ;
    private int lostSightTicks;
    private int searchTicks;
    private int interceptCooldown;
    private boolean hasMemory;

    public StalkerEntity(EntityType<? extends Monster> type, Level level) { super(type, level); xpReward = 10; }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 46.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D).add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.5D).add(Attributes.ARMOR, 5.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new MeleeAttackGoal(this, 1.20D, true));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                player -> player instanceof Player p && p.isAlive() && !p.isCreative() && !p.isSpectator()));
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (interceptCooldown > 0) interceptCooldown--;

        if (getTarget() instanceof Player player && player.isAlive()) {
            double distance = distanceToSqr(player);
            if (hasLineOfSight(player)) {
                memoryX = player.getX(); memoryY = player.getY(); memoryZ = player.getZ();
                hasMemory = true; lostSightTicks = 0; searchTicks = 0;

                // Periodically aim ahead of a moving target, attempting to intercept
                // rather than following the exact trail of the player.
                if (interceptCooldown == 0 && distance > 100.0D && distance < 2500.0D
                        && player.getDeltaMovement().horizontalDistanceSqr() > 0.012D) {
                    Vec3 lead = player.position().add(player.getDeltaMovement().scale(12.0D));
                    getNavigation().moveTo(lead.x, lead.y, lead.z, 1.08D);
                    interceptCooldown = 55 + getRandom().nextInt(35);
                }
            } else {
                lostSightTicks++;
                if (hasMemory && lostSightTicks < 120 && tickCount % 10 == 0)
                    getNavigation().moveTo(memoryX, memoryY, memoryZ, 1.05D);
                if (lostSightTicks >= 120) {
                    // Keep the memory briefly and search around it instead of
                    // immediately dropping the hunt when a corner breaks sight.
                    searchTicks++;
                    if (searchTicks < 90 && tickCount % 16 == 0) {
                        double radius = 3.0D + (searchTicks / 12.0D);
                        double angle = searchTicks * 0.72D;
                        getNavigation().moveTo(memoryX + Math.cos(angle) * radius, memoryY,
                                memoryZ + Math.sin(angle) * radius, 0.92D);
                    }
                    if (searchTicks >= 90) {
                        hasMemory = false;
                        searchTicks = 0;
                        lostSightTicks = 0;
                        setTarget(null);
                        getNavigation().stop();
                    }
                }
            }

            setSprinting(distance > 25.0D && distance < 1600.0D);
            // At close range it briefly slows to circle and pressure the player,
            // then commits again; this avoids a permanently straight-line chase.
            if (distance < 100.0D && distance > 20.0D && tickCount % 65 == 0) {
                Vec3 side = player.position().subtract(position()).normalize()
                        .yRot((getRandom().nextBoolean() ? 1.0F : -1.0F) * 0.85F);
                Vec3 flank = player.position().add(side.scale(3.5D));
                getNavigation().moveTo(flank.x, flank.y, flank.z, 0.82D);
            }
        } else if (hasMemory) {
            searchTicks++;
            if (searchTicks <= 90 && tickCount % 16 == 0) {
                double angle = searchTicks * 0.72D;
                double radius = 3.0D + searchTicks / 12.0D;
                getNavigation().moveTo(memoryX + Math.cos(angle) * radius, memoryY,
                        memoryZ + Math.sin(angle) * radius, 0.9D);
            } else if (searchTicks > 90) {
                hasMemory = false;
                searchTicks = 0;
                getNavigation().stop();
                setSprinting(false);
            }
        }
    }

    @Override protected SoundEvent getAmbientSound() { return OutlandsSounds.STALKER_BREATH.get(); }
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return OutlandsSounds.STALKER_BREATH.get(); }
    @Override protected SoundEvent getDeathSound() { return null; }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("stalker_memory_x", memoryX); tag.putDouble("stalker_memory_y", memoryY);
        tag.putDouble("stalker_memory_z", memoryZ); tag.putBoolean("stalker_has_memory", hasMemory);
        tag.putInt("stalker_lost_sight", lostSightTicks); tag.putInt("stalker_search", searchTicks);
        tag.putInt("stalker_intercept_cd", interceptCooldown);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        memoryX = tag.getDouble("stalker_memory_x"); memoryY = tag.getDouble("stalker_memory_y");
        memoryZ = tag.getDouble("stalker_memory_z"); hasMemory = tag.getBoolean("stalker_has_memory");
        lostSightTicks = tag.getInt("stalker_lost_sight"); searchTicks = tag.getInt("stalker_search");
        interceptCooldown = tag.getInt("stalker_intercept_cd");
    }
}
