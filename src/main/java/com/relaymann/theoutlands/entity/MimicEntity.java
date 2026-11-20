package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;

/**
 * A block-mimicking ambush predator. Before revealing, the client renders the
 * Mimic as a nearby real block state. It never replaces or destroys world blocks.
 */
public final class MimicEntity extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_REVEALED =
            SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_DISGUISE_STATE =
            SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.INT);

    private boolean revealed;
    private boolean disguiseChosen;
    private int revealCooldown;
    private int unseenTicks;
    private int retreatTicks;
    private double memoryX, memoryY, memoryZ;
    private boolean hasMemory;

    public MimicEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 42.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.31D).add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D).add(Attributes.ARMOR, 3.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_REVEALED, false);
        builder.define(DATA_DISGUISE_STATE, Block.getId(Blocks.BARREL.defaultBlockState()));
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.28D, true));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 12, true, false,
                p -> p instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator()));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        if (!disguiseChosen) chooseDisguiseBlock();
        if (revealCooldown > 0) revealCooldown--;
        if (retreatTicks > 0) retreatTicks--;

        if (getTarget() instanceof Player player && player.isAlive()) {
            double distance = distanceToSqr(player);
            boolean watched = player.hasLineOfSight(this);
            if (watched) {
                memoryX = player.getX();
                memoryY = player.getY();
                memoryZ = player.getZ();
                hasMemory = true;
                unseenTicks = 0;
            } else {
                unseenTicks++;
            }

            if (!revealed && !watched && unseenTicks > 12 && distance > 30.0D
                    && distance < 625.0D && tickCount % 18 == 0) {
                getNavigation().moveTo(player, 0.78D);
            } else if (!revealed && watched) {
                getNavigation().stop();
            }

            // The block disguise breaks only when the player gets genuinely close.
            if (!revealed && distance < 64.0D) {
                revealed = true;
                entityData.set(DATA_REVEALED, true);
                revealCooldown = 120;
                unseenTicks = 0;
                playSound(OutlandsSounds.MIMIC_REVEAL.get(), 1.0F,
                        0.90F + getRandom().nextFloat() * 0.16F);
                Vec3 direction = player.position().subtract(position()).normalize();
                if (distance > 9.0D) {
                    setDeltaMovement(getDeltaMovement().add(direction.x * 0.48D, 0.16D, direction.z * 0.48D));
                    hasImpulse = true;
                }
            }

            if (revealed) {
                setSprinting(distance > 16.0D);
                if (distance < 9.0D && retreatTicks == 0 && getHealth() < getMaxHealth() * 0.35F
                        && getRandom().nextInt(100) == 0) {
                    Vec3 away = position().subtract(player.position()).normalize();
                    getNavigation().moveTo(getX() + away.x * 5.0D, getY(), getZ() + away.z * 5.0D, 1.1D);
                    retreatTicks = 70;
                }
            } else {
                setSprinting(false);
            }
        } else {
            setSprinting(false);
            if (revealed && revealCooldown == 0) {
                revealed = false;
                entityData.set(DATA_REVEALED, false);
                unseenTicks = 0;
                getNavigation().stop();
            }
            if (hasMemory && tickCount % 40 == 0 && retreatTicks == 0) {
                getNavigation().moveTo(memoryX, memoryY, memoryZ, 0.72D);
            }
        }
    }

    private void chooseDisguiseBlock() {
        disguiseChosen = true;
        BlockPos origin = blockPosition().below();
        BlockState selected = Blocks.BARREL.defaultBlockState();

        // Prefer an ordinary nearby world block so the disguise fits its surroundings.
        for (int attempt = 0; attempt < 18; attempt++) {
            BlockPos candidate = origin.offset(
                    getRandom().nextInt(7) - 3,
                    getRandom().nextInt(3),
                    getRandom().nextInt(7) - 3);
            BlockState state = level().getBlockState(candidate);
            if (!state.isAir() && state.getFluidState().isEmpty()
                    && state.getRenderShape() == RenderShape.MODEL) {
                selected = state;
                break;
            }
        }
        entityData.set(DATA_DISGUISE_STATE, Block.getId(selected));
    }

    public boolean isRevealed() {
        return entityData.get(DATA_REVEALED);
    }

    public BlockState getDisguiseBlockState() {
        return Block.stateById(entityData.get(DATA_DISGUISE_STATE));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("revealed", revealed);
        tag.putBoolean("disguise_chosen", disguiseChosen);
        tag.putInt("disguise_state", entityData.get(DATA_DISGUISE_STATE));
        tag.putInt("reveal_cd", revealCooldown);
        tag.putInt("unseen_ticks", unseenTicks);
        tag.putInt("retreat_ticks", retreatTicks);
        tag.putDouble("mimic_memory_x", memoryX);
        tag.putDouble("mimic_memory_y", memoryY);
        tag.putDouble("mimic_memory_z", memoryZ);
        tag.putBoolean("mimic_has_memory", hasMemory);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        revealed = tag.getBoolean("revealed");
        disguiseChosen = tag.getBoolean("disguise_chosen");
        revealCooldown = tag.getInt("reveal_cd");
        unseenTicks = tag.getInt("unseen_ticks");
        retreatTicks = tag.getInt("retreat_ticks");
        memoryX = tag.getDouble("mimic_memory_x");
        memoryY = tag.getDouble("mimic_memory_y");
        memoryZ = tag.getDouble("mimic_memory_z");
        hasMemory = tag.getBoolean("mimic_has_memory");
        entityData.set(DATA_REVEALED, revealed);
        if (tag.contains("disguise_state")) {
            entityData.set(DATA_DISGUISE_STATE, tag.getInt("disguise_state"));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return OutlandsSounds.MIMIC_REVEAL.get();
    }

    @Override
    protected SoundEvent getDeathSound() { return null; }
}
