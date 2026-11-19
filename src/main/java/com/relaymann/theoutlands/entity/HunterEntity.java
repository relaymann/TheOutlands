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

/** A pursuit specialist that searches the last place it saw its prey and commits to short ambush lunges. */
public final class HunterEntity extends Monster {
    private double memoryX, memoryY, memoryZ;
    private int lostSightTicks, pounceCooldown, searchTicks;
    private boolean hasMemory;
    public HunterEntity(EntityType<? extends Monster> type, Level level) { super(type, level); xpReward = 9; }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 52.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D).add(Attributes.FOLLOW_RANGE, 56.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D).add(Attributes.ARMOR, 3.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.18D, true));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 12, true, false,
                p -> p instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator()));
    }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (pounceCooldown > 0) pounceCooldown--;
        if (getTarget() instanceof Player player && player.isAlive()) {
            if (hasLineOfSight(player)) {
                memoryX=player.getX(); memoryY=player.getY(); memoryZ=player.getZ();
                hasMemory=true; lostSightTicks=0; searchTicks=0;
            } else {
                lostSightTicks++;
                if (hasMemory && lostSightTicks < 100 && tickCount % 12 == 0)
                    getNavigation().moveTo(memoryX, memoryY, memoryZ, 1.12D);
                if (lostSightTicks >= 100) { hasMemory=false; setTarget(null); getNavigation().stop(); }
            }
            double d=distanceToSqr(player);
            setSprinting(d > 20.0D && d < 1800.0D);
            if (pounceCooldown==0 && d > 16.0D && d < 90.0D && onGround() && hasLineOfSight(player)) {
                Vec3 dir=player.position().subtract(position()).normalize();
                setDeltaMovement(getDeltaMovement().add(dir.x*0.72D,0.38D,dir.z*0.72D));
                hasImpulse=true; pounceCooldown=90+getRandom().nextInt(45);
            }
        } else if (hasMemory && searchTicks++ < 80 && tickCount % 20 == 0) {
            getNavigation().moveTo(memoryX,memoryY,memoryZ,0.95D);
        }
    }
    @Override protected SoundEvent getAmbientSound(){return OutlandsSounds.HUNTER_STALK.get();}
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source){return OutlandsSounds.HUNTER_STALK.get();}
    @Override protected SoundEvent getDeathSound(){return OutlandsSounds.HUNTER_STALK.get();}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putDouble("hunt_x",memoryX);tag.putDouble("hunt_y",memoryY);tag.putDouble("hunt_z",memoryZ);tag.putBoolean("has_memory",hasMemory);tag.putInt("lost_sight",lostSightTicks);tag.putInt("pounce_cd",pounceCooldown);}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);memoryX=tag.getDouble("hunt_x");memoryY=tag.getDouble("hunt_y");memoryZ=tag.getDouble("hunt_z");hasMemory=tag.getBoolean("has_memory");lostSightTicks=tag.getInt("lost_sight");pounceCooldown=tag.getInt("pounce_cd");}
}