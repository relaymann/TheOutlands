package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.horror.AlmightyTerritory;
import com.relaymann.theoutlands.horror.HorrorDecision;
import com.relaymann.theoutlands.horror.HorrorEventCatalog;
import com.relaymann.theoutlands.horror.HorrorMemory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/** Executes player-local horror cues without narrating events or changing server-world blocks. */
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
            case "wall_knock" -> {
                double a=random.nextDouble()*Math.PI*2.0D; double d=4.0D+random.nextDouble()*5.0D;
                double nx=player.getX()+Math.cos(a)*d, nz=player.getZ()+Math.sin(a)*d;
                positionalSound(player, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 0.34F, 0.65F+random.nextFloat()*0.3F, nx, player.getY()+1.0D, nz);
                if(random.nextBoolean()) positionalSound(player, SoundEvents.WOODEN_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.2F, 0.8F, nx+1.0D, player.getY(), nz);
            }
            case "behind_steps" -> {
                double yaw=Math.toRadians(player.getYRot()); double bx=player.getX()-Math.sin(yaw)*5.0D, bz=player.getZ()+Math.cos(yaw)*5.0D;
                positionalSound(player, SoundEvents.GRAVEL_STEP, SoundSource.BLOCKS, 0.38F, 0.7F, bx, player.getY(), bz);
                positionalSound(player, SoundEvents.GRASS_STEP, SoundSource.BLOCKS, 0.22F, 0.6F, bx-0.8D, player.getY(), bz+0.8D);
            }
            case "breathing_presence" -> {
                double yaw=Math.toRadians(player.getYRot()); double bx=player.getX()+Math.sin(yaw)*2.0D, bz=player.getZ()-Math.cos(yaw)*2.0D;
                positionalSound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.22F, 0.5F, bx, player.getY()+1.0D, bz);
                particles(player, ParticleTypes.ASH, bx, player.getY()+1.4D, bz, 2, 0.04D, 0.08D, 0.04D, 0.0D);
            }
            case "door_afterimage" -> ClientAfterimageManager.showDoorAfterimage(player);
            case "false_wakeup" -> {
                ClientAfterimageManager.showTerrainAfterimage(player);
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.2F, 0.45F);
                particles(player, ParticleTypes.SMOKE, player.getX(), player.getY()+1.0D, player.getZ(), 6, 0.2D, 0.3D, 0.2D, 0.0D);
            }
            case "mimic_activity" -> {
                double a=random.nextDouble()*Math.PI*2.0D; double d=8.0D+random.nextDouble()*12.0D;
                double mx=player.getX()+Math.cos(a)*d, mz=player.getZ()+Math.sin(a)*d;
                positionalSound(player, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.18F, 0.65F, mx, player.getY(), mz);
                positionalSound(player, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 0.16F, 0.7F, mx+2.0D, player.getY(), mz+1.0D);
            }
            case "ambient_displacement" -> {
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.42F, 0.65F);
                particles(player, ParticleTypes.SMOKE, x, y, z, 3, 0.18D, 0.25D, 0.18D, 0.002D);
            }
            case "false_footsteps" -> {
                double stepAngle = random.nextDouble() * Math.PI * 2.0D;
                double stepDistance = 5.0D + random.nextDouble() * 4.0D;
                double stepX = player.getX() + Math.cos(stepAngle) * stepDistance;
                double stepZ = player.getZ() + Math.sin(stepAngle) * stepDistance;
                positionalSound(player, SoundEvents.GRASS_STEP, SoundSource.BLOCKS, 0.48F, 0.78F,
                        stepX, player.getY(), stepZ);
                positionalSound(player, SoundEvents.GRAVEL_STEP, SoundSource.BLOCKS, 0.22F, 0.86F,
                        stepX + Math.cos(stepAngle) * 1.8D, player.getY(), stepZ + Math.sin(stepAngle) * 1.8D);
            }
            case "distant_figure", "observer", "false_player" -> {
                spawnNullWatcher(player);
                ClientAfterimageManager.showSilhouette(player);
                particles(player, ParticleTypes.ASH, x, y + 1.0D, z, 4, 0.2D, 0.55D, 0.2D, 0.002D);
            }
            case "unexplained_message" -> {
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.18F, 1.7F);
                particles(player, ParticleTypes.ASH, x, y + 0.5D, z, 2, 0.12D, 0.2D, 0.12D, 0.0D);
            }
            case "wrong_sounds" -> {
                double behindX = player.getX() + Math.sin(Math.toRadians(player.getYRot())) * 7.0D;
                double behindZ = player.getZ() - Math.cos(Math.toRadians(player.getYRot())) * 7.0D;
                positionalSound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.24F, 1.7F,
                        behindX, player.getY() + 1.0D, behindZ);
                positionalSound(player, SoundEvents.GRAVEL_STEP, SoundSource.BLOCKS, 0.2F, 0.55F,
                        behindX + 1.5D, player.getY(), behindZ - 1.5D);
            }
            case "environmental_distortion" -> {
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.5F, 0.45F);
                particles(player, ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 10, 0.5D, 0.65D, 0.5D, 0.008D);
                particles(player, ParticleTypes.SMOKE, x, y, z, 5, 0.3D, 0.3D, 0.3D, 0.002D);
            }
            case "missing_terrain", "fake_structure", "repeated_location" -> {
                ClientAfterimageManager.showTerrainAfterimage(player);
                particles(player, ParticleTypes.REVERSE_PORTAL, x, y + 0.2D, z, 5, 0.25D, 0.35D, 0.25D, 0.003D);
            }
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
                double screamAngle = random.nextDouble() * Math.PI * 2.0D;
                double screamDistance = 26.0D + random.nextDouble() * 14.0D;
                double screamX = player.getX() + Math.cos(screamAngle) * screamDistance;
                double screamZ = player.getZ() + Math.sin(screamAngle) * screamDistance;
                positionalSound(player, SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 0.2F, 0.62F,
                        screamX, player.getY() + random.nextDouble() * 5.0D - 2.0D, screamZ);
                particles(player, ParticleTypes.SOUL, x, y + 1.0D, z, 2, 0.12D, 0.3D, 0.12D, 0.0D);
            }
            case "fear_whisper" -> {
                double whisperX = player.getX() + Math.sin(Math.toRadians(player.getYRot())) * 1.4D;
                double whisperZ = player.getZ() - Math.cos(Math.toRadians(player.getYRot())) * 1.4D;
                positionalSound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.18F, 0.55F,
                        whisperX, player.getY() + 1.2D, whisperZ);
            }
            case "fear_presence" -> {
                FearEncounterManager.showPresence(player);
                particles(player, ParticleTypes.REVERSE_PORTAL, x, y + 1.0D, z, 8, 0.18D, 0.5D, 0.18D, 0.004D);
                sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.28F, 0.4F);
            }
            case "fear_call" -> {
                double callAngle = Math.toRadians(player.getYRot());
                double callX = player.getX() - Math.sin(callAngle) * 30.0D;
                double callZ = player.getZ() + Math.cos(callAngle) * 30.0D;
                positionalSound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.22F, 0.35F,
                        callX, player.getY() + 2.0D, callZ);
            }
            case "direct_intervention" -> {
                sound(player, SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 0.3F, 0.55F);
                // Keep horror diegetic: do not explain or narrate events in chat.
                particles(player, ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 12, 0.45D, 0.65D, 0.45D, 0.01D);
            }
            case "familiar_echo" -> executeFamiliarPlaceDistortion(player);
            case "world_inconsistency" -> {
                ClientAfterimageManager.showTerrainAfterimage(player);
                if (random.nextBoolean()) ClientAfterimageManager.showFalseStructure(player);
                positionalSound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT,
                        0.14F, 0.48F, x + 12.0D, y, z - 9.0D);
            }
            case "false_structure" -> {
                ClientAfterimageManager.showFalseStructure(player);
                positionalSound(player, SoundEvents.GRAVEL_STEP, SoundSource.BLOCKS, 0.16F, 0.58F, x, y, z);
            }
            case "paranoia_echo" -> {
                ClientAfterimageManager.showSilhouette(player);
                double back = Math.toRadians(player.getYRot());
                positionalSound(player, SoundEvents.GRASS_STEP, SoundSource.BLOCKS, 0.18F, 0.7F,
                        player.getX() + Math.sin(back) * 5.5D, player.getY(),
                        player.getZ() - Math.cos(back) * 5.5D);
            }
            case "null_apostle_sign" -> {
                NullApostleManager.spawn(player);
                ClientAfterimageManager.showSilhouette(player);
                ClientAfterimageManager.showFalseStructure(player);
                positionalSound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT,
                        0.11F, 0.42F, x - 18.0D, y + 2.0D, z + 18.0D);
            }
            case "almighty_influence" -> executeAlmightyInfluence(player);
            default -> { }
        }
    }

    public static void executeFamiliarPlaceDistortion(ServerPlayer player) {
        var level = player.serverLevel();
        BlockPos chosen = null;
        var remembered = HorrorMemory.rememberedLocations(player);
        for (int i = remembered.size() - 1; i >= 0; i--) {
            BlockPos candidate = remembered.get(i).offset(16, 0, 16);
            if (level.hasChunkAt(candidate) && player.position().distanceToSqr(candidate.getX() + 16.5D,
                    candidate.getY() + 0.5D, candidate.getZ() + 16.5D) <= 48.0D * 48.0D) {
                chosen = candidate;
                break;
            }
        }
        if (chosen != null) {
            double py = Math.max(level.getMinBuildHeight() + 1,
                    Math.min(level.getMaxBuildHeight() - 2, chosen.getY() + 1.0D));
            particles(player, ParticleTypes.ASH, chosen.getX() + 0.5D, py, chosen.getZ() + 0.5D,
                    5, 0.18D, 0.35D, 0.18D, 0.001D);
        }
        sound(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.16F, 0.72F);
    }

    private static void executeAlmightyInfluence(ServerPlayer player) {
        double borderDepth = AlmightyTerritory.distanceInsideBorder(player.serverLevel(), player.getX(), player.getZ());
        if (!Double.isFinite(borderDepth)) return;
        sound(player, SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 0.12F, 0.35F);
        if (borderDepth > OutlandsConfig.ALMIGHTY_TERRITORY_DEPTH.get()) {
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
    private static void positionalSound(ServerPlayer player, SoundEvent sound, SoundSource source,
                                        float volume, float pitch, double x, double y, double z) {
        if (!OutlandsConfig.HORROR_AUDIO_ENABLED.get()) return;
        player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                source, x, y, z, volume, pitch, player.getRandom().nextLong()));
    }
}
