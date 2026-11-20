package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.level.Level;

/** Client-only Fear territory atmosphere. It never changes server state or other players' audio. */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, value = Dist.CLIENT)
public final class FearClientAtmosphere {
    private FearClientAtmosphere() {}

    private static double territoryStrength(LocalPlayer player) {
        if (player == null || player.level() == null || !player.isAlive() || player.isCreative() || player.isSpectator()
                || !player.level().dimension().equals(Level.OVERWORLD)
                || !OutlandsConfig.FEAR_AMBIENT_ATTENUATION.get()) return 0.0D;
        var key = player.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .getKey(player.level().getBiome(player.blockPosition()).value());
        if (key == null || !(key.getPath().contains("forest") || key.getPath().equals("fear_forest"))) return 0.0D;
        var spawn = player.level().getSharedSpawnPos();
        double distance = Math.hypot(player.getX() - (spawn.getX() + 0.5D),
                player.getZ() - (spawn.getZ() + 0.5D));
        double threshold = OutlandsConfig.FEAR_FULL_TERRITORY_DISTANCE.get();
        if (distance < threshold) return 0.0D;
        return Math.min(1.0D, 0.35D + (distance - threshold) / 120_000.0D);
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        double strength = territoryStrength(player);
        if (strength <= 0.0D) return;
        SoundInstance sound = event.getOriginalSound();
        SoundSource source = sound.getSource();
        if (source != SoundSource.AMBIENT && source != SoundSource.WEATHER) return;
        // Keep important player/entity sounds intact; only soften the world's ambient bed.
        float scale = (float) (1.0D - strength * 0.72D);
        event.setSound(sound instanceof TickableSoundInstance ticking
                ? new ScaledTickableSoundInstance(ticking, scale)
                : new ScaledSoundInstance(sound, scale));
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        LocalPlayer player = Minecraft.getInstance().player;
        double strength = territoryStrength(player);
        if (strength <= 0.0D) return;
        float blend = (float) (0.24D + strength * 0.18D);
        event.setRed(event.getRed() * (1.0F - blend) + 0.035F * blend);
        event.setGreen(event.getGreen() * (1.0F - blend) + 0.045F * blend);
        event.setBlue(event.getBlue() * (1.0F - blend) + 0.055F * blend);
    }

    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || event.getType() != FogType.NONE) return;

        // Alpha's Overworld horizon used a simple, early fade instead of the
        // much denser modern volumetric horizon. Keep the effect local to the
        // Overworld and leave water, lava, powder snow, and Nether/End fog alone.
        if (OutlandsConfig.ALPHA_VISUALS_ENABLED.get()
                && player.level().dimension().equals(Level.OVERWORLD)) {
            event.scaleFarPlaneDistance(0.86F);
            event.scaleNearPlaneDistance(0.72F);
        }

        // Fear's territory is a separate, stronger layer over the global retro
        // horizon. The vanilla fog event is deliberately not cancelled.
        double strength = territoryStrength(player);
        if (strength > 0.0D) {
            event.scaleFarPlaneDistance((float) (1.0D - strength * 0.32D));
            event.scaleNearPlaneDistance((float) (1.0D - strength * 0.12D));
        }
    }
}
