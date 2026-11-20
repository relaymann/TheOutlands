package com.relaymann.theoutlands.sound;

import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Classic-era sound events backed by the user's locally imported 1.2.5-era recordings. */
public final class AlphaEraSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, TheOutlands.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> STEP_STONE = register("alpha_step_stone");
    public static final DeferredHolder<SoundEvent, SoundEvent> STEP_GRASS = register("alpha_step_grass");
    public static final DeferredHolder<SoundEvent, SoundEvent> STEP_GRAVEL = register("alpha_step_gravel");
    public static final DeferredHolder<SoundEvent, SoundEvent> STEP_SAND = register("alpha_step_sand");
    public static final DeferredHolder<SoundEvent, SoundEvent> STEP_SNOW = register("alpha_step_snow");
    public static final DeferredHolder<SoundEvent, SoundEvent> STEP_WOOD = register("alpha_step_wood");
    public static final DeferredHolder<SoundEvent, SoundEvent> STEP_CLOTH = register("alpha_step_cloth");
    public static final DeferredHolder<SoundEvent, SoundEvent> STEP_LADDER = register("alpha_step_ladder");
    public static final DeferredHolder<SoundEvent, SoundEvent> DIG_STONE = register("alpha_dig_stone");
    public static final DeferredHolder<SoundEvent, SoundEvent> DIG_GRASS = register("alpha_dig_grass");
    public static final DeferredHolder<SoundEvent, SoundEvent> DIG_GRAVEL = register("alpha_dig_gravel");
    public static final DeferredHolder<SoundEvent, SoundEvent> DIG_SAND = register("alpha_dig_sand");
    public static final DeferredHolder<SoundEvent, SoundEvent> DIG_SNOW = register("alpha_dig_snow");
    public static final DeferredHolder<SoundEvent, SoundEvent> DIG_WOOD = register("alpha_dig_wood");
    public static final DeferredHolder<SoundEvent, SoundEvent> DIG_CLOTH = register("alpha_dig_cloth");
    public static final DeferredHolder<SoundEvent, SoundEvent> DOOR_OPEN = register("alpha_door_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> DOOR_CLOSE = register("alpha_door_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLAYER_HURT = register("alpha_player_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> FALL_SMALL = register("alpha_fall_small");
    public static final DeferredHolder<SoundEvent, SoundEvent> FALL_BIG = register("alpha_fall_big");
    public static final DeferredHolder<SoundEvent, SoundEvent> WATER = register("alpha_water");
    public static final DeferredHolder<SoundEvent, SoundEvent> SWIM = register("alpha_swim");
    public static final DeferredHolder<SoundEvent, SoundEvent> LAVA = register("alpha_lava");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAVE = register("alpha_cave_ambience");
    public static final DeferredHolder<SoundEvent, SoundEvent> RAIN = register("alpha_rain");
    public static final DeferredHolder<SoundEvent, SoundEvent> THUNDER = register("alpha_thunder");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHICKEN = register("alpha_chicken");
    public static final DeferredHolder<SoundEvent, SoundEvent> COW = register("alpha_cow");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIG = register("alpha_pig");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHEEP = register("alpha_sheep");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHEEP_SHEAR = register("alpha_sheep_shear");
    public static final DeferredHolder<SoundEvent, SoundEvent> WOLF = register("alpha_wolf");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZOMBIE = register("alpha_zombie");
    public static final DeferredHolder<SoundEvent, SoundEvent> SKELETON = register("alpha_skeleton");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIDER = register("alpha_spider");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREEPER = register("alpha_creeper");
    public static final DeferredHolder<SoundEvent, SoundEvent> GHAST = register("alpha_ghast");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENDERMAN = register("alpha_enderman");
    public static final DeferredHolder<SoundEvent, SoundEvent> VILLAGER = register("alpha_villager");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    private AlphaEraSounds() {}
}
