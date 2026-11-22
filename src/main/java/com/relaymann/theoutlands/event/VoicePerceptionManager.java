package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.entity.*;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Server-side voice perception. Only the normalized activity percentage,
 * whisper flag, sender UUID, and current Minecraft position are retained.
 * Raw microphone samples and conversations are never stored or interpreted.
 */
public final class VoicePerceptionManager {
    private static final int ACTIVE_PACKET_TTL = 14;
    private static final int MEMORY_TTL = 300;
    private static final Map<UUID, ReportedLevel> REPORTED_LEVELS = new HashMap<>();
    private static final Map<UUID, VoiceSignal> ACTIVE_VOICES = new HashMap<>();
    private static final Map<UUID, VoiceMemory> ENTITY_MEMORY = new HashMap<>();
    private static final Map<UUID, Long> PLAYER_EVENT_COOLDOWNS = new HashMap<>();

    private VoicePerceptionManager() {}

    public static void onClientLevel(ServerPlayer player, int level, boolean whispering) {
        if (!OutlandsConfig.VOICE_HORROR_ENABLED.get()) return;
        int clamped = Math.max(0, Math.min(100, level));
        REPORTED_LEVELS.put(player.getUUID(), new ReportedLevel(clamped, whispering,
                player.serverLevel().getGameTime()));
        VoiceSignal active = ACTIVE_VOICES.get(player.getUUID());
        if (active != null) {
            ACTIVE_VOICES.put(player.getUUID(), new VoiceSignal(player.position(),
                    player.serverLevel().dimension(), clamped, whispering,
                    player.serverLevel().getGameTime()));
        }
    }

    /** Called only after Simple Voice Chat confirms an actual microphone packet. */
    public static void onVoicePacket(ServerPlayer player, boolean whispering) {
        if (!OutlandsConfig.VOICE_HORROR_ENABLED.get()
                || !player.isAlive() || player.isCreative() || player.isSpectator()) return;
        long now = player.serverLevel().getGameTime();
        ReportedLevel reported = REPORTED_LEVELS.get(player.getUUID());
        int level = reported != null && now - reported.gameTime() <= 12L
                ? reported.level() : 34;
        boolean whisper = whispering || (reported != null && reported.whispering());
        if (whisper && level > 38) level = 38;
        ACTIVE_VOICES.put(player.getUUID(), new VoiceSignal(player.position(),
                player.serverLevel().dimension(), Math.max(12, level), whisper, now));
    }

    public static void tick(ServerLevel level) {
        if (!OutlandsConfig.VOICE_HORROR_ENABLED.get()) return;
        long now = level.getGameTime();
        ACTIVE_VOICES.entrySet().removeIf(entry -> now - entry.getValue().lastPacket() > ACTIVE_PACKET_TTL);
        REPORTED_LEVELS.entrySet().removeIf(entry -> now - entry.getValue().gameTime() > 40L);

        for (var entry : ACTIVE_VOICES.entrySet()) {
            VoiceSignal signal = entry.getValue();
            if (!signal.dimension().equals(level.dimension())) continue;
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null || !player.isAlive()) continue;
            hearVoice(level, player, signal, now);
        }

