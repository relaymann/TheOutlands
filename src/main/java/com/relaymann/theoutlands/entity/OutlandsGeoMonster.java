package com.relaymann.theoutlands.entity;

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
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.outlands.attack");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected OutlandsGeoMonster(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "locomotion", 4, state ->
                state.setAndContinue(state.isMoving() ? WALK : IDLE)));
        controllers.add(new AnimationController<>(this, "combat", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK));
    }

    /** Starts the attack keyframes only after a real melee hit succeeds. */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            triggerAnim("combat", "attack");
        }
        return hit;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
