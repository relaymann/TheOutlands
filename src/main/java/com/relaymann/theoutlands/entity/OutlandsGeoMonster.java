package com.relaymann.theoutlands.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.sounds.SoundEvent;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Shared GeckoLib animation host. Gameplay AI remains server-authoritative in each concrete creature. */
public abstract class OutlandsGeoMonster extends Monster implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.outlands.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.outlands.walk");
    private static final RawAnimation CRAWL = RawAnimation.begin().thenLoop("animation.outlands.crawl");
    private static final RawAnimation HEAVY_WALK = RawAnimation.begin().thenLoop("animation.outlands.heavy_walk");
    private static final RawAnimation HOVER = RawAnimation.begin().thenLoop("animation.outlands.hover");
    private static final RawAnimation STALK_IDLE = RawAnimation.begin().thenLoop("animation.outlands.stalk_idle");
    private static final RawAnimation WATCH_IDLE = RawAnimation.begin().thenLoop("animation.outlands.watch_idle");
    private static final RawAnimation STATUE_IDLE = RawAnimation.begin().thenLoop("animation.outlands.statue_idle");
    private static final RawAnimation SPRINT = RawAnimation.begin().thenLoop("animation.outlands.sprint");
    private static final RawAnimation CRAWL_SPRINT = RawAnimation.begin().thenLoop("animation.outlands.crawl_sprint");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.outlands.attack");
    private static final RawAnimation ATTACK_STALKER = RawAnimation.begin().thenPlay("animation.outlands.attack_stalker");
    private static final RawAnimation ATTACK_BITE = RawAnimation.begin().thenPlay("animation.outlands.attack_bite");
    private static final RawAnimation ATTACK_HEAVY = RawAnimation.begin().thenPlay("animation.outlands.attack_heavy");
    private static final RawAnimation ATTACK_SWEEP = RawAnimation.begin().thenPlay("animation.outlands.attack_sweep");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.outlands.death");
    private static final RawAnimation DEATH_STALKER = RawAnimation.begin().thenPlay("animation.outlands.death_stalker");
    private static final RawAnimation DEATH_HEAVY = RawAnimation.begin().thenPlay("animation.outlands.death_heavy");
    private static final RawAnimation DEATH_CRAWLER = RawAnimation.begin().thenPlay("animation.outlands.death_crawler");
    private static final RawAnimation DEATH_WATCHER = RawAnimation.begin().thenPlay("animation.outlands.death_watcher");
    private static final List<String> CREATURE_IDS = List.of(
            "null", "fear", "death", "almighty", "null_apostle", "cave_jar", "stalker", "observer",
            "hunter", "burrower", "mimic", "stillborn_statue", "agitated_one", "lurker", "howler",
            "dredge", "pilgrim", "ceiling_dweller", "colossus", "nightmare_watcher", "nightmare_hound",
            "nightmare_wardened", "nightmare_false_survivor", "nightmare_unbound");
    private static Map<String, RawAnimation> buildVariantAnimations(String prefix, boolean looping) {
        Map<String, RawAnimation> animations = new HashMap<>();
        for (String id : CREATURE_IDS) {
            String clip = "animation.outlands." + prefix + "_" + id;
            animations.put(id, looping ? RawAnimation.begin().thenLoop(clip) : RawAnimation.begin().thenPlay(clip));
        }
        return Map.copyOf(animations);
    }
    private static final Map<String, RawAnimation> IDLE_VARIANTS = buildVariantAnimations("idle", true);
    private static final Map<String, RawAnimation> WALK_VARIANTS = buildVariantAnimations("walk", true);
    private static final Map<String, RawAnimation> SPRINT_VARIANTS = buildVariantAnimations("sprint", true);
    private static final Map<String, RawAnimation> ATTACK_VARIANTS = buildVariantAnimations("attack", false);
    private static final Map<String, RawAnimation> DEATH_VARIANTS = buildVariantAnimations("death", false);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int movementSoundCooldown;
    private long lastHeardNoiseId;
    private int investigationTicks;
    private int investigationRepath;
    private double investigationX, investigationY, investigationZ;
    private boolean investigating;

    protected OutlandsGeoMonster(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    private String creatureId() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath();
    }

    private RawAnimation movementAnimation(boolean moving) {
        String id = creatureId();
        if (!moving) {
            RawAnimation individualIdle = IDLE_VARIANTS.get(id);
            if (individualIdle != null) return individualIdle;
            if (id.equals("fear")) return HOVER;
            if (id.equals("observer") || id.equals("nightmare_watcher")) return WATCH_IDLE;
            if (id.equals("stillborn_statue")) return STATUE_IDLE;
            if (switch (id) { case "death", "stalker", "hunter", "lurker", "nightmare_false_survivor" -> true; default -> false; }) return STALK_IDLE;
            return IDLE;
        }
        if (isSprinting()) {
            RawAnimation individualSprint = SPRINT_VARIANTS.get(id);
            if (individualSprint != null) return individualSprint;
            if (switch (id) { case "burrower", "dredge", "ceiling_dweller", "nightmare_hound", "lurker" -> true; default -> false; }) return CRAWL_SPRINT;
            return SPRINT;
        }
        RawAnimation individualWalk = WALK_VARIANTS.get(id);
        if (individualWalk != null) return individualWalk;
        if (switch (id) { case "burrower", "dredge", "ceiling_dweller", "nightmare_hound", "lurker" -> true; default -> false; }) return CRAWL;
        if (switch (id) { case "almighty", "colossus", "stillborn_statue" -> true; default -> false; }) return HEAVY_WALK;
        return WALK;
    }

    private String attackAnimationId() {
        return ATTACK_VARIANTS.containsKey(creatureId()) ? "attack_" + creatureId() : "attack";
    }

    private String deathAnimation() {
        return DEATH_VARIANTS.containsKey(creatureId()) ? "death_" + creatureId() : "death";
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "locomotion", 4, state ->
                state.setAndContinue(movementAnimation(state.isMoving()))));
        AnimationController<OutlandsGeoMonster> combat = new AnimationController<>(this, "combat", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK)
                .triggerableAnim("attack_stalker", ATTACK_STALKER)
                .triggerableAnim("attack_bite", ATTACK_BITE)
                .triggerableAnim("attack_heavy", ATTACK_HEAVY)
                .triggerableAnim("attack_sweep", ATTACK_SWEEP)
                .triggerableAnim("scream", RawAnimation.begin().thenPlay("animation.outlands.scream"))
                .triggerableAnim("lunge", RawAnimation.begin().thenPlay("animation.outlands.lunge"))
                .triggerableAnim("pounce", RawAnimation.begin().thenPlay("animation.outlands.pounce"))
                .triggerableAnim("reveal", RawAnimation.begin().thenPlay("animation.outlands.reveal"))
                .triggerableAnim("burrow", RawAnimation.begin().thenPlay("animation.outlands.burrow"))
                .triggerableAnim("emerge", RawAnimation.begin().thenPlay("animation.outlands.emerge"))
                .triggerableAnim("drop", RawAnimation.begin().thenPlay("animation.outlands.drop"))
                .triggerableAnim("ground_slam", RawAnimation.begin().thenPlay("animation.outlands.ground_slam"))
                .triggerableAnim("stalk", RawAnimation.begin().thenPlay("animation.outlands.stalk"))
                .triggerableAnim("death", DEATH)
                .triggerableAnim("death_stalker", DEATH_STALKER)
                .triggerableAnim("death_heavy", DEATH_HEAVY)
                .triggerableAnim("death_crawler", DEATH_CRAWLER)
                .triggerableAnim("death_watcher", DEATH_WATCHER);
        for (String id : CREATURE_IDS) {
            combat.triggerableAnim("attack_" + id, ATTACK_VARIANTS.get(id));
            combat.triggerableAnim("death_" + id, DEATH_VARIANTS.get(id));
        }
        controllers.add(combat);
    }

    /** Starts an authored, creature-specific action on the synchronized GeckoLib controller. */
    public final void playHorrorAnimation(String animation) {
        triggerAnim("combat", animation);
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide) playHorrorAnimation(deathAnimation());
        super.die(source);
    }

    /** Starts creature-specific attack keyframes only after a real melee hit succeeds. */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && !level().isClientSide) playHorrorAnimation(attackAnimationId());
        return hit;
    }

    /** Adds quiet, irregular movement tells without duplicating creatures that already own footsteps. */
    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && OutlandsConfig.ENTITY_INTELLIGENCE_ENABLED.get()) tickCreatureIntelligence();
        if (level().isClientSide || !OutlandsConfig.HORROR_AUDIO_ENABLED.get()) return;
        if (movementSoundCooldown > 0) movementSoundCooldown--;
        if (movementSoundCooldown > 0 || !onGround()
                || getDeltaMovement().horizontalDistanceSqr() < 0.0009D) return;

        SoundEvent step = switch (creatureId()) {
            case "death" -> OutlandsSounds.DEATH_PRESENCE.get();
            case "almighty" -> OutlandsSounds.COLOSSUS_STEP.get();
            case "cave_jar" -> OutlandsSounds.CAVEJAR_STALK.get();
            case "stalker" -> OutlandsSounds.STALKER_JOINT.get();
            case "observer" -> OutlandsSounds.OBSERVER_CLICK.get();
            case "hunter" -> OutlandsSounds.HUNTER_STALK.get();
            case "mimic" -> OutlandsSounds.MIMIC_REVEAL.get();
            case "stillborn_statue" -> OutlandsSounds.STILLBORN_STONE.get();
            case "agitated_one" -> OutlandsSounds.AGITATED_WARNING.get();
            case "lurker" -> OutlandsSounds.LURKER_BREATH.get();
            case "howler" -> OutlandsSounds.HOWLER_BREATH.get();
            case "dredge" -> OutlandsSounds.DREDGE_CRAWL.get();
            case "nightmare_watcher" -> OutlandsSounds.NIGHTMARE_WATCHER.get();
            case "nightmare_hound" -> OutlandsSounds.NIGHTMARE_HOUND.get();
            case "nightmare_wardened" -> OutlandsSounds.NIGHTMARE_WARDENED.get();
            case "nightmare_false_survivor" -> OutlandsSounds.NIGHTMARE_FALSE_SURVIVOR.get();
            case "nightmare_unbound" -> OutlandsSounds.NIGHTMARE_UNBOUND.get();
            default -> null;
        };
        if (step == null) return;
        float volume = creatureId().equals("almighty") ? 0.42F : 0.13F;
        playSound(step, volume, 0.82F + getRandom().nextFloat() * 0.24F);
        movementSoundCooldown = 24 + getRandom().nextInt(24);
    }

    /**
     * Shared sensory memory layer. Concrete creatures keep their signature behavior;
     * this layer only supplies sound investigation when they are not already engaged.
     */
    private void tickCreatureIntelligence() {
        if (!(level() instanceof ServerLevel serverLevel) || !isAlive()) return;
        String id = creatureId();
        if (getTarget() != null && getTarget().isAlive()) {
            if (hasLineOfSight(getTarget())) {
                investigating = false;
                investigationTicks = 0;
            }
            return;
        }

        double hearingRange = switch (id) {
            case "howler", "nightmare_hound", "nightmare_wardened", "nightmare_unbound" -> 38.0D;
            case "hunter", "stalker", "death", "dredge", "burrower" -> 32.0D;
            case "colossus", "almighty" -> 30.0D;
            case "observer", "nightmare_watcher", "stillborn_statue" -> 22.0D;
            case "mimic", "lurker", "cave_jar", "ceiling_dweller" -> 25.0D;
            default -> 18.0D;
        };
        CreatureNoise.Stimulus stimulus = CreatureNoise.hear(serverLevel, position(), hearingRange, lastHeardNoiseId);
        if (stimulus != null) {
            lastHeardNoiseId = stimulus.id();
            investigationX = stimulus.position().x;
            investigationY = stimulus.position().y;
            investigationZ = stimulus.position().z;
            investigationTicks = 0;
            investigationRepath = 0;
            investigating = true;

            // Pursuit specialists can identify and follow a player's sound source,
            // but never receive a target from an anonymous alarm or block sound.
            boolean pursuer = switch (id) {
                case "hunter", "stalker", "lurker", "dredge", "nightmare_hound",
                        "nightmare_wardened", "nightmare_unbound", "death" -> true;
                default -> false;
            };
            if (pursuer && stimulus.source() != null) {
                Player source = serverLevel.getPlayerByUUID(stimulus.source());
                if (source != null && source.isAlive() && !source.isCreative() && !source.isSpectator()) {
                    setTarget(source);
                    investigating = false;
                    return;
                }
            }
        }

        if (!investigating) return;
        investigationTicks++;
        if (investigationTicks > investigationDuration(id)) {
            investigating = false;
            investigationTicks = 0;
            getNavigation().stop();
            return;
        }

        // Search an expanding ring around the sound origin instead of repeatedly
        // pathing into one blocked doorway or wall.
        if (investigationRepath-- <= 0 || getNavigation().isDone()) {
            investigationRepath = 12 + getRandom().nextInt(9);
            double radius = Math.min(7.0D, 1.5D + investigationTicks / 18.0D);
            double angle = investigationTicks * investigationAngularSpeed(id);
            double x = investigationX + Math.cos(angle) * radius;
            double z = investigationZ + Math.sin(angle) * radius;
            double speed = investigationSpeed(id);
            getNavigation().moveTo(x, investigationY, z, speed);
        }
        if (distanceToSqr(investigationX, investigationY, investigationZ) < 3.0D
                && investigationTicks > 28) {
            // Pause briefly at the source, as if listening for the next movement.
            if (investigationTicks > 58) {
                investigating = false;
                investigationTicks = 0;
                getNavigation().stop();
            }
        }
    }

    private static int investigationDuration(String id) {
        return switch (id) {
            case "hunter", "stalker", "nightmare_hound", "nightmare_wardened", "nightmare_unbound" -> 150;
            case "howler", "observer", "nightmare_watcher", "mimic", "stillborn_statue" -> 105;
            case "colossus", "almighty", "death" -> 90;
            default -> 120;
        };
    }

    private static double investigationSpeed(String id) {
        return switch (id) {
            case "hunter", "nightmare_hound", "nightmare_unbound" -> 1.08D;
            case "stalker", "mimic", "nightmare_watcher", "observer" -> 0.76D;
            case "colossus", "almighty" -> 0.62D;
            case "burrower", "dredge", "lurker", "ceiling_dweller" -> 0.88D;
            default -> 0.82D;
        };
    }

    private static double investigationAngularSpeed(String id) {
        return switch (id) {
            case "observer", "nightmare_watcher", "stillborn_statue" -> 0.34D;
            case "hunter", "stalker", "nightmare_hound" -> 0.82D;
            case "mimic", "cave_jar" -> 0.48D;
            default -> 0.63D;
        };
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("outlands_last_heard_noise", lastHeardNoiseId);
        tag.putInt("outlands_investigation_ticks", investigationTicks);
        tag.putInt("outlands_investigation_repath", investigationRepath);
        tag.putDouble("outlands_investigation_x", investigationX);
        tag.putDouble("outlands_investigation_y", investigationY);
        tag.putDouble("outlands_investigation_z", investigationZ);
        tag.putBoolean("outlands_investigating", investigating);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        lastHeardNoiseId = tag.getLong("outlands_last_heard_noise");
        investigationTicks = tag.getInt("outlands_investigation_ticks");
        investigationRepath = tag.getInt("outlands_investigation_repath");
        investigationX = tag.getDouble("outlands_investigation_x");
        investigationY = tag.getDouble("outlands_investigation_y");
        investigationZ = tag.getDouble("outlands_investigation_z");
        investigating = tag.getBoolean("outlands_investigating");
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