        investigateRemembered(level, now);
        if (now % 40L == 0L) {
            ENTITY_MEMORY.entrySet().removeIf(entry -> now - entry.getValue().lastHeard() > MEMORY_TTL);
        }
    }

    private static void hearVoice(ServerLevel level, ServerPlayer player, VoiceSignal signal, long now) {
        double configuredRange = OutlandsConfig.VOICE_DETECTION_RANGE.get();
        maybeVoiceKnock(level, player, signal, now);
        AABB search = player.getBoundingBox().inflate(configuredRange * 1.7D + 20.0D);
        List<Mob> nearby = level.getEntitiesOfClass(Mob.class, search,
                mob -> mob.isAlive() && profile(mob) != null);
        for (Mob mob : nearby) {
            if (mob.getTarget() != null && mob.getTarget() != player) continue;
            HearingProfile profile = profile(mob);
            double range = configuredRange * profile.rangeScale();
            range *= 0.25D + (signal.level() / 100.0D) * 0.75D;
            if (signal.whispering()) range *= 0.48D;
            else if (signal.level() >= 76) range *= 1.18D;

            boolean visible = mob.hasLineOfSight(player);
            if (!visible) range *= 0.68D;
            if (level.isRainingAt(player.blockPosition())) range *= 0.88D;
            if (level.isThundering()) range *= 0.92D;
            if (profile.behavior() == Behavior.UNDERGROUND
                    && (player.getY() < 52.0D || mob.getY() < 52.0D)) range *= 1.28D;

            int nearbyVoices = 0;
            for (VoiceSignal other : ACTIVE_VOICES.values()) {
                if (other.dimension().equals(level.dimension())
                        && other.position().distanceToSqr(player.position()) < 20.0D * 20.0D) nearbyVoices++;
            }
            if (nearbyVoices > 1) range *= 1.0D + Math.min(0.28D, (nearbyVoices - 1) * 0.10D);
            range = Math.min(range, configuredRange * 1.65D);
            if (mob.distanceToSqr(player) > range * range) continue;

            VoiceMemory previous = ENTITY_MEMORY.get(mob.getUUID());
            if (previous == null || !previous.player().equals(player.getUUID())
                    || now - previous.lastReaction() >= profile.cooldown()) {
                VoiceMemory memory = new VoiceMemory(player.position(), player.getUUID(),
                        level.dimension(), now, now, signal.level(), signal.whispering());
                ENTITY_MEMORY.put(mob.getUUID(), memory);
                react(mob, player, level, profile, memory, signal.level(), now);
            } else {
                ENTITY_MEMORY.put(mob.getUUID(), new VoiceMemory(player.position(), player.getUUID(),
                        level.dimension(), now, previous.lastReaction(), signal.level(), signal.whispering()));
            }
        }
    }

    private static void react(Mob mob, ServerPlayer player, ServerLevel level,
                              HearingProfile profile, VoiceMemory memory, int intensity, long now) {
        if (profile.behavior() == Behavior.OBSERVE) {
            mob.getLookControl().setLookAt(player, 18.0F, 18.0F);
            if (mob.distanceToSqr(player) < 18.0D * 18.0D) mob.getNavigation().stop();
            else moveToward(mob, memory.position(), profile.speed() * 0.72D);
            return;
        }

        if (profile.behavior() == Behavior.TERRITORIAL) {
            mob.getLookControl().setLookAt(player, 20.0F, 20.0F);
            if (mob.distanceToSqr(player) < 16.0D * 16.0D) mob.getNavigation().stop();
            return;
        }

        if (profile.behavior() == Behavior.FLANK) {
            Vec3 forward = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
            if (forward.lengthSqr() < 0.01D) forward = new Vec3(0.0D, 0.0D, 1.0D);
            forward = forward.normalize();
            Vec3 side = new Vec3(-forward.z, 0.0D, forward.x)
                    .scale(mob.getRandom().nextBoolean() ? 1.0D : -1.0D);
            Vec3 waypoint = memory.position().subtract(forward.scale(5.0D)).add(side.scale(7.0D));
            moveToward(mob, waypoint, profile.speed());
            return;
        }

        if (profile.behavior() == Behavior.UNDERGROUND) {
            Vec3 waypoint = new Vec3(memory.position().x, mob.getY(), memory.position().z);
            moveToward(mob, waypoint, profile.speed());
            return;
        }

        if (profile.behavior() == Behavior.MIMIC) {
            if (mob.distanceToSqr(player) > 10.0D * 10.0D) {
                moveToward(mob, memory.position(), profile.speed() * 0.78D);
            } else {
                mob.getNavigation().stop();
            }
            if (intensity >= 28 && mob.getRandom().nextInt(8) == 0) {
                Vec3 falseOrigin = player.position().add(
                        mob.getRandom().nextBoolean() ? 8.0D : -8.0D, 0.0D,
                        mob.getRandom().nextBoolean() ? 6.0D : -6.0D);
                level.playSound(null, BlockPos.containing(falseOrigin), OutlandsSounds.MIMIC_REVEAL.get(),
                        SoundSource.HOSTILE, 0.42F, 0.72F + mob.getRandom().nextFloat() * 0.22F);
            }
            return;
        }

        if (profile.behavior() == Behavior.HOWLER) {
            if (intensity >= 72 && mob.getRandom().nextInt(8) == 0) {
                level.playSound(null, mob.blockPosition(), OutlandsSounds.HOWLER_CALL.get(),
                        SoundSource.HOSTILE, 0.72F, 0.82F + mob.getRandom().nextFloat() * 0.18F);
                alertNearby(level, mob, player, now);
            } else {
                moveToward(mob, memory.position(), profile.speed());
            }
            return;
        }

        if (profile.behavior() == Behavior.STALK) {
            if (mob.distanceToSqr(player) < profile.approachDistance() * profile.approachDistance()) {
                mob.getNavigation().stop();
                mob.getLookControl().setLookAt(player, 20.0F, 20.0F);
            } else {
                moveToward(mob, memory.position(), profile.speed());
            }
            return;
        }

        moveToward(mob, memory.position(), profile.speed());
    }

    /** Rare knocks from an existing nearby door after an unusually loud call. */
    private static void maybeVoiceKnock(ServerLevel level, ServerPlayer player, VoiceSignal signal, long now) {
        if (signal.whispering() || signal.level() < 82
                || now < PLAYER_EVENT_COOLDOWNS.getOrDefault(player.getUUID(), 0L)
                || player.getRandom().nextInt(240) != 0) return;
        BlockPos origin = player.blockPosition();
        BlockPos nearest = null;
        double nearestDistance = 15.0D * 15.0D;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-14, -5, -14), origin.offset(14, 5, 14))) {
            var block = level.getBlockState(pos).getBlock();
            if (!(block instanceof DoorBlock) && !(block instanceof TrapDoorBlock)) continue;
            double distance = Vec3.atCenterOf(pos).distanceToSqr(player.position());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = pos.immutable();
            }
        }
        if (nearest == null) return;
        level.playSound(null, nearest, SoundEvents.WOODEN_DOOR_CLOSE,
                SoundSource.BLOCKS, 0.42F, 0.72F);
        level.playSound(null, nearest, SoundEvents.WOODEN_DOOR_CLOSE,
                SoundSource.BLOCKS, 0.28F, 0.86F);
        PLAYER_EVENT_COOLDOWNS.put(player.getUUID(), now + 1800L);
    }

    private static void alertNearby(ServerLevel level, Mob caller, ServerPlayer player, long now) {
        for (Mob other : level.getEntitiesOfClass(Mob.class, caller.getBoundingBox().inflate(34.0D),
                mob -> mob != caller && mob.isAlive() && profile(mob) != null)) {
            VoiceMemory existing = ENTITY_MEMORY.get(other.getUUID());
            if (existing == null || now - existing.lastReaction() > 35L) {
                ENTITY_MEMORY.put(other.getUUID(), new VoiceMemory(player.position(), player.getUUID(),
                        level.dimension(), now, now, 62, false));
            }
        }
    }

    private static void moveToward(Mob mob, Vec3 position, double speed) {
        BlockPos destination = BlockPos.containing(position);
        if (!mob.level().hasChunkAt(destination)) return;
        mob.getNavigation().moveTo(position.x, position.y, position.z, speed);
    }

    /** Continue investigating briefly after the player stops speaking. */
    private static void investigateRemembered(ServerLevel level, long now) {
        for (var entry : ENTITY_MEMORY.entrySet()) {
            VoiceMemory memory = entry.getValue();
            if (!memory.dimension().equals(level.dimension())
                    || now - memory.lastHeard() > MEMORY_TTL
                    || now - memory.lastHeard() < ACTIVE_PACKET_TTL) continue;
            Mob mob = level.getEntity(entry.getKey()) instanceof Mob found ? found : null;
            if (mob == null || !mob.isAlive() || mob.getTarget() != null || !mob.getNavigation().isDone()) continue;
            HearingProfile profile = profile(mob);
            if (profile == null || profile.behavior() == Behavior.TERRITORIAL
                    || now - memory.lastReaction() < 24L) continue;
            moveToward(mob, memory.position(), profile.speed() * 0.68D);
            ENTITY_MEMORY.put(entry.getKey(), new VoiceMemory(memory.position(), memory.player(),
                    memory.dimension(), memory.lastHeard(), now, memory.intensity(), memory.whispering()));
        }
    }

    private static HearingProfile profile(Mob mob) {
        if (mob instanceof StalkerEntity) return new HearingProfile(1.08D, 0.78D, 8.0D, 42, Behavior.STALK);
        if (mob instanceof HunterEntity) return new HearingProfile(1.25D, 1.02D, 0.0D, 24, Behavior.HUNT);
        if (mob instanceof MimicEntity) return new HearingProfile(0.86D, 0.72D, 10.0D, 100, Behavior.MIMIC);
        if (mob instanceof HowlerEntity) return new HearingProfile(1.40D, 0.80D, 12.0D, 120, Behavior.HOWLER);
        if (mob instanceof DredgeEntity || mob instanceof BurrowerEntity)
            return new HearingProfile(0.94D, 0.92D, 0.0D, 48, Behavior.UNDERGROUND);
        if (mob instanceof CeilingDwellerEntity) return new HearingProfile(0.90D, 0.82D, 0.0D, 55, Behavior.UNDERGROUND);
        if (mob instanceof LurkerEntity) return new HearingProfile(1.00D, 0.84D, 9.0D, 50, Behavior.FLANK);
        if (mob instanceof ObserverEntity || mob instanceof FearEntity)
            return new HearingProfile(0.78D, 0.64D, 16.0D, 90, Behavior.OBSERVE);
        if (mob instanceof CaveJarEntity || mob instanceof StillbornStatueEntity)
            return new HearingProfile(0.52D, 0.56D, 14.0D, 150, Behavior.TERRITORIAL);
        if (mob instanceof AgitatedOneEntity || mob instanceof DeathEntity || mob instanceof NullEntity)
            return new HearingProfile(1.02D, 0.96D, 0.0D, 36, Behavior.HUNT);
        if (mob instanceof NullApostleEntity || mob instanceof AlmightyEntity || mob instanceof ColossusEntity)
            return new HearingProfile(0.84D, 0.78D, 0.0D, 100, Behavior.OBSERVE);
        if (mob instanceof PilgrimEntity)
            return new HearingProfile(0.82D, 0.70D, 13.0D, 100, Behavior.STALK);
        if (mob instanceof NightmareCreature creature) {
            return switch (creature.getVariantId()) {
                case "nightmare_watcher" -> new HearingProfile(1.10D, 0.76D, 18.0D, 65, Behavior.OBSERVE);
                case "nightmare_hound" -> new HearingProfile(1.42D, 1.12D, 0.0D, 20, Behavior.HUNT);
                case "nightmare_wardened" -> new HearingProfile(0.82D, 0.86D, 16.0D, 130, Behavior.TERRITORIAL);
                case "nightmare_false_survivor" -> new HearingProfile(0.96D, 0.72D, 10.0D, 110, Behavior.MIMIC);
                case "nightmare_unbound" -> new HearingProfile(1.16D, 0.96D, 0.0D, 50, Behavior.STALK);
                default -> new HearingProfile(1.0D, 0.85D, 0.0D, 50, Behavior.HUNT);
            };
        }
        return null;
    }

    public static void clear(UUID playerId) {
        REPORTED_LEVELS.remove(playerId);
        ACTIVE_VOICES.remove(playerId);
        ENTITY_MEMORY.entrySet().removeIf(entry -> entry.getValue().player().equals(playerId));
        PLAYER_EVENT_COOLDOWNS.remove(playerId);
    }

    public static void clear() {
        REPORTED_LEVELS.clear();
        ACTIVE_VOICES.clear();
        ENTITY_MEMORY.clear();
        PLAYER_EVENT_COOLDOWNS.clear();
    }

    private enum Behavior { STALK, HUNT, OBSERVE, FLANK, UNDERGROUND, TERRITORIAL, MIMIC, HOWLER }
    private record HearingProfile(double rangeScale, double speed, double approachDistance,
                                  int cooldown, Behavior behavior) {}
    private record ReportedLevel(int level, boolean whispering, long gameTime) {}
    private record VoiceSignal(Vec3 position, ResourceKey<Level> dimension, int level,
                               boolean whispering, long lastPacket) {}
    private record VoiceMemory(Vec3 position, UUID player, ResourceKey<Level> dimension,
                               long lastHeard, long lastReaction, int intensity, boolean whispering) {}
}
