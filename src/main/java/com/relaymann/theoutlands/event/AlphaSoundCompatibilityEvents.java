package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.sound.AlphaEraSounds;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

import java.util.Map;

/**
 * Client sound-engine replacement catches both world-played and local-only
 * sounds (notably block hit/break sounds) without replacing vanilla sounds.json.
 */
@EventBusSubscriber(modid = "theoutlands", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class AlphaSoundCompatibilityEvents {
    private static final Map<String, AlphaEraSoundRef> REPLACEMENTS = Map.ofEntries(
        Map.entry("minecraft:block.stone.step", new AlphaEraSoundRef(() -> AlphaEraSounds.STEP_STONE.get())),
        Map.entry("minecraft:block.grass.step", new AlphaEraSoundRef(() -> AlphaEraSounds.STEP_GRASS.get())),
        Map.entry("minecraft:block.gravel.step", new AlphaEraSoundRef(() -> AlphaEraSounds.STEP_GRAVEL.get())),
        Map.entry("minecraft:block.sand.step", new AlphaEraSoundRef(() -> AlphaEraSounds.STEP_SAND.get())),
        Map.entry("minecraft:block.snow.step", new AlphaEraSoundRef(() -> AlphaEraSounds.STEP_SNOW.get())),
        Map.entry("minecraft:block.wood.step", new AlphaEraSoundRef(() -> AlphaEraSounds.STEP_WOOD.get())),
        Map.entry("minecraft:block.wool.step", new AlphaEraSoundRef(() -> AlphaEraSounds.STEP_CLOTH.get())),
        Map.entry("minecraft:block.ladder.step", new AlphaEraSoundRef(() -> AlphaEraSounds.STEP_LADDER.get())),
        Map.entry("minecraft:block.stone.hit", new AlphaEraSoundRef(() -> AlphaEraSounds.DIG_STONE.get())),
        Map.entry("minecraft:block.stone.break", new AlphaEraSoundRef(() -> AlphaEraSounds.DIG_STONE.get())),
        Map.entry("minecraft:block.grass.hit", new AlphaEraSoundRef(() -> AlphaEraSounds.DIG_GRASS.get())),
        Map.entry("minecraft:block.grass.break", new AlphaEraSoundRef(() -> AlphaEraSounds.DIG_GRASS.get())),
        Map.entry("minecraft:block.gravel.hit", new AlphaEraSoundRef(() -> AlphaEraSounds.DIG_GRAVEL.get())),
        Map.entry("minecraft:block.gravel.break", new AlphaEraSoundRef(() -> AlphaEraSounds.DIG_GRAVEL.get())),
        Map.entry("minecraft:block.sand.hit", new AlphaEraSoundRef(() -> AlphaEraSounds.DIG_SAND.get())),
        Map.entry("minecraft:block.sand.break", new AlphaEraSoundRef(() -> AlphaEraSounds.DIG_SAND.get())),
        Map.entry("minecraft:block.snow.hit", new AlphaEraSoundRef(() -> AlphaEraSounds.DIG_SNOW.get())),
        Map.entry("minecraft:block.snow.break", new AlphaEraSoundRef(() -> AlphaEraSounds.DIG_SNOW.get())),
        Map.entry("minecraft:block.wood.hit", new AlphaEraSoundRef(() -> AlphaEraSounds.DIG_WOOD.get())),
        Map.entry("minecraft:block.wood.break", new AlphaEraSoundRef(() -> AlphaEraSounds.DIG_WOOD.get())),
        Map.entry("minecraft:block.wool.hit", new AlphaEraSoundRef(() -> AlphaEraSounds.DIG_CLOTH.get())),
        Map.entry("minecraft:block.wool.break", new AlphaEraSoundRef(() -> AlphaEraSounds.DIG_CLOTH.get())),
        Map.entry("minecraft:block.wooden_door.open", new AlphaEraSoundRef(() -> AlphaEraSounds.DOOR_OPEN.get())),
        Map.entry("minecraft:block.wooden_door.close", new AlphaEraSoundRef(() -> AlphaEraSounds.DOOR_CLOSE.get())),
        Map.entry("minecraft:block.iron_door.open", new AlphaEraSoundRef(() -> AlphaEraSounds.DOOR_OPEN.get())),
        Map.entry("minecraft:block.iron_door.close", new AlphaEraSoundRef(() -> AlphaEraSounds.DOOR_CLOSE.get())),
        Map.entry("minecraft:entity.player.hurt", new AlphaEraSoundRef(() -> AlphaEraSounds.PLAYER_HURT.get())),
        Map.entry("minecraft:entity.player.small_fall", new AlphaEraSoundRef(() -> AlphaEraSounds.FALL_SMALL.get())),
        Map.entry("minecraft:entity.player.big_fall", new AlphaEraSoundRef(() -> AlphaEraSounds.FALL_BIG.get())),
        Map.entry("minecraft:block.water.ambient", new AlphaEraSoundRef(() -> AlphaEraSounds.WATER.get())),
        Map.entry("minecraft:entity.generic.splash", new AlphaEraSoundRef(() -> AlphaEraSounds.WATER.get())),
        Map.entry("minecraft:entity.player.splash", new AlphaEraSoundRef(() -> AlphaEraSounds.WATER.get())),
        Map.entry("minecraft:entity.player.swim", new AlphaEraSoundRef(() -> AlphaEraSounds.SWIM.get())),
        Map.entry("minecraft:block.lava.ambient", new AlphaEraSoundRef(() -> AlphaEraSounds.LAVA.get())),
        Map.entry("minecraft:ambient.cave", new AlphaEraSoundRef(() -> AlphaEraSounds.CAVE.get())),
        Map.entry("minecraft:weather.rain", new AlphaEraSoundRef(() -> AlphaEraSounds.RAIN.get())),
        Map.entry("minecraft:entity.lightning_bolt.thunder", new AlphaEraSoundRef(() -> AlphaEraSounds.THUNDER.get())),
        Map.entry("minecraft:entity.chicken.ambient", new AlphaEraSoundRef(() -> AlphaEraSounds.CHICKEN.get())),
        Map.entry("minecraft:entity.cow.ambient", new AlphaEraSoundRef(() -> AlphaEraSounds.COW.get())),
        Map.entry("minecraft:entity.pig.ambient", new AlphaEraSoundRef(() -> AlphaEraSounds.PIG.get())),
        Map.entry("minecraft:entity.sheep.ambient", new AlphaEraSoundRef(() -> AlphaEraSounds.SHEEP.get())),
        Map.entry("minecraft:entity.wolf.ambient", new AlphaEraSoundRef(() -> AlphaEraSounds.WOLF.get())),
        Map.entry("minecraft:entity.zombie.ambient", new AlphaEraSoundRef(() -> AlphaEraSounds.ZOMBIE.get())),
        Map.entry("minecraft:entity.skeleton.ambient", new AlphaEraSoundRef(() -> AlphaEraSounds.SKELETON.get())),
        Map.entry("minecraft:entity.spider.ambient", new AlphaEraSoundRef(() -> AlphaEraSounds.SPIDER.get())),
        Map.entry("minecraft:entity.creeper.primed", new AlphaEraSoundRef(() -> AlphaEraSounds.CREEPER.get())),
        Map.entry("minecraft:entity.ghast.ambient", new AlphaEraSoundRef(() -> AlphaEraSounds.GHAST.get())),
        Map.entry("minecraft:entity.enderman.ambient", new AlphaEraSoundRef(() -> AlphaEraSounds.ENDERMAN.get())),
        Map.entry("minecraft:entity.villager.ambient", new AlphaEraSoundRef(() -> AlphaEraSounds.VILLAGER.get()))
    );

    @SubscribeEvent
    public static void replaceClassicSounds(PlaySoundEvent event) {
        if (!OutlandsConfig.ALPHA_SOUNDS_ENABLED.get()) return;
        SoundInstance original = event.getOriginalSound();
        AlphaEraSoundRef replacement = REPLACEMENTS.get(original.getLocation().toString());
        if (replacement == null) return;
        event.setSound(new SimpleSoundInstance(replacement.sound().get(),
                original.getSource(), original.getVolume(), original.getPitch(),
                RandomSource.create(), original.getX(), original.getY(), original.getZ()));
    }

    private record AlphaEraSoundRef(java.util.function.Supplier<SoundEvent> sound) {}
    private AlphaSoundCompatibilityEvents() {}
}
