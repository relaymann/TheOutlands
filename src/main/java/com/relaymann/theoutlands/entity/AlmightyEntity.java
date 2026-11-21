package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.horror.HorrorMemory;
import com.relaymann.theoutlands.horror.AlmightyTerritory;
import com.relaymann.theoutlands.item.OutlandsItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** The Almighty's endgame manifestation, with escalating phases and a persistent ending clue. */
public final class AlmightyEntity extends Monster {
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("The Almighty"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    private int lastPhase = 1;

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

    private boolean isWithinHuntTerritory(Player player) {
        // The story boss is confined to the configured Overworld borderland.
        // Its exceptionally rare natural End spawns still need to hunt normally.
        return !level().dimension().equals(Level.OVERWORLD)
                || AlmightyTerritory.contains(level(), player.getX(), player.getZ());
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.12D, true));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 96.0F));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                player -> player instanceof Player p && p.isAlive() && !p.isCreative() && !p.isSpectator()
                        && isWithinHuntTerritory(p)));
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (getTarget() instanceof Player target && (!target.isAlive() || target.isCreative() || target.isSpectator()
                || !isWithinHuntTerritory(target))) {
            setTarget(null);
            getNavigation().stop();
        }
        float healthRatio = getHealth() / getMaxHealth();
        bossEvent.setProgress(Math.max(0.0F, healthRatio));
        int phase = healthRatio > 0.66F ? 1 : healthRatio > 0.33F ? 2 : 3;
        if (phase != lastPhase) {
            lastPhase = phase;
            bossEvent.setColor(phase == 1 ? BossEvent.BossBarColor.PURPLE
                    : phase == 2 ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.WHITE);
            if (level() instanceof ServerLevel server) {
                server.sendParticles(phase == 3 ? ParticleTypes.SOUL : ParticleTypes.REVERSE_PORTAL,
                        getX(), getY() + 1.0D, getZ(), 70, 8.0D, 3.0D, 8.0D, 0.04D);
                if (OutlandsConfig.HORROR_AUDIO_ENABLED.get()) server.playSound(null, blockPosition(), OutlandsSounds.ALMIGHTY_PRESENCE.get(),
                        SoundSource.HOSTILE, 1.0F, phase == 3 ? 0.32F : 0.48F);
            }
        }

        int interval = phase == 1 ? 120 : phase == 2 ? 90 : 65;
        if (tickCount % interval != 0) return;
        double radius = phase == 1 ? 7.0D : phase == 2 ? 10.0D : 14.0D;
        float damage = phase == 1 ? 5.0F : phase == 2 ? 7.0F : 9.0F;
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(radius),
                player -> player.isAlive() && !player.isCreative() && !player.isSpectator()
                        && isWithinHuntTerritory(player)
                        && distanceToSqr(player) <= radius * radius && hasLineOfSight(player))) {
            if (player.isCreative() || player.isSpectator()) continue;
            double dx = player.getX() - getX();
            double dz = player.getZ() - getZ();
            double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
            if (phase == 2) {
                // The middle phase drags the player inward before the next melee window.
                player.push(-dx / length * 0.72D, 0.16D, -dz / length * 0.72D);
            } else if (phase == 3) {
                // The final phase breaks defensive positioning with a wider shockwave.
                player.push(dx / length * 1.35D, 0.48D, dz / length * 1.35D);
            }
            player.hurt(damageSources().mobAttack(this), damage);
        }
        if (OutlandsConfig.HORROR_AUDIO_ENABLED.get()) level().playSound(null, blockPosition(), SoundEvents.WARDEN_ROAR,
                SoundSource.HOSTILE, phase == 3 ? 0.85F : 0.62F, phase == 3 ? 0.38F : 0.56F);
    }

    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("almighty_phase", lastPhase);
    }

    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        int savedPhase = tag.contains("almighty_phase") ? tag.getInt("almighty_phase")
                : (getHealth() / getMaxHealth() > 0.66F ? 1 : getHealth() / getMaxHealth() > 0.33F ? 2 : 3);
        lastPhase = Math.max(1, Math.min(3, savedPhase));
    }

    @Override public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override public void die(DamageSource source) {
        if (!level().isClientSide && !getPersistentData().getBoolean("outlands_ending_dropped")) {
            ItemEntity page = new ItemEntity(level(), getX(), getY() + 0.8D, getZ(),
                    new ItemStack(OutlandsItems.THE_LAST_PAGE.get()));
            page.setDefaultPickUpDelay();
            page.setUnlimitedLifetime();
            page.setInvulnerable(true);
            boolean dropped = level().addFreshEntity(page);
            if (dropped) getPersistentData().putBoolean("outlands_ending_dropped", true);
            if (level() instanceof ServerLevel server) {
                for (ServerPlayer player : server.getEntitiesOfClass(ServerPlayer.class,
                        new AABB(blockPosition()).inflate(128.0D))) {
                    HorrorMemory.recordDiscovery(player, "almighty_defeated");
                    if (dropped) HorrorMemory.recordDiscovery(player, "the_last_page_dropped");
                    // The ending is conveyed by the dropped page and the world, not narrator chat.
                }
            }
        }
        super.die(source);
    }

    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return null; }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return OutlandsConfig.HORROR_AUDIO_ENABLED.get() ? OutlandsSounds.ALMIGHTY_PRESENCE.get() : null; }
}
