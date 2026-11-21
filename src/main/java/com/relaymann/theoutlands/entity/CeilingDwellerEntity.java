package com.relaymann.theoutlands.entity;
import com.relaymann.theoutlands.OutlandsConfig;import com.relaymann.theoutlands.sound.OutlandsSounds;import net.minecraft.nbt.CompoundTag;import net.minecraft.sounds.SoundEvent;import net.minecraft.world.entity.EntityType;import net.minecraft.world.entity.ai.attributes.AttributeSupplier;import net.minecraft.world.entity.ai.attributes.Attributes;import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;import net.minecraft.world.entity.monster.Monster;import net.minecraft.world.entity.player.Player;import net.minecraft.world.level.Level;import net.minecraft.world.phys.Vec3;
/** A cave ambusher that rises toward overhead stone, braces, then drops on nearby prey. */
public final class CeilingDwellerEntity extends Monster{
 private int braceTicks,dropCooldown;private boolean braced;
 public CeilingDwellerEntity(EntityType<? extends Monster> t,Level l){super(t,l);xpReward=6;}
 public static AttributeSupplier.Builder createAttributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,30).add(Attributes.MOVEMENT_SPEED,.31).add(Attributes.FOLLOW_RANGE,26).add(Attributes.ATTACK_DAMAGE,6).add(Attributes.ARMOR,1);}
 @Override protected void registerGoals(){goalSelector.addGoal(1,new MeleeAttackGoal(this,1.25D,true));goalSelector.addGoal(7,new RandomLookAroundGoal(this));targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,16,true,false,p->p instanceof Player x&&x.isAlive()&&!x.isCreative()&&!x.isSpectator()));}
 @Override public void tick(){super.tick();if(level().isClientSide)return;if(dropCooldown>0)dropCooldown--;if(braceTicks>0)braceTicks--;Player p=getTarget() instanceof Player x?x:null;
 if(p==null){if(!braced&&braceTicks==0&&getRandom().nextInt(180)==0&&hasOverheadStone()){braced=true;braceTicks=70;setNoGravity(true);setDeltaMovement(0,.16,0);}if(braced&&braceTicks>0){var above=blockPosition().above();if(horizontalCollision||level().getBlockState(above).isSolidRender(level(),above))setDeltaMovement(Vec3.ZERO);else setDeltaMovement(0,.12,0);}if(braceTicks==0){braced=false;setNoGravity(false);}}
 else if(braced&&distanceToSqr(p)<100&&dropCooldown==0){braced=false;setNoGravity(false);setDeltaMovement(0,-.42,0);hasImpulse=true;dropCooldown=100;}
 else if(distanceToSqr(p)<16&&onGround()&&dropCooldown==0&&hasLineOfSight(p)){Vec3 v=p.position().subtract(position()).normalize();setDeltaMovement(getDeltaMovement().add(v.x*.35,.18,v.z*.35));hasImpulse=true;dropCooldown=80;}
 if(!braced)setNoGravity(false);}
 private boolean hasOverheadStone(){for(int y=3;y<=8;y++){var p=blockPosition().above(y);if(level().getBlockState(p).isSolidRender(level(),p))return true;}return false;}
 @Override protected SoundEvent getAmbientSound(){return OutlandsConfig.HORROR_AUDIO_ENABLED.get()?OutlandsSounds.CEILING_CRAWL.get():null;}
 @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource s){return OutlandsConfig.HORROR_AUDIO_ENABLED.get()?OutlandsSounds.CEILING_HIT.get():null;}
 @Override protected SoundEvent getDeathSound(){return getHurtSound(null);}
 @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("brace",braceTicks);t.putInt("drop",dropCooldown);t.putBoolean("braced",braced);}
 @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);braceTicks=t.getInt("brace");dropCooldown=t.getInt("drop");braced=t.getBoolean("braced");}
}