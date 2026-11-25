package com.relaymann.theoutlands.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
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
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected OutlandsGeoMonster(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    private RawAnimation movementAnimation(boolean moving) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath();
        if (!moving) {
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

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "locomotion", 4, state ->
                state.setAndContinue(movementAnimation(state.isMoving()))));
        controllers.add(new AnimationController<>(this, "combat", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK)
                .triggerableAnim("scream", RawAnimation.begin().thenPlay("animation.outlands.scream"))
                .triggerableAnim("lunge", RawAnimation.begin().thenPlay("animation.outlands.lunge"))
                .triggerableAnim("pounce", RawAnimation.begin().thenPlay("animation.outlands.pounce"))
                .triggerableAnim("reveal", RawAnimation.begin().thenPlay("animation.outlands.reveal"))
                .triggerableAnim("burrow", RawAnimation.begin().thenPlay("animation.outlands.burrow"))
                .triggerableAnim("emerge", RawAnimation.begin().thenPlay("animation.outlands.emerge"))
                .triggerableAnim("drop", RawAnimation.begin().thenPlay("animation.outlands.drop"))
                .triggerableAnim("ground_slam", RawAnimation.begin().thenPlay("animation.outlands.ground_slam"))
                .triggerableAnim("stalk", RawAnimation.begin().thenPlay("animation.outlands.stalk"))
                .triggerableAnim("death", RawAnimation.begin().thenPlay("animation.outlands.death")));
    }

    /** Starts an authored, creature-specific action on the synchronized GeckoLib controller. */
    public final void playHorrorAnimation(String animation) {
        triggerAnim("combat", animation);
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide) playHorrorAnimation("death");
        super.die(source);
    }

    /** Starts the attack keyframes only after a real melee hit succeeds. */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) triggerAnim("combat", "attack");
        return hit;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
