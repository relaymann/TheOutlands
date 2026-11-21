package com.relaymann.theoutlands.entity;
import com.relaymann.theoutlands.OutlandsConfig;import com.relaymann.theoutlands.sound.OutlandsSounds;import net.minecraft.nbt.CompoundTag;import net.minecraft.sounds.SoundEvent;import net.minecraft.world.entity.EntityType;import net.minecraft.world.entity.ai.attributes.AttributeSupplier;import net.minecraft.world.entity.ai.attributes.Attributes;import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;import net.minecraft.world.entity.monster.Monster;import net.minecraft.world.entity.player.Player;import net.minecraft.world.level.Level;import net.minecraft.util.Mth;
/** A silent wilderness wanderer; it avoids players and only fights back when attacked. */
public final class PilgrimEntity extends Monster{
 private int pauseTicks;
 public PilgrimEntity(EntityType<? extends Monster> t,Level l){super(t,l);xpReward=2;}
 public static AttributeSupplier.Builder createAttributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,22).add(Attributes.MOVEMENT_SPEED,.22).add(Attributes.FOLLOW_RANGE,36).add(Attributes.ATTACK_DAMAGE,3).add(Attributes.KNOCKBACK_RESISTANCE,.25);}
 @Override protected void registerGoals(){goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.82D,.001F));goalSelector.addGoal(7,new RandomLookAroundGoal(this));targetSelector.addGoal(1,new HurtByTargetGoal(this));}
 @Override public void tick(){super.tick();if(level().isClientSide)return;if(pauseTicks>0)pauseTicks--;Player p=level().getNearestPlayer(this,10);if(p!=null&&p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&getTarget()==null&&distanceToSqr(p)<36){getNavigation().stop();setYRot(getYRot()+Mth.wrapDegrees(p.getYRot()+180-getYRot())*.025F);if(pauseTicks==0)pauseTicks=60+getRandom().nextInt(80);}setSprinting(getTarget()!=null);}
 @Override protected SoundEvent getAmbientSound(){return OutlandsConfig.HORROR_AUDIO_ENABLED.get()?OutlandsSounds.PILGRIM_BREATH.get():null;}
 @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource s){return OutlandsConfig.HORROR_AUDIO_ENABLED.get()?OutlandsSounds.PILGRIM_HURT.get():null;}
 @Override protected SoundEvent getDeathSound(){return getHurtSound(null);}
 @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("pause",pauseTicks);}
 @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);pauseTicks=t.getInt("pause");}
}