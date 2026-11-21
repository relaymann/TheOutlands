package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** A patient woodland ambusher: it watches, closes distance, strikes, then breaks contact. */
public final class LurkerEntity extends Monster {
    private int retreatTicks;
    private int ambushCooldown;
    public LurkerEntity(EntityType<? extends Monster> type, Level level) { super(type, level); xpReward=5; }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 26.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.29D).add(Attributes.FOLLOW_RANGE, 30.0D)
            .add(Attributes.ATTACK_DAMAGE, 5.0D).add(Attributes.ARMOR, 1.0D);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(1,new MeleeAttackGoal(this,1.18D,true));
        goalSelector.addGoal(6,new RandomStrollGoal(this,0.72D));
        goalSelector.addGoal(7,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,18,true,false,
            p -> p instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator()));
    }
    @Override public void tick() {
        super.tick();
        if(level().isClientSide) return;
        if(ambushCooldown>0) ambushCooldown--;
        if(retreatTicks>0) {
            retreatTicks--;
            if(retreatTicks==0) { setTarget(null); setSprinting(false); }
        }
        if(getTarget() instanceof Player player && player.isAlive()) {
            double d=distanceToSqr(player);
            if(d>100 && d<625 && hasLineOfSight(player)) {
                // Hold position and lower its silhouette while it observes the player.
                getNavigation().stop(); setPose(net.minecraft.world.entity.Pose.CROUCHING);
                if(ambushCooldown==0 && getRandom().nextInt(100)==0) ambushCooldown=50;
            } else setPose(net.minecraft.world.entity.Pose.STANDING);
            if(getHealth()<getMaxHealth()*0.34F && retreatTicks==0 && ambushCooldown==0) {
                Vec3 away=position().subtract(player.position()).normalize();
                getNavigation().moveTo(getX()+away.x*13,getY(),getZ()+away.z*13,1.22D);
                setSprinting(true); retreatTicks=70; ambushCooldown=240;
            }
            if(d<12 && onGround() && ambushCooldown==0) {
                Vec3 toward=player.position().subtract(position()).normalize();
                setDeltaMovement(getDeltaMovement().add(toward.x*.38D,.16D,toward.z*.38D));
                hasImpulse=true; ambushCooldown=85;
            }
        } else if(getPose()==net.minecraft.world.entity.Pose.CROUCHING) setPose(net.minecraft.world.entity.Pose.STANDING);
    }
    @Override protected SoundEvent getAmbientSound(){return OutlandsConfig.HORROR_AUDIO_ENABLED.get()?OutlandsSounds.LURKER_BREATH.get():null;}
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource s){return OutlandsConfig.HORROR_AUDIO_ENABLED.get()?OutlandsSounds.LURKER_HIT.get():null;}
    @Override protected SoundEvent getDeathSound(){return OutlandsConfig.HORROR_AUDIO_ENABLED.get()?OutlandsSounds.LURKER_HIT.get():null;}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("retreat_ticks",retreatTicks);t.putInt("ambush_cd",ambushCooldown);}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);retreatTicks=t.getInt("retreat_ticks");ambushCooldown=t.getInt("ambush_cd");}
}
