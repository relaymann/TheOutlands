package com.relaymann.theoutlands.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.outlands.attack");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected OutlandsGeoMonster(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    private RawAnimation movementAnimation(boolean moving) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath();
        if (!moving && (id.equals("fear") || id.equals("observer"))) return HOVER;
        if (moving) {
            if (switch (id) { case "burrower", "dredge", "ceiling_dweller", "nightmare_hound", "lurker" -> true; default -> false; }) return CRAWL;
            if (switch (id) { case "almighty", "colossus", "stillborn_statue" -> true; default -> false; }) return HEAVY_WALK;
            return WALK;
        }
        return IDLE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "locomotion", 4, state ->
                state.setAndContinue(movementAnimation(state.isMoving()))));
        controllers.add(new AnimationController<>(this, "combat", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK));
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
