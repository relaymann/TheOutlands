package com.relaymann.theoutlands.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import com.relaymann.theoutlands.sound.OutlandsSounds;

/** A still, human-shaped lure that only commits once a player enters its reveal radius. */
public final class MimicEntity extends Monster {
    private boolean revealed;
    private int revealCooldown;
    public MimicEntity(EntityType<? extends Monster> type,Level level){super(type,level);xpReward=8;}
    public static AttributeSupplier.Builder createAttributes(){
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,38.0D)
                .add(Attributes.MOVEMENT_SPEED,0.34D).add(Attributes.FOLLOW_RANGE,24.0D)
                .add(Attributes.ATTACK_DAMAGE,6.0D).add(Attributes.ARMOR,2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE,0.15D);
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(1,new MeleeAttackGoal(this,1.32D,true));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,10,true,false,
                p -> p instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator()));
    }
    @Override public void tick(){
        super.tick();
        if(level().isClientSide)return;
        if(revealCooldown>0)revealCooldown--;
        if(getTarget() instanceof Player player){
            double d=distanceToSqr(player);
            if(!revealed && d<64.0D){
                revealed=true;revealCooldown=100;
                playSound(OutlandsSounds.MIMIC_REVEAL.get(),1.0F,0.92F+getRandom().nextFloat()*0.12F);
            }
            if(!revealed){getNavigation().stop();setSprinting(false);}
            else setSprinting(d>12.0D);
        }else if(revealed && revealCooldown==0){revealed=false;}
    }
    public boolean isRevealed(){return revealed;}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putBoolean("revealed",revealed);tag.putInt("reveal_cd",revealCooldown);}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);revealed=tag.getBoolean("revealed");revealCooldown=tag.getInt("reveal_cd");}
    @Override protected SoundEvent getAmbientSound(){return null;}
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source){return OutlandsSounds.MIMIC_REVEAL.get();}
    @Override protected SoundEvent getDeathSound(){return null;}
}