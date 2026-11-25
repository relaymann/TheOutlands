package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
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

/** A cave ambusher that climbs into overhead cover, braces, and drops when prey comes close. */
public final class CeilingDwellerEntity extends OutlandsGeoMonster {
    private static final EntityDataAccessor<Boolean> DATA_BRACED =
            SynchedEntityData.defineId(CeilingDwellerEntity.class, EntityDataSerializers.BOOLEAN);
    private int braceTicks;
    private int dropCooldown;

    public CeilingDwellerEntity(EntityType<? extends OutlandsGeoMonster> type, Level level) {
        super(type, level);
        xpReward = 6;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BRACED, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 34.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.29D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.ARMOR, 1.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.18D, true));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 16, true, false,
                player -> player instanceof Player p && p.isAlive() && !p.isCreative() && !p.isSpectator()));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            setNoGravity(isBraced());
            return;
        }

        if (dropCooldown > 0) dropCooldown--;
        if (braceTicks > 0) braceTicks--;

        Player prey = getTarget() instanceof Player player ? player : null;
        if (prey != null && isValidPrey(prey)) {
            if (isBraced() && distanceToSqr(prey) < 144.0D) releaseAmbush();
            if (distanceToSqr(prey) < 20.25D && onGround() && dropCooldown == 0 && hasLineOfSight(prey)) {
                Vec3 direction = prey.position().subtract(position()).normalize();
                setDeltaMovement(getDeltaMovement().add(direction.x * 0.32D, 0.16D, direction.z * 0.32D));
                hasImpulse = true;
                dropCooldown = 75;
            }
        } else if (!isBraced() && braceTicks == 0 && dropCooldown == 0
                && getRandom().nextInt(240) == 0 && hasOverheadStone()) {
            setNoGravity(true);
            Vec3 lift = new Vec3(0.0D, 0.12D, 0.0D);
            if (level().noCollision(this, getBoundingBox().move(lift))) {
                setDeltaMovement(lift);
                setBraced(true);
                braceTicks = 75;
            } else {
                setNoGravity(false);
            }
        }

        if (isBraced()) {
            BlockPos ceiling = findCeiling();
            if (ceiling == null || braceTicks == 0) {
                releaseAmbush();
            } else if (level().noCollision(this, getBoundingBox().move(0.0D, 0.04D, 0.0D))) {
                setDeltaMovement(0.0D, 0.04D, 0.0D);
            } else {
                setDeltaMovement(Vec3.ZERO);
            }
        } else {
            setNoGravity(false);
        }
    }

    private boolean isValidPrey(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    private boolean hasOverheadStone() {
        return findCeiling() != null;
    }

    private BlockPos findCeiling() {
        for (int dy = 2; dy <= 8; dy++) {
            BlockPos pos = blockPosition().above(dy);
            if (level().getBlockState(pos).isSolidRender(level(), pos)) return pos;
        }
        return null;
    }

    private void setBraced(boolean braced) {
        entityData.set(DATA_BRACED, braced);
        setNoGravity(braced);
    }

    private void releaseAmbush() {
        playHorrorAnimation("drop");
        setBraced(false);
        braceTicks = 0;
        setDeltaMovement(0.0D, -0.38D, 0.0D);
        hasImpulse = true;
        dropCooldown = Math.max(dropCooldown, 90);
        if (OutlandsConfig.HORROR_AUDIO_ENABLED.get()) {
            level().playSound(null, blockPosition(), OutlandsSounds.CEILING_DROP.get(),
                    SoundSource.HOSTILE, 0.8F, 0.78F + getRandom().nextFloat() * 0.12F);
        }
    }

    public boolean isBraced() {
        return entityData.get(DATA_BRACED);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.CEILING_CRAWL.get() : null;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.CEILING_HIT.get() : null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return getHurtSound(null);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("brace_ticks", braceTicks);
        tag.putInt("drop_cooldown", dropCooldown);
        tag.putBoolean("braced", isBraced());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        braceTicks = tag.getInt("brace_ticks");
        dropCooldown = tag.getInt("drop_cooldown");
        setBraced(tag.getBoolean("braced"));
    }
}