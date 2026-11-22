package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundSource;
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
        double strength = combinedStrength(player);
        if (strength <= 0.0D) return;
        SoundInstance sound = event.getOriginalSound();
        SoundSource source = sound.getSource();
        if (source != SoundSource.AMBIENT && source != SoundSource.WEATHER) return;
        float scale = (float) (1.0D - strength * 0.72D);
        event.setSound(sound instanceof TickableSoundInstance ticking
                ? new ScaledTickableSoundInstance(ticking, scale)
                : new ScaledSoundInstance(sound, scale));
    }

    /** Weather, biome, depth and nearby territorial presence gently layer onto the existing Fear fog. */
    private static double environmentalStrength(LocalPlayer player) {
        if (player == null || player.level() == null || !player.isAlive()
                || player.isCreative() || player.isSpectator()) return 0.0D;
        var level = player.level();
        var biomeKey = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .getKey(level.getBiome(player.blockPosition()).value());
        String biome = biomeKey == null ? "" : biomeKey.getPath();
        double biomeFactor = (biome.contains("forest") || biome.contains("taiga")
                || biome.contains("swamp") || biome.contains("dark_forest")) ? 0.16D : 0.0D;
        double weather = level.getRainLevel(1.0F) * 0.12D + level.getThunderLevel(1.0F) * 0.10D;
        double depth = player.getY() < 48.0D ? Math.min(0.22D, (48.0D - player.getY()) / 220.0D) : 0.0D;
        double night = level.getSkyDarken() > 8 ? 0.07D : 0.0D;
        return Math.min(0.34D, biomeFactor + weather + depth + night);
    }

    private static double combinedStrength(LocalPlayer player) {
        return Math.min(0.82D, territoryStrength(player) + environmentalStrength(player));
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || event.getCamera().getFluidInCamera() != FogType.NONE) return;
        double strength = territoryStrength(player);
        if (strength <= 0.0D) return;
        float blend = (float) (0.24D + strength * 0.18D);
        event.setRed(event.getRed() * (1.0F - blend) + 0.035F * blend);
        event.setGreen(event.getGreen() * (1.0F - blend) + 0.045F * blend);
        event.setBlue(event.getBlue() * (1.0F - blend) + 0.055F * blend);
        // A very brief, low-amplitude light instability in dark, threatening places.
        if (player.level().getSkyDarken() > 8 && player.level().getGameTime() % 173L < 2L
                && player.level().getMaxLocalRawBrightness(player.blockPosition()) > 7) {
            float flicker = 0.975F + (float) ((player.blockPosition().asLong() & 3L) * 0.004D);
            event.setRed(event.getRed() * flicker);
            event.setGreen(event.getGreen() * flicker);
            event.setBlue(event.getBlue() * flicker);
        }
    }

    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || event.getType() != FogType.NONE) return;
        double strength = territoryStrength(player);
        if (strength <= 0.0D) return;
        event.scaleFarPlaneDistance((float) (1.0D - strength * 0.32D));
        event.scaleNearPlaneDistance((float) (1.0D - strength * 0.12D));
    }
}
