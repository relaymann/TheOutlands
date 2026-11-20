package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.sound.AlphaEraSounds;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;

import java.util.Map;

/** Replaces selected vanilla sound events with classic-era recordings without replacing terrain or resource systems. */
@EventBusSubscriber(modid = "theoutlands", bus = EventBusSubscriber.Bus.GAME)
public final class AlphaSoundCompatibilityEvents {
    private static final Map<String, Holder<SoundEvent>> REPLACEMENTS = Map.ofEntries(
        Map.entry("minecraft:block.stone.step", AlphaEraSounds.STEP_STONE),
        Map.entry("minecraft:block.grass.step", AlphaEraSounds.STEP_GRASS),
        Map.entry("minecraft:block.gravel.step", AlphaEraSounds.STEP_GRAVEL),
        Map.entry("minecraft:block.sand.step", AlphaEraSounds.STEP_SAND),
        Map.entry("minecraft:block.snow.step", AlphaEraSounds.STEP_SNOW),
        Map.entry("minecraft:block.wood.step", AlphaEraSounds.STEP_WOOD),
        Map.entry("minecraft:block.wool.step", AlphaEraSounds.STEP_CLOTH),
        Map.entry("minecraft:block.ladder.step", AlphaEraSounds.STEP_LADDER),
        Map.entry("minecraft:block.stone.hit", AlphaEraSounds.DIG_STONE),
        Map.entry("minecraft:block.stone.break", AlphaEraSounds.DIG_STONE),
        Map.entry("minecraft:block.grass.hit", AlphaEraSounds.DIG_GRASS),
        Map.entry("minecraft:block.grass.break", AlphaEraSounds.DIG_GRASS),
        Map.entry("minecraft:block.gravel.hit", AlphaEraSounds.DIG_GRAVEL),
        Map.entry("minecraft:block.gravel.break", AlphaEraSounds.DIG_GRAVEL),
        Map.entry("minecraft:block.sand.hit", AlphaEraSounds.DIG_SAND),
        Map.entry("minecraft:block.sand.break", AlphaEraSounds.DIG_SAND),
        Map.entry("minecraft:block.snow.hit", AlphaEraSounds.DIG_SNOW),
        Map.entry("minecraft:block.snow.break", AlphaEraSounds.DIG_SNOW),
        Map.entry("minecraft:block.wood.hit", AlphaEraSounds.DIG_WOOD),
        Map.entry("minecraft:block.wood.break", AlphaEraSounds.DIG_WOOD),
        Map.entry("minecraft:block.wool.hit", AlphaEraSounds.DIG_CLOTH),
        Map.entry("minecraft:block.wool.break", AlphaEraSounds.DIG_CLOTH),
        Map.entry("minecraft:block.wooden_door.open", AlphaEraSounds.DOOR_OPEN),
        Map.entry("minecraft:block.wooden_door.close", AlphaEraSounds.DOOR_CLOSE),
        Map.entry("minecraft:block.iron_door.open", AlphaEraSounds.DOOR_OPEN),
        Map.entry("minecraft:block.iron_door.close", AlphaEraSounds.DOOR_CLOSE),
        Map.entry("minecraft:entity.player.hurt", AlphaEraSounds.PLAYER_HURT),
        Map.entry("minecraft:entity.player.small_fall", AlphaEraSounds.FALL_SMALL),
        Map.entry("minecraft:entity.player.big_fall", AlphaEraSounds.FALL_BIG),
        Map.entry("minecraft:block.water.ambient", AlphaEraSounds.WATER),
        Map.entry("minecraft:entity.generic.splash", AlphaEraSounds.WATER),
        Map.entry("minecraft:entity.player.splash", AlphaEraSounds.WATER),
        Map.entry("minecraft:entity.player.swim", AlphaEraSounds.SWIM),
        Map.entry("minecraft:block.lava.ambient", AlphaEraSounds.LAVA),
        Map.entry("minecraft:ambient.cave", AlphaEraSounds.CAVE),
        Map.entry("minecraft:weather.rain", AlphaEraSounds.RAIN),
        Map.entry("minecraft:entity.lightning_bolt.thunder", AlphaEraSounds.THUNDER),
        Map.entry("minecraft:entity.chicken.ambient", AlphaEraSounds.CHICKEN),
        Map.entry("minecraft:entity.chicken.hurt", AlphaEraSounds.CHICKEN),
        Map.entry("minecraft:entity.chicken.death", AlphaEraSounds.CHICKEN),
        Map.entry("minecraft:entity.cow.ambient", AlphaEraSounds.COW),
        Map.entry("minecraft:entity.cow.hurt", AlphaEraSounds.COW),
        Map.entry("minecraft:entity.cow.death", AlphaEraSounds.COW),
        Map.entry("minecraft:entity.pig.ambient", AlphaEraSounds.PIG),
        Map.entry("minecraft:entity.pig.hurt", AlphaEraSounds.PIG),
        Map.entry("minecraft:entity.pig.death", AlphaEraSounds.PIG),
        Map.entry("minecraft:entity.sheep.ambient", AlphaEraSounds.SHEEP),
        Map.entry("minecraft:entity.sheep.hurt", AlphaEraSounds.SHEEP),
        Map.entry("minecraft:entity.sheep.death", AlphaEraSounds.SHEEP),
        Map.entry("minecraft:entity.wolf.ambient", AlphaEraSounds.WOLF),
        Map.entry("minecraft:entity.wolf.hurt", AlphaEraSounds.WOLF),
        Map.entry("minecraft:entity.wolf.death", AlphaEraSounds.WOLF),
        Map.entry("minecraft:entity.zombie.ambient", AlphaEraSounds.ZOMBIE),
        Map.entry("minecraft:entity.zombie.hurt", AlphaEraSounds.ZOMBIE),
        Map.entry("minecraft:entity.zombie.death", AlphaEraSounds.ZOMBIE),
        Map.entry("minecraft:entity.skeleton.ambient", AlphaEraSounds.SKELETON),
        Map.entry("minecraft:entity.skeleton.hurt", AlphaEraSounds.SKELETON),
        Map.entry("minecraft:entity.skeleton.death", AlphaEraSounds.SKELETON),
        Map.entry("minecraft:entity.spider.ambient", AlphaEraSounds.SPIDER),
        Map.entry("minecraft:entity.spider.hurt", AlphaEraSounds.SPIDER),
        Map.entry("minecraft:entity.spider.death", AlphaEraSounds.SPIDER),
        Map.entry("minecraft:entity.creeper.primed", AlphaEraSounds.CREEPER),
        Map.entry("minecraft:entity.creeper.hurt", AlphaEraSounds.CREEPER),
        Map.entry("minecraft:entity.creeper.death", AlphaEraSounds.CREEPER),
        Map.entry("minecraft:entity.ghast.ambient", AlphaEraSounds.GHAST),
        Map.entry("minecraft:entity.ghast.scream", AlphaEraSounds.GHAST),
        Map.entry("minecraft:entity.ghast.hurt", AlphaEraSounds.GHAST),
        Map.entry("minecraft:entity.enderman.ambient", AlphaEraSounds.ENDERMAN),
        Map.entry("minecraft:entity.enderman.scream", AlphaEraSounds.ENDERMAN),
        Map.entry("minecraft:entity.villager.ambient", AlphaEraSounds.VILLAGER),
        Map.entry("minecraft:entity.villager.hurt", AlphaEraSounds.VILLAGER)
    );

    @SubscribeEvent
    public static void replaceClassicSounds(PlayLevelSoundEvent event) {
        if (!com.relaymann.theoutlands.OutlandsConfig.ALPHA_SOUNDS_ENABLED.get()) return;
        var sound = event.getSound();
        if (sound == null) return;
        String id = sound.unwrapKey().map(key -> key.location().toString()).orElse("");
        Holder<SoundEvent> replacement = REPLACEMENTS.get(id);
        if (replacement != null) event.setSound(replacement);
    }

    private AlphaSoundCompatibilityEvents() {}
}
