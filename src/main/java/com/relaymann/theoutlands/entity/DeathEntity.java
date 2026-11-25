package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.relaymann.theoutlands.horror.AlmightyTerritory;
import java.util.UUID;

public final class DeathEntity extends OutlandsGeoMonster {
    private UUID huntTarget;
    private int farTicks;
    private long pauseUntilGameTime;
    private long temporaryExpiresAt = -1L;

    public void setTemporaryExpiresAt(long gameTime) { temporaryExpiresAt = gameTime; }

    public DeathEntity(EntityType<? extends OutlandsGeoMonster> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        xpReward = 0;
        addTag("theoutlands_death");
    }

    public void setHuntTarget(ServerPlayer player) {
        huntTarget = player.getUUID();
        setTarget(player);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 160.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.29D)
                .add(Attributes.ATTACK_DAMAGE, 11.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.2D)
                .add(Attributes.FOLLOW_RANGE, 160.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.65D);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.18D, true));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.72D));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 48.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));

    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (temporaryExpiresAt >= 0L && level().getGameTime() >= temporaryExpiresAt) { discard(); return; }
        if (huntTarget == null) {

            Player nearest = level().getNearestPlayer(getX(), getY(), getZ(), 72.0D,
                    entity -> entity instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator());
            if (!(nearest instanceof ServerPlayer serverPlayer)) return;
            huntTarget = serverPlayer.getUUID();
        }
        ServerPlayer target = level().getServer().getPlayerList().getPlayer(huntTarget);
        if (target == null || !target.isAlive() || target.isCreative() || target.isSpectator() || target.serverLevel() != level()
                || AlmightyTerritory.contains(target.serverLevel(), target.getX(), target.getZ())) {
            setTarget(null);
            getNavigation().stop();
            return;
        }

        double distance = distanceToSqr(target);

        if (OutlandsConfig.HORROR_AUDIO_ENABLED.get() && tickCount % 120 == 0
                && distance <= 36.0D * 36.0D && getRandom().nextInt(3) == 0) {
            level().playSound(null, blockPosition(), OutlandsSounds.DEATH_PRESENCE.get(),
                    SoundSource.HOSTILE, 0.24F, 0.68F + getRandom().nextFloat() * 0.1F);
        }
        if (distance > 100.0D * 100.0D) farTicks++;
        else farTicks = 0;

        if (farTicks >= 60 && pauseUntilGameTime <= level().getGameTime()) {
            pauseUntilGameTime = level().getGameTime() + 100L + getRandom().nextInt(100);
            farTicks = 0;
        }
        if (level().getGameTime() < pauseUntilGameTime || distance > 160.0D * 160.0D) {
            setTarget(null);
            getNavigation().stop();
            setSprinting(false);
            return;
        }

        setTarget(target);

        if (distance < 24.0D * 24.0D && tickCount % 360 < 64) {
            getNavigation().stop();
            getLookControl().setLookAt(target, 24.0F, 24.0F);
            setSprinting(false);
        } else {
            setSprinting(distance < 18.0D * 18.0D);
        }
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (huntTarget != null) tag.putUUID("hunt_target", huntTarget);
        tag.putInt("far_ticks", farTicks);
        tag.putLong("pause_until_game_time", pauseUntilGameTime);
        if (temporaryExpiresAt >= 0L) tag.putLong("outlands_temporary_expires_at", temporaryExpiresAt);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        huntTarget = tag.hasUUID("hunt_target") ? tag.getUUID("hunt_target") : null;
        farTicks = tag.getInt("far_ticks");
        pauseUntilGameTime = tag.getLong("pause_until_game_time");
        temporaryExpiresAt = tag.contains("outlands_temporary_expires_at") ? tag.getLong("outlands_temporary_expires_at")
                : (getTags().contains("theoutlands_aggressive_jumpscare") ? level().getGameTime() + 14L : -1L);
    }

    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return null; }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return null; }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return null; }
}
