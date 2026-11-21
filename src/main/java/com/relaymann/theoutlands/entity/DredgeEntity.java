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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Low-profile pursuit predator. It follows a confirmed trail and lunges in short bursts. */
public final class DredgeEntity extends Monster {
    private int lungeCooldown;
    private int lostSight;
    private double lastX,lastY,lastZ;
    private boolean hasTrail;
    private int lungeTicks;
    public DredgeEntity(EntityType<? extends Monster> type,Level level){super(type,level);xpReward=6;}
    public static AttributeSupplier.Builder createAttributes(){
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,30.0D)
            .add(Attributes.MOVEMENT_SPEED,.31D).add(Attributes.FOLLOW_RANGE,42.0D)
            .add(Attributes.ATTACK_DAMAGE,5.5D).add(Attributes.ARMOR,1.0D);
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(1,new MeleeAttackGoal(this,1.16D,true));
        goalSelector.addGoal(6,new RandomStrollGoal(this,.72D));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,18,true,false,
            p -> p instanceof Player player && player.isAlive()&&!player.isCreative()&&!player.isSpectator()));
    }
    @Override public void tick(){
        super.tick();if(level().isClientSide)return;
        if(lungeCooldown>0)lungeCooldown--;
        if(lungeTicks>0)lungeTicks--;
        if(getTarget() instanceof Player player && player.isAlive()){
            if(hasLineOfSight(player)){lastX=player.getX();lastY=player.getY();lastZ=player.getZ();hasTrail=true;lostSight=0;}
            else if(hasTrail && ++lostSight<140 && tickCount%10==0)getNavigation().moveTo(lastX,lastY,lastZ,1.08D);
            if(lostSight>=140){hasTrail=false;setTarget(null);}
            double d=distanceToSqr(player);
            if(d>16&&d<144&&lungeCooldown==0&&onGround()&&hasLineOfSight(player)){
                Vec3 dir=player.position().subtract(position()).normalize();
                setDeltaMovement(getDeltaMovement().add(dir.x*.62D,.28D,dir.z*.62D));
                hasImpulse=true;lungeTicks=12;lungeCooldown=115+getRandom().nextInt(65);
                if(OutlandsConfig.HORROR_AUDIO_ENABLED.get())playSound(OutlandsSounds.DREDGE_LUNGE.get(),.8F,.9F+getRandom().nextFloat()*.15F);
            }
            setSprinting(d>25&&d<900);
        }else if(hasTrail){hasTrail=false;lostSight=0;getNavigation().stop();}
    }
    public boolean isLunging(){return lungeTicks>0;}
    @Override protected SoundEvent getAmbientSound(){return OutlandsConfig.HORROR_AUDIO_ENABLED.get()?OutlandsSounds.DREDGE_CRAWL.get():null;}
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource s){return OutlandsConfig.HORROR_AUDIO_ENABLED.get()?OutlandsSounds.DREDGE_HIT.get():null;}
    @Override protected SoundEvent getDeathSound(){return OutlandsConfig.HORROR_AUDIO_ENABLED.get()?OutlandsSounds.DREDGE_HIT.get():null;}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putDouble("trail_x",lastX);t.putDouble("trail_y",lastY);t.putDouble("trail_z",lastZ);t.putBoolean("has_trail",hasTrail);t.putInt("lost_sight",lostSight);t.putInt("lunge_cd",lungeCooldown);t.putInt("lunge_ticks",lungeTicks);}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);lastX=t.getDouble("trail_x");lastY=t.getDouble("trail_y");lastZ=t.getDouble("trail_z");hasTrail=t.getBoolean("has_trail");lostSight=t.getInt("lost_sight");lungeCooldown=t.getInt("lunge_cd");lungeTicks=t.getInt("lunge_ticks");}
}
