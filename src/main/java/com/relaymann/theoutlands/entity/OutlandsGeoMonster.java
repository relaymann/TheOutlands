package com.relaymann.theoutlands.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
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

    public final void playHorrorAnimation(String animation) {
        triggerAnim("combat", animation);
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide) playHorrorAnimation(deathAnimation());
        super.die(source);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && !level().isClientSide) playHorrorAnimation(attackAnimationId());
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
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

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
