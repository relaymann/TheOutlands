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
    private static final Map<String, RawAnimation> ATTACK_VARIANTS = buildVariantAnimations("attack", false);
    private static final Map<String, RawAnimation> DEATH_VARIANTS = buildVariantAnimations("death", false);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

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
            if (switch (id) { case "burrower", "dredge", "ceiling_dweller", "nightmare_hound", "lurker" -> true; default -> false; }) return CRAWL_SPRINT;
            return SPRINT;
        }
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

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
