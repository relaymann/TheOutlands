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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvent;

/**
 * A subterranean ambusher. It phases through the existing underground without
 * changing blocks, then finds a collision-safe surface opening before attacking.
 */
public final class BurrowerEntity extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_BURROWING =
            SynchedEntityData.defineId(BurrowerEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean submerged;
    private int revealCooldown;
    private int burrowTicks;
    private double burrowStartX, burrowStartY, burrowStartZ;

    public BurrowerEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 7;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 46.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D).add(Attributes.FOLLOW_RANGE, 36.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D).add(Attributes.ARMOR, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BURROWING, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, true));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                p -> p instanceof Player player && player.isAlive()
                        && !player.isCreative() && !player.isSpectator()));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (revealCooldown > 0) revealCooldown--;

        if (submerged) {
            tickBurrowTravel();
            return;
        }

        if (getTarget() instanceof Player player && player.isAlive()) {
            double distance = distanceToSqr(player);
            // Commit to a subterranean approach after a confirmed sighting or
            // when the player breaks sight at medium range.
            if (revealCooldown == 0 && distance > 64.0D && distance < 900.0D
                    && (hasLineOfSight(player) || !player.hasLineOfSight(this))) {
                beginBurrow();
            }
        }
    }

    private void beginBurrow() {
        submerged = true;
        entityData.set(DATA_BURROWING, true);
        burrowTicks = 0;
        burrowStartX = getX();
        burrowStartY = getY();
        burrowStartZ = getZ();
        getNavigation().stop();
        setSprinting(false);
        refreshDimensions();
        setInvisible(true);
        setSilent(true);
        noPhysics = true;
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);
        hasImpulse = true;
    }

    private void tickBurrowTravel() {
        // The normal melee goal runs in super.tick(); cancel its pathing while phased.
        getNavigation().stop();
        setDeltaMovement(Vec3.ZERO);
        Player player = getTarget() instanceof Player p && p.isAlive() ? p : null;
        if (player == null || ++burrowTicks > 260) {
            emergeAtStart();
            return;
        }

        // Travel below the target's feet, not directly through its body.
        Vec3 destination = new Vec3(player.getX(), Math.max(level().getMinBuildHeight() + 2,
                player.getY() - 2.6D), player.getZ());
        Vec3 offset = destination.subtract(position());
        double horizontal = Math.sqrt(offset.x * offset.x + offset.z * offset.z);

        if (horizontal <= 3.6D) {
            BlockPos opening = findEmergencePosition(player);
            if (opening != null) {
                emergeAt(new Vec3(opening.getX() + 0.5D, opening.getY(), opening.getZ() + 0.5D), player);
                return;
            }
        }

        Vec3 step = offset.normalize().scale(Math.min(0.22D, offset.length()));
        // Keep the creature below the target's feet while traversing.
        setPos(getX() + step.x, getY() + step.y, getZ() + step.z);
        setDeltaMovement(Vec3.ZERO);
        hasImpulse = true;
    }

    private BlockPos findEmergencePosition(Player player) {
        BlockPos base = player != null ? player.blockPosition()
                : BlockPos.containing(burrowStartX, burrowStartY, burrowStartZ);
        for (int radius = 0; radius <= 3; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    for (int dy = -1; dy <= 1; dy++) {
                        BlockPos feet = base.offset(dx, dy, dz);
                        BlockPos floor = feet.below();
                        if (!level().getBlockState(floor).isFaceSturdy(level(), floor,
                                net.minecraft.core.Direction.UP)) continue;
                        AABB candidate = getBoundingBox().move(
                                feet.getX() + 0.5D - getX(), feet.getY() - getY(),
                                feet.getZ() + 0.5D - getZ());
                        if (level().noCollision(this, candidate)) return feet;
                    }
                }
            }
        }
        return null;
    }

    private void emergeAtStart() {
        Vec3 position = new Vec3(burrowStartX, burrowStartY, burrowStartZ);
        if (level().noCollision(this, getBoundingBox().move(position.subtract(position())))) {
            emergeAt(position, getTarget() instanceof Player p ? p : null);
        } else {
            BlockPos safe = findEmergencePosition(getTarget() instanceof Player p ? p : null);
            if (safe != null) emergeAt(new Vec3(safe.getX() + 0.5D, safe.getY(), safe.getZ() + 0.5D),
                    getTarget() instanceof Player p ? p : null);
            else {
                // Keep phasing for a short extra interval rather than materialize inside stone.
                burrowTicks = Math.min(burrowTicks, 240);
            }
        }
    }

    private void emergeAt(Vec3 location, Player target) {
        setPos(location.x, location.y, location.z);
        noPhysics = false;
        setNoGravity(false);
        submerged = false;
        entityData.set(DATA_BURROWING, false);
        refreshDimensions();
        setInvisible(false);
        setSilent(false);
        revealCooldown = 90;
        playSound(OutlandsSounds.BURROWER_RISE.get(), 1.1F,
                0.82F + getRandom().nextFloat() * 0.18F);
        if (target != null && target.isAlive()) {
            Vec3 direction = target.position().subtract(position()).normalize();
            setDeltaMovement(direction.x * 0.58D, 0.24D, direction.z * 0.58D);
            hasImpulse = true;
            setSprinting(true);
        }
    }

    public boolean isSubmerged() {
        return entityData.get(DATA_BURROWING);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("submerged", submerged);
        tag.putInt("reveal_cd", revealCooldown);
        tag.putInt("burrow_ticks", burrowTicks);
        tag.putDouble("burrow_start_x", burrowStartX);
        tag.putDouble("burrow_start_y", burrowStartY);
        tag.putDouble("burrow_start_z", burrowStartZ);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        submerged = tag.getBoolean("submerged");
        revealCooldown = tag.getInt("reveal_cd");
        burrowTicks = tag.getInt("burrow_ticks");
        burrowStartX = tag.getDouble("burrow_start_x");
        burrowStartY = tag.getDouble("burrow_start_y");
        burrowStartZ = tag.getDouble("burrow_start_z");
        entityData.set(DATA_BURROWING, submerged);
        noPhysics = submerged;
        setNoGravity(submerged);
        setInvisible(submerged);
        setSilent(submerged);
    }

    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return OutlandsSounds.BURROWER_RISE.get();
    }
    @Override protected SoundEvent getDeathSound() { return null; }
}
