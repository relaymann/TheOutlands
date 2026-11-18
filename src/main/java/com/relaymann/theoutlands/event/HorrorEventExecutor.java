package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.horror.HorrorDecision;
import com.relaymann.theoutlands.horror.HorrorEventCatalog;
import com.relaymann.theoutlands.horror.HorrorMemory;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/** Executes all registered director events without altering player-built blocks. */
public final class HorrorEventExecutor {
    private HorrorEventExecutor() {}

    public static void execute(ServerPlayer player, HorrorDecision decision) {
        if (decision == null || !decision.hasSelectedEvent()
                || !HorrorEventCatalog.ids().contains(decision.eventId())) return;
        HorrorMemory.recordEncounter(player, decision.eventId());
        var level = player.serverLevel();
        var random = player.getRandom();
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = 2.0D + random.nextDouble() * 3.0D;
        double x = player.getX() + Math.cos(angle) * radius;
        double z = player.getZ() + Math.sin(angle) * radius;
        double y = player.getY() + 0.2D;

        switch (decision.eventId()) {
            case "ambient_displacement" -> {
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.55F, 0.65F);
                particles(player, ParticleTypes.SMOKE, x, y, z, 5, 0.25D, 0.35D, 0.25D, 0.005D);
            }
            case "false_footsteps" -> {
                sound(player, SoundEvents.GRASS_STEP, SoundSource.BLOCKS, 0.8F, 0.78F);
                sound(player, SoundEvents.GRAVEL_STEP, SoundSource.BLOCKS, 0.45F, 0.86F);
            }
            case "distant_figure" -> {
                spawnNullWatcher(player);
                particles(player, ParticleTypes.ASH, x, y + 1.0D, z, 10, 0.35D, 0.8D, 0.35D, 0.01D);
            }
            case "unexplained_message" -> player.sendSystemMessage(Component.literal("§8[???] §7You have been here before."));
            case "wrong_sounds" -> {
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.4F, 1.7F);
                sound(player, SoundEvents.GRAVEL_STEP, SoundSource.BLOCKS, 0.3F, 0.55F);
                player.displayClientMessage(Component.literal("The sounds are coming from the wrong places."), true);
            }
            case "observer" -> {
                spawnNullWatcher(player);
                particles(player, ParticleTypes.ASH, x, y + 1.0D, z, 6, 0.2D, 0.6D, 0.2D, 0.0D);
            }
            case "environmental_distortion" -> {
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.9F, 0.45F);
                particles(player, ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 24, 0.7D, 0.9D, 0.7D, 0.025D);
                particles(player, ParticleTypes.SMOKE, x, y, z, 12, 0.5D, 0.4D, 0.5D, 0.01D);
            }
            case "missing_terrain" -> {
                particles(player, ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 0.8D, player.getZ(), 32, 0.8D, 0.7D, 0.8D, 0.035D);
                player.displayClientMessage(Component.literal("For a moment, something that should be here is not."), true);
            }
            case "fake_structure" -> {
                for (int i = 0; i < 20; i++) {
                    double theta = Math.PI * 2.0D * i / 20.0D;
                    particles(player, ParticleTypes.REVERSE_PORTAL, player.getX() + Math.cos(theta) * 4.0D,
                            player.getY() + 0.1D, player.getZ() + Math.sin(theta) * 4.0D, 1, 0.0D, 0.15D, 0.0D, 0.0D);
                }
                player.displayClientMessage(Component.literal("Something was standing here."), true);
            }
            case "hostile_warning" -> {
                sound(player, SoundEvents.WARDEN_NEARBY_CLOSER, SoundSource.HOSTILE, 0.8F, 0.7F);
                player.displayClientMessage(Component.literal("Something noticed you."), true);
                particles(player, ParticleTypes.SOUL, x, y + 0.5D, z, 8, 0.25D, 0.45D, 0.25D, 0.01D);
            }
            case "the_return" -> {
                spawnNullWatcher(player);
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.7F, 0.42F);
                player.displayClientMessage(Component.literal("Something you left behind has returned."), true);
            }
            case "repeated_location" -> {
                player.displayClientMessage(Component.literal("You know this place. You have never been here."), true);
                particles(player, ParticleTypes.ASH, x, y + 0.3D, z, 9, 0.35D, 0.3D, 0.35D, 0.002D);
            }
            case "silent_world" -> {
                // True ambient muting needs a client effect; this is the safe server-side cue.
                player.displayClientMessage(Component.literal("The world falls quiet."), true);
                particles(player, ParticleTypes.SMOKE, x, y + 1.0D, z, 3, 0.1D, 0.2D, 0.1D, 0.0D);
            }
            case "entity_imitation" -> {
                Mob mimic = level.getEntitiesOfClass(Mob.class, new AABB(player.blockPosition()).inflate(16.0D), Mob::isAlive)
                        .stream().findFirst().orElse(null);
                if (mimic != null) mimic.getLookControl().setLookAt(player, 30.0F, 30.0F);
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.45F, 1.25F);
                player.displayClientMessage(Component.literal("It moved when you moved."), true);
            }
            case "false_death" -> player.displayClientMessage(Component.literal("You died."), false);
            case "distant_scream" -> {
                sound(player, SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 0.32F, 0.62F);
                particles(player, ParticleTypes.SOUL, x, y + 1.0D, z, 4, 0.2D, 0.5D, 0.2D, 0.0D);
            }
            case "false_player" -> {
                // Provisional false-player silhouette; a dedicated fake-player renderer remains future work.
                spawnNullWatcher(player);
                player.displayClientMessage(Component.literal("Someone is standing where you were looking."), true);
            }
            case "fear_whisper" -> {
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.3F, 0.55F);
                player.displayClientMessage(Component.literal("§5[?] §7I remember you."), true);
            }
            case "fear_presence" -> {
                particles(player, ParticleTypes.REVERSE_PORTAL, x, y + 1.0D, z, 18, 0.25D, 0.8D, 0.25D, 0.01D);
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.45F, 0.4F);
            }
            case "fear_call" -> {
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.5F, 0.35F);
                player.displayClientMessage(Component.literal("Something is calling you further out."), true);
            }
            case "direct_intervention" -> {
                sound(player, SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 0.65F, 0.55F);
                player.sendSystemMessage(Component.literal("§8[Null] §7I know where you are."));
                particles(player, ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 36, 0.8D, 1.0D, 0.8D, 0.04D);
            }
            default -> { }
        }
    }

    private static void spawnNullWatcher(ServerPlayer player) {
        if (OutlandsConfig.NULL_SIGHTINGS_ENABLED.get()) NullWatcherManager.spawn(player);
    }

    private static void particles(ServerPlayer player, net.minecraft.core.particles.ParticleOptions type,
                                  double x, double y, double z, int count, double dx, double dy, double dz, double speed) {
        player.serverLevel().sendParticles(player, type, true, x, y, z, count, dx, dy, dz, speed);
    }

    private static void sound(ServerPlayer player, SoundEvent sound, SoundSource source, float volume, float pitch) {
        if (OutlandsConfig.HORROR_AUDIO_ENABLED.get()) player.playNotifySound(sound, source, volume, pitch);
    }
}
