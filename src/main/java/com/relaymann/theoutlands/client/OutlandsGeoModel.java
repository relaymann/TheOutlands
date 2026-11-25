package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.OutlandsGeoMonster;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** Shared animated rig with texture selection per registered Outlands creature. */
public final class OutlandsGeoModel<T extends OutlandsGeoMonster> extends GeoModel<T> {
    private ResourceLocation modelFor(T entity) { String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath(); return ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID, "geo/entity/" + id + ".geo.json"); }
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
}
