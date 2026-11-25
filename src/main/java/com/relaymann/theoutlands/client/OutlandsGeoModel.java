package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.OutlandsGeoMonster;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

/** Shared animated rig with per-creature resources, gaze tracking, and subtle facial motion. */
public final class OutlandsGeoModel<T extends OutlandsGeoMonster> extends GeoModel<T> {
    private ResourceLocation modelFor(T entity) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        return ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID, "geo/entity/" + id + ".geo.json");
    }
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID, "animations/entity/outlands_monster.animation.json");

    @Override public ResourceLocation getModelResource(T entity) { return modelFor(entity); }

    @Override public ResourceLocation getTextureResource(T entity) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        if (id.startsWith("nightmare_")) {
            return ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID, "textures/entity/nightmare/" + id.substring("nightmare_".length()) + ".png");
        }
        return ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID, "textures/entity/" + id + ".png");
    }

    @Override public ResourceLocation getAnimationResource(T entity) { return ANIMATIONS; }

    /** Adds restrained, client-side gaze tracking on top of authored animation keyframes. */
    @Override
    public void setCustomAnimations(T entity, long instanceId, AnimationState<T> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);
        GeoBone head = getAnimationProcessor().getBone("head");
        if (head == null) return;

        EntityModelData look = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        if (look != null) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
            float yawLimit = (id.equals("observer") || id.equals("nightmare_watcher")) ? 42.0F : 28.0F;
            float pitchLimit = (id.equals("observer") || id.equals("nightmare_watcher")) ? 24.0F : 17.0F;
            float yaw = Mth.clamp(look.netHeadYaw(), -yawLimit, yawLimit);
            float pitch = Mth.clamp(look.headPitch(), -pitchLimit, pitchLimit);
            head.setRotY(head.getRotY() + yaw * Mth.DEG_TO_RAD * 0.72F);
            head.setRotX(head.getRotX() + pitch * Mth.DEG_TO_RAD * 0.72F);
        }

        GeoBone jaw = getAnimationProcessor().getBone("jaw");
        if (jaw != null && (entity.getType() == com.relaymann.theoutlands.entity.OutlandsEntities.STALKER.get()
                || entity.getType() == com.relaymann.theoutlands.entity.OutlandsEntities.DEATH.get()
                || entity.getType() == com.relaymann.theoutlands.entity.OutlandsEntities.NIGHTMARE_FALSE_SURVIVOR.get())) {
            float twitch = Mth.sin((entity.tickCount + animationState.getPartialTick()) * 0.19F) * 0.018F;
            jaw.setRotX(jaw.getRotX() + twitch);
        }
    }
}
