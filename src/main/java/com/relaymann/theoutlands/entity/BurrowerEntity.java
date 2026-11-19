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

/** A stealth ambusher. It uses vanilla pathfinding while concealed and reveals at striking distance; terrain is never modified. */
public final class BurrowerEntity extends Monster {
    private boolean submerged;
    private int revealCooldown;
    public BurrowerEntity(EntityType<? extends Monster> type, Level level){super(type,level);xpReward=7;}
    public static AttributeSupplier.Builder createAttributes(){
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,46.0D)
                .add(Attributes.MOVEMENT_SPEED,0.27D).add(Attributes.FOLLOW_RANGE,36.0D)
                .add(Attributes.ATTACK_DAMAGE,8.0D).add(Attributes.ARMOR,4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE,0.5D);
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(1,new MeleeAttackGoal(this,1.25D,true));
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
            if(!submerged && d>30.0D && d<400.0D && hasLineOfSight(player)){
                submerged=true;setInvisible(true);setSprinting(false);
            }
            if(submerged && d<20.0D){
                submerged=false;setInvisible(false);revealCooldown=65;
                playSound(OutlandsSounds.BURROWER_RISE.get(),1.1F,0.82F+getRandom().nextFloat()*0.18F);
                setSprinting(true);
            }
            if(!submerged && revealCooldown==0 && d>100.0D && !hasLineOfSight(player)){
                submerged=true;setInvisible(true);
            }
        } else if(submerged){submerged=false;setInvisible(false);}
        if(submerged){setSprinting(false);}
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putBoolean("submerged",submerged);tag.putInt("reveal_cd",revealCooldown);}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);submerged=tag.getBoolean("submerged");revealCooldown=tag.getInt("reveal_cd");setInvisible(submerged);}
    @Override protected SoundEvent getAmbientSound(){return null;}
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source){return OutlandsSounds.BURROWER_RISE.get();}
    @Override protected SoundEvent getDeathSound(){return null;}
}