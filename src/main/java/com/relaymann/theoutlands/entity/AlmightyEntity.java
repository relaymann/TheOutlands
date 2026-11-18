package com.relaymann.theoutlands.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** The Almighty's endgame manifestation and final boss encounter. */
public final class AlmightyEntity extends Monster {
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("The Almighty"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    public AlmightyEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        xpReward = 0;
        addTag("theoutlands_almighty");
        bossEvent.setDarkenScreen(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 900.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.31D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 160.0D)
                .add(Attributes.ARMOR, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.12D, true));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 96.0F));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        bossEvent.setProgress(Math.max(0.0F, getHealth() / getMaxHealth()));
        // Three escalating pressure pulses make the fight change as its health falls.
        int interval = getHealth() > getMaxHealth() * 0.66F ? 120
                : getHealth() > getMaxHealth() * 0.33F ? 90 : 65;
        if (tickCount % interval == 0) {
            float damage = getHealth() > getMaxHealth() * 0.33F ? 5.0F : 8.0F;
            for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(7.0D),
                    Player::isAlive)) {
                if (player.isCreative() || player.isSpectator()) continue;
                if (player.hurt(damageSources().mobAttack(this), damage)) {
                    double dx = player.getX() - getX();
                    double dz = player.getZ() - getZ();
                    double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
                    player.push(dx / length * 1.25D, 0.42D, dz / length * 1.25D);
                }
            }
            level().playSound(null, blockPosition(), SoundEvents.WARDEN_ROAR,
                    SoundSource.HOSTILE, 0.7F, getHealth() > getMaxHealth() * 0.33F ? 0.55F : 0.38F);
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return null; }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return null; }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return SoundEvents.WARDEN_DEATH; }
}
