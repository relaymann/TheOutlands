package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.horror.HorrorDecision;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** Executes approved events on the logical server without changing player-built blocks. */
public final class HorrorEventExecutor {
    private HorrorEventExecutor() {}
    public static void execute(ServerPlayer player, HorrorDecision decision) {
        if (decision == null || !decision.hasSelectedEvent()) return;
        var level = player.serverLevel();
        var random = player.getRandom();
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = 2.0D + random.nextDouble() * 3.0D;
        double x = player.getX() + Math.cos(angle) * radius;
        double z = player.getZ() + Math.sin(angle) * radius;
        double y = player.getY() + 0.2D;
        switch (decision.eventId()) {
            case "ambient_displacement" -> {
                player.playNotifySound(SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.55F, 0.65F);
                level.sendParticles(player, ParticleTypes.SMOKE, true, x, y, z, 5, 0.25D, 0.35D, 0.25D, 0.005D);
            }
            case "false_footsteps" -> {
                player.playNotifySound(SoundEvents.GRASS_STEP, SoundSource.BLOCKS, 0.8F, 0.78F);
                player.playNotifySound(SoundEvents.GRAVEL_STEP, SoundSource.BLOCKS, 0.45F, 0.86F);
            }
            case "distant_figure" -> {
                NullWatcherManager.spawn(player);
                level.sendParticles(player, ParticleTypes.ASH, true, x, y + 1.0D, z, 10, 0.35D, 0.8D, 0.35D, 0.01D);
            }
            case "environmental_distortion" -> {
                player.playNotifySound(SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.9F, 0.45F);
                level.sendParticles(player, ParticleTypes.PORTAL, true, player.getX(), player.getY() + 1.0D,
                        player.getZ(), 24, 0.7D, 0.9D, 0.7D, 0.025D);
                level.sendParticles(player, ParticleTypes.SMOKE, true, x, y, z, 12, 0.5D, 0.4D, 0.5D, 0.01D);
            }
            case "hostile_warning" -> {
                player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSER, SoundSource.HOSTILE, 0.8F, 0.7F);
                player.displayClientMessage(Component.literal("Something noticed you."), true);
                level.sendParticles(player, ParticleTypes.SOUL, true, x, y + 0.5D, z, 8, 0.25D, 0.45D, 0.25D, 0.01D);
            }
            case "direct_intervention" -> {
                player.playNotifySound(SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 0.65F, 0.55F);
                player.sendSystemMessage(Component.literal("§8[Null] §7I know where you are."));
                level.sendParticles(player, ParticleTypes.PORTAL, true, player.getX(), player.getY() + 1.0D,
                        player.getZ(), 36, 0.8D, 1.0D, 0.8D, 0.04D);
            }
            default -> { /* Unknown IDs fail closed. */ }
        }
    }
}
