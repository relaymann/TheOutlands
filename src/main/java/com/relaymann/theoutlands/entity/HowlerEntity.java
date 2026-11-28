package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.sound.OutlandsSounds;
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

public final class HowlerEntity extends OutlandsGeoMonster {
    private int callCooldown;
    private int windup;
    private boolean calling;
    private int territoryPressure;
    public HowlerEntity(EntityType<? extends OutlandsGeoMonster> type, Level level){super(type,level);xpReward=6;}
    public static AttributeSupplier.Builder createAttributes(){
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,34.0D)
            .add(Attributes.MOVEMENT_SPEED,.25D).add(Attributes.FOLLOW_RANGE,34.0D)
            .add(Attributes.ATTACK_DAMAGE,6.0D).add(Attributes.ARMOR,2.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE,.18D);
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(1,new MeleeAttackGoal(this,1.05D,true));
        goalSelector.addGoal(6,new RandomStrollGoal(this,.65D));
        goalSelector.addGoal(7,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,20,true,false,
            p -> p instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator()));
    }
    @Override public void tick(){
        super.tick(); if(level().isClientSide)return;
        if(callCooldown>0)callCooldown--;
        if(territoryPressure>0)territoryPressure--;
        if(getTarget() instanceof Player player && player.isAlive()){
            double d=distanceToSqr(player);
            if(d>64 && d<900 && callCooldown==0 && !calling){calling=true;windup=36;playHorrorAnimation("scream");getNavigation().stop();territoryPressure=36;}
            if(calling){
                getNavigation().stop();
                if(--windup<=0){callTerritory();calling=false;callCooldown=520+getRandom().nextInt(280);}
            }
        } else calling=false;
    }
    private void callTerritory(){
        if(OutlandsConfig.HORROR_AUDIO_ENABLED.get())playSound(OutlandsSounds.HOWLER_CALL.get(),1.45F,.82F+getRandom().nextFloat()*.12F);

        for(Monster mob:level().getEntitiesOfClass(Monster.class,getBoundingBox().inflate(32,14,32),
            m -> m!=this && (m instanceof HunterEntity || m instanceof StalkerEntity || m instanceof LurkerEntity || m instanceof DredgeEntity))){
            if(getTarget()!=null && mob.getTarget()==null && mob.hasLineOfSight(this)) mob.setTarget(getTarget());
            else if(mob instanceof LurkerEntity && mob.getTarget()==null && mob.distanceToSqr(this)>36.0D)
                mob.getNavigation().moveTo(this,0.82D);
        }
    }
    public boolean isCalling(){return calling;}
    public float getCallProgress(float partialTick){return calling?1.0F-(Math.max(0,windup-partialTick)/36.0F):0.0F;}
    @Override protected SoundEvent getAmbientSound(){return OutlandsConfig.HORROR_AUDIO_ENABLED.get()?OutlandsSounds.HOWLER_BREATH.get():null;}
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource s){return OutlandsConfig.HORROR_AUDIO_ENABLED.get()?OutlandsSounds.HOWLER_HIT.get():null;}
    @Override protected SoundEvent getDeathSound(){return OutlandsConfig.HORROR_AUDIO_ENABLED.get()?OutlandsSounds.HOWLER_HIT.get():null;}
}
