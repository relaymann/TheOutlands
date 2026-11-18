package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.horror.AlmightyTerritory;
import com.relaymann.theoutlands.horror.HorrorDecision;
import com.relaymann.theoutlands.horror.HorrorEventCatalog;
import com.relaymann.theoutlands.horror.HorrorMemory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/** Executes player-local horror cues without narrating events or changing player-built blocks. */
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
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.42F, 0.65F);
                particles(player, ParticleTypes.SMOKE, x, y, z, 3, 0.18D, 0.25D, 0.18D, 0.002D);
            }
            case "false_footsteps" -> {
                sound(player, SoundEvents.GRASS_STEP, SoundSource.BLOCKS, 0.55F, 0.78F);
                sound(player, SoundEvents.GRAVEL_STEP, SoundSource.BLOCKS, 0.28F, 0.86F);
            }
            case "distant_figure", "observer", "false_player" -> {
                spawnNullWatcher(player);
                particles(player, ParticleTypes.ASH, x, y + 1.0D, z, 4, 0.2D, 0.55D, 0.2D, 0.002D);
            }
            case "unexplained_message" -> {
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.18F, 1.7F);
                particles(player, ParticleTypes.ASH, x, y + 0.5D, z, 2, 0.12D, 0.2D, 0.12D, 0.0D);
            }
            case "wrong_sounds" -> {
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.28F, 1.7F);
                sound(player, SoundEvents.GRAVEL_STEP, SoundSource.BLOCKS, 0.2F, 0.55F);
            }
            case "environmental_distortion" -> {
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.5F, 0.45F);
                particles(player, ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 10, 0.5D, 0.65D, 0.5D, 0.008D);
                particles(player, ParticleTypes.SMOKE, x, y, z, 5, 0.3D, 0.3D, 0.3D, 0.002D);
            }
            case "missing_terrain", "fake_structure", "repeated_location" ->
                    particles(player, ParticleTypes.REVERSE_PORTAL, x, y + 0.2D, z, 5, 0.25D, 0.35D, 0.25D, 0.003D);
            case "the_return" -> {
                spawnNullWatcher(player);
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.38F, 0.42F);
            }
            case "silent_world", "environmental_hush" ->
                    particles(player, ParticleTypes.SMOKE, x, y + 1.0D, z, 2, 0.08D, 0.12D, 0.08D, 0.0D);
            case "entity_imitation" -> {
                Mob mimic = level.getEntitiesOfClass(Mob.class, new AABB(player.blockPosition()).inflate(16.0D), Mob::isAlive)
                        .stream().findFirst().orElse(null);
                if (mimic != null) mimic.getLookControl().setLookAt(player, 30.0F, 30.0F);
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.25F, 1.25F);
            }
            case "false_death" -> sound(player, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.18F, 0.55F);
            case "distant_scream" -> {
                sound(player, SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 0.22F, 0.62F);
                particles(player, ParticleTypes.SOUL, x, y + 1.0D, z, 2, 0.12D, 0.3D, 0.12D, 0.0D);
            }
            case "fear_whisper" -> sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.2F, 0.55F);
            case "fear_presence" -> {
                FearEncounterManager.showPresence(player);
                particles(player, ParticleTypes.REVERSE_PORTAL, x, y + 1.0D, z, 8, 0.18D, 0.5D, 0.18D, 0.004D);
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.28F, 0.4F);
            }
            case "fear_call" -> sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.28F, 0.35F);
            case "direct_intervention" -> {
                sound(player, SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 0.3F, 0.55F);
                // Rare, intentional character dialogue; ordinary events never narrate themselves.
                player.sendSystemMessage(Component.literal("§8[Null] §7I know where you are."));
                particles(player, ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 12, 0.45D, 0.65D, 0.45D, 0.01D);
            }
            case "familiar_echo" -> executeFamiliarPlaceDistortion(player);
            case "almighty_influence" -> executeAlmightyInfluence(player);
            default -> { }
        }
    }

    /** Revisited locations leave a private, non-destructive visual/audio trace. */
    public static void executeFamiliarPlaceDistortion(ServerPlayer player) {
        var level = player.serverLevel();
        BlockPos chosen = null;
        var remembered = HorrorMemory.rememberedLocations(player);
        for (int i = remembered.size() - 1; i >= 0; i--) {
            BlockPos candidate = remembered.get(i).offset(16, 0, 16);
            if (level.hasChunkAt(candidate) && player.position().distanceToSqr(candidate.getX() + 16.5D, candidate.getY() + 0.5D, candidate.getZ() + 16.5D) <= 48.0D * 48.0D) {
                chosen = candidate;
                break;
            }
        }
        if (chosen != null) {
            double y = Math.max(level.getMinBuildHeight() + 1, Math.min(level.getMaxBuildHeight() - 2, chosen.getY() + 1.0D));
            particles(player, ParticleTypes.ASH, chosen.getX() + 0.5D, y, chosen.getZ() + 0.5D,
                    5, 0.18D, 0.35D, 0.18D, 0.001D);
        }
        sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.16F, 0.72F);
    }

    private static void executeAlmightyInfluence(ServerPlayer player) {
        double borderDepth = AlmightyTerritory.distanceInsideBorder(player.serverLevel(), player.getX(), player.getZ());
        if (!Double.isFinite(borderDepth)) return;
        sound(player, SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 0.12F, 0.35F);
        if (borderDepth > com.relaymann.theoutlands.OutlandsConfig.ALMIGHTY_TERRITORY_DEPTH.get()) {
            Mob influenced = player.serverLevel().getEntitiesOfClass(Mob.class,
                    new AABB(player.blockPosition()).inflate(24.0D), Mob::isAlive).stream().findFirst().orElse(null);
            if (influenced != null) {
                influenced.getNavigation().stop();
                influenced.getLookControl().setLookAt(player, 12.0F, 12.0F);
            }
        }
        particles(player, ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0D,
                player.getZ(), 8, 0.35D, 0.5D, 0.35D, 0.004D);
        if (borderDepth <= 0.0D) {
            particles(player, ParticleTypes.SMOKE, player.getX(), player.getY() + 0.5D,
                    player.getZ(), 4, 0.18D, 0.3D, 0.18D, 0.0D);
        }
        HorrorMemory.setLastAlmightySign(player, player.serverLevel().getGameTime());
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
