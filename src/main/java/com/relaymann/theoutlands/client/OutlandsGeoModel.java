package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.OutlandsGeoMonster;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** Shared GeckoLib model selector; each registered creature retains its own texture. */
public final class OutlandsGeoModel<T extends OutlandsGeoMonster> extends GeoModel<T> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID, "geo/entity/outlands_monster.geo.json");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID, "animations/entity/outlands_monster.animation.json");

    @Override public ResourceLocation getModelResource(T entity) { return MODEL; }

    @Override public ResourceLocation getTextureResource(T entity) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        if (id.startsWith("nightmare_")) {
            return ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID, "textures/entity/nightmare/" + id.substring("nightmare_".length()) + ".png");
        }
        String texture = switch (id) {
            case "cave_jar" -> "agitated_one";
            case "stalker" -> "howler";
            case "hunter" -> "dredge";
            case "observer" -> "null_apostle";
            case "pilgrim" -> "null";
            case "ceiling_dweller" -> "burrower";
            case "colossus" -> "almighty";
            default -> id;
        };
        return ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID, "textures/entity/" + texture + ".png");
    }

    @Override public ResourceLocation getAnimationResource(T entity) { return ANIMATIONS; }
}
