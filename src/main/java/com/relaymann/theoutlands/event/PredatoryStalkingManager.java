package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

/**
 * Per-species stalking and settlement pressure. This layer complements (rather
 * than replaces) each entity's own goals: it never teleports creatures, edits
 * terrain, forces a target, or announces an encounter to the player.
 */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class PredatoryStalkingManager {
    private static final int SAMPLE_INTERVAL = 10;
    private static final int BUILD_MEMORY = 2400;
    private static final int MAX_BUILD_MARKS = 256;
    private static final String TEMPORARY_DISAPPEARANCE_TAG = "theoutlands_temporary_disappearance";
    private static final Map<UUID, Memory> MEMORIES = new HashMap<>();
    private static final Map<UUID, Long> BEHAVIOR_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Disappearance> DISAPPEAR_UNTIL = new HashMap<>();
    private static final Map<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>, ArrayDeque<BuildMark>> BUILD_MARKS = new HashMap<>();

    private PredatoryStalkingManager() {}

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof ServerPlayer player)
                || !player.isAlive() || player.isCreative() || player.isSpectator()) return;
        ArrayDeque<BuildMark> marks = BUILD_MARKS.computeIfAbsent(level.dimension(), ignored -> new ArrayDeque<>());
        marks.addLast(new BuildMark(event.getPos().immutable(), player.getUUID(), level.getGameTime(), false));
        while (marks.size() > MAX_BUILD_MARKS) marks.removeFirst();
    }


    @SubscribeEvent
    public static void onDoorUsed(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof ServerPlayer player)
                || !player.isAlive() || player.isCreative() || player.isSpectator()) return;
        var block = level.getBlockState(event.getPos()).getBlock();
        if (!(block instanceof DoorBlock) && !(block instanceof TrapDoorBlock)) return;
        ArrayDeque<BuildMark> marks = BUILD_MARKS.computeIfAbsent(level.dimension(), ignored -> new ArrayDeque<>());
        marks.addLast(new BuildMark(event.getPos().immutable(), player.getUUID(), level.getGameTime(), true));
        while (marks.size() > MAX_BUILD_MARKS) marks.removeFirst();
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getTags().contains(TEMPORARY_DISAPPEARANCE_TAG)) return;
        Disappearance state = DISAPPEAR_UNTIL.get(mob.getUUID());
        if (state == null || state.level() != level
                || !OutlandsConfig.ENTITY_INTELLIGENCE_ENABLED.get()
                || level.getGameTime() >= state.expiresAt()) {
            // The marker survives chunk unloads and server restarts; never strand a mob invisible.
            mob.setInvisible(false);
            mob.removeTag(TEMPORARY_DISAPPEARANCE_TAG);
            DISAPPEAR_UNTIL.remove(mob.getUUID());
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % SAMPLE_INTERVAL != 0) return;
        boolean intelligenceEnabled = OutlandsConfig.ENTITY_INTELLIGENCE_ENABLED.get();
        restoreTrackedDisappearances(event.getServer().getAllLevels(), !intelligenceEnabled);
        if (!intelligenceEnabled) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            Set<UUID> processed = new HashSet<>();
            for (ServerPlayer player : level.players()) {
                if (!eligible(player)) continue;
                List<Mob> nearby = level.getEntitiesOfClass(Mob.class,
                        player.getBoundingBox().inflate(48.0D),
                        mob -> mob.isAlive() && personality(mob) != null);
                for (Mob mob : nearby) {
                    if (!processed.add(mob.getUUID())) continue;
                    restoreExpiredDisappearance(mob, level);
                    if (mob.getTarget() instanceof Player target && eligible(target)) {
                        remember(mob, target, level.getGameTime());
                        applyPersonality(mob, target, level);
                    } else {
                        resumeLastKnownRoute(mob, level);
                        pressureSettlement(mob, player, level);
                    }
                    tryWindowPosition(mob, player, level);
                    maybeDisappearWhenWatched(mob, player, level);
                }
            }
        }
        if (event.getServer().getTickCount() % 1200 == 0) cleanup(event.getServer().getAllLevels());
    }

    private static boolean eligible(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    /** Every species has its own tactical profile; special entities keep their own attack goals. */
    private static Personality personality(Mob mob) {
        if (mob instanceof StalkerEntity) return new Personality(Style.SHADOW, 0.84D, 14, 34, 150, true);
        if (mob instanceof HunterEntity) return new Personality(Style.PURSUIT, 1.12D, 0, 56, 80, false);
        if (mob instanceof ObserverEntity) return new Personality(Style.OBSERVE, 0.68D, 22, 46, 240, true);
        if (mob instanceof LurkerEntity) return new Personality(Style.FLANK, 0.82D, 12, 30, 180, true);
        if (mob instanceof MimicEntity) return new Personality(Style.AMBUSH, 0.72D, 7, 24, 260, false);
        if (mob instanceof BurrowerEntity) return new Personality(Style.SUBTERRANEAN, 0.90D, 8, 38, 220, false);
        if (mob instanceof CaveJarEntity) return new Personality(Style.TERRITORIAL, 0.58D, 0, 18, 300, false);
        if (mob instanceof DredgeEntity) return new Personality(Style.SUBTERRANEAN, 0.96D, 0, 32, 170, false);
        if (mob instanceof CeilingDwellerEntity) return new Personality(Style.DROP_AMBUSH, 0.76D, 5, 24, 260, false);
        if (mob instanceof StillbornStatueEntity) return new Personality(Style.STATUE, 0.62D, 0, 18, 320, false);
        if (mob instanceof AgitatedOneEntity) return new Personality(Style.CHARGE, 1.04D, 0, 38, 110, false);
        if (mob instanceof HowlerEntity) return new Personality(Style.CALLER, 0.72D, 18, 48, 240, false);
        if (mob instanceof NullEntity) return new Personality(Style.INEVITABLE, 0.92D, 0, 44, 260, true);
        if (mob instanceof NullApostleEntity) return new Personality(Style.PATROL, 0.78D, 0, 34, 220, false);
        if (mob instanceof FearEntity) return new Personality(Style.PRESENCE, 0.64D, 24, 52, 360, true);
        if (mob instanceof DeathEntity) return new Personality(Style.INEVITABLE, 0.88D, 0, 56, 180, false);
        if (mob instanceof AlmightyEntity || mob instanceof ColossusEntity)
            return new Personality(Style.DOMINANT, 0.72D, 0, 44, 300, false);
        if (mob instanceof PilgrimEntity) return new Personality(Style.PILGRIM, 0.74D, 16, 40, 280, true);
        if (mob instanceof NightmareCreature creature) {
            return switch (creature.getVariantId()) {
                case "nightmare_watcher" -> new Personality(Style.OBSERVE, 0.82D, 24, 56, 180, true);
                case "nightmare_hound" -> new Personality(Style.PURSUIT, 1.18D, 0, 64, 70, false);
                case "nightmare_wardened" -> new Personality(Style.TERRITORIAL, 0.86D, 0, 28, 300, false);
                case "nightmare_false_survivor" -> new Personality(Style.DECOY, 0.76D, 8, 36, 240, true);
                case "nightmare_unbound" -> new Personality(Style.INEVITABLE, 1.04D, 0, 52, 120, false);
                default -> new Personality(Style.PURSUIT, 0.9D, 0, 36, 180, false);
            };
        }
        return null;
    }

    private static void remember(Mob mob, Player target, long now) {
        Memory memory = MEMORIES.computeIfAbsent(mob.getUUID(), ignored -> new Memory());
        if (mob.hasLineOfSight(target)) {
            memory.x = target.getX();
            memory.y = target.getY();
            memory.z = target.getZ();
            memory.lastSeen = now;
            memory.target = target.getUUID();
        }
    }

    private static void applyPersonality(Mob mob, Player target, ServerLevel level) {
        Personality profile = personality(mob);
        if (profile == null || mob.tickCount % 20 != 0 || !mob.getNavigation().isDone()) return;
        double distance = mob.distanceToSqr(target);
        if (distance < profile.minimumRange() * profile.minimumRange()
                || distance > profile.maximumRange() * profile.maximumRange()) return;
        long now = level.getGameTime();
        if (now < BEHAVIOR_COOLDOWNS.getOrDefault(mob.getUUID(), 0L)) return;

        boolean seen = target.hasLineOfSight(mob);
        Vec3 destination = null;
        if (profile.style() == Style.SHADOW || profile.style() == Style.PURSUIT
                || profile.style() == Style.FLANK || profile.style() == Style.OBSERVE
                || profile.style() == Style.DECOY) {
            destination = routeAwareWaypoint(mob, target, level);
        }
        switch (profile.style()) {
            case SHADOW, FLANK -> {
                if (seen && distance > 100.0D) destination = flankPoint(mob, target, profile.style() == Style.SHADOW);
            }
            case OBSERVE, PRESENCE, DECOY -> {
                if (seen) {
                    mob.getNavigation().stop();
                    mob.getLookControl().setLookAt(target, 25.0F, 25.0F);
                } else if (distance > 256.0D) destination = behindCoverPoint(mob, target);
            }
            case AMBUSH, DROP_AMBUSH -> {
                if (seen && distance > 64.0D) destination = flankPoint(mob, target, true);
            }
            case PURSUIT, INEVITABLE -> {
                // Their native goals handle the attack. The director only corrects a
                // stale route after the player crosses a doorway or changes floors.
                if (!seen && distance > 100.0D) destination = lastKnown(mob);
            }
            case SUBTERRANEAN -> {
                if (distance > 100.0D && seen) destination = target.position().add(target.getDeltaMovement().scale(5.0D));
            }
            case TERRITORIAL, STATUE, DOMINANT -> {
                // These entities hold ground unless their own goal has a target.
                if (distance < 196.0D && seen) mob.getNavigation().stop();
            }
            case CHARGE -> {
                // Preserve the Agitated One's wind-up and charge sequence.
                if (seen && distance > 36.0D) destination = target.position();
            }
            case CALLER -> {
                if (seen && distance > 400.0D) destination = flankPoint(mob, target, true);
            }
            case PATROL -> {
                if (!seen && distance > 144.0D) destination = lastKnown(mob);
            }
            case PILGRIM -> {
                if (seen && distance > 256.0D) destination = flankPoint(mob, target, false);
            }
        }
        if (destination != null && level.isLoaded(BlockPos.containing(destination))) {
            mob.getNavigation().moveTo(destination.x, destination.y, destination.z, profile.speed());
            BEHAVIOR_COOLDOWNS.put(mob.getUUID(), now + profile.cooldown());
        }
    }

    /**
     * Reads recently used doorways and player-placed obstructions along the
     * predator's direct approach. It sends the mob to the doorway or around
     * the barricade; vanilla navigation then calculates a collision-safe path.
     */
    private static Vec3 routeAwareWaypoint(Mob mob, Player target, ServerLevel level) {
        ArrayDeque<BuildMark> marks = BUILD_MARKS.get(level.dimension());
        if (marks == null || mob.distanceToSqr(target) < 64.0D) return null;
        Vec3 start = mob.position();
        Vec3 end = target.position();
        Vec3 line = end.subtract(start);
        double lengthSqr = line.x * line.x + line.z * line.z;
        if (lengthSqr < 1.0D) return null;
        BuildMark best = null;
        double bestAlong = Double.MAX_VALUE;
        long now = level.getGameTime();
        for (BuildMark mark : marks) {
            if (!mark.owner().equals(target.getUUID()) || now - mark.time() > BUILD_MEMORY
                    || !level.isLoaded(mark.pos())) continue;
            Vec3 relative = Vec3.atCenterOf(mark.pos()).subtract(start);
            double along = (relative.x * line.x + relative.z * line.z) / lengthSqr;
            if (along < 0.12D || along > 0.88D) continue;
            Vec3 closest = start.add(line.scale(along));
            double lateral = closest.distanceToSqr(Vec3.atCenterOf(mark.pos()));
            if (lateral > 3.2D * 3.2D || along >= bestAlong) continue;
            best = mark;
            bestAlong = along;
        }
        if (best == null) return null;
        if (best.doorway()) {
            return new Vec3(best.pos().getX() + 0.5D, best.pos().getY(), best.pos().getZ() + 0.5D);
        }
        Vec3 perpendicular = new Vec3(-line.z, 0.0D, line.x).normalize();
        Vec3 left = Vec3.atCenterOf(best.pos()).add(perpendicular.scale(3.6D));
        Vec3 right = Vec3.atCenterOf(best.pos()).subtract(perpendicular.scale(3.6D));
        return mob.position().distanceToSqr(left) < mob.position().distanceToSqr(right) ? left : right;
    }

    private static Vec3 lastKnown(Mob mob) {
        Memory memory = MEMORIES.get(mob.getUUID());
        return memory == null || memory.lastSeen == 0L ? null : new Vec3(memory.x, memory.y, memory.z);
    }

    private static Vec3 flankPoint(Mob mob, Player target, boolean behind) {
        double yaw = Math.toRadians(target.getYRot());
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x)
                .scale(mob.getRandom().nextBoolean() ? 1.0D : -1.0D);
        Vec3 point = target.position().subtract(forward.scale(behind ? 7.0D : 3.0D))
                .add(side.scale(behind ? 6.0D : 10.0D));
        return new Vec3(point.x, target.getY(), point.z);
    }

    private static Vec3 behindCoverPoint(Mob mob, Player target) {
        Vec3 away = mob.position().subtract(target.position());
        if (away.lengthSqr() < 0.01D) away = new Vec3(1, 0, 0);
        return target.position().add(away.normalize().scale(12.0D));
    }

    /** Follow a player through buildings using the last real sighting, never teleporting. */
    private static void resumeLastKnownRoute(Mob mob, ServerLevel level) {
        Memory memory = MEMORIES.get(mob.getUUID());
        if (memory == null || memory.lastSeen == 0L || mob.getTarget() != null
                || level.getGameTime() - memory.lastSeen > 260L || !mob.getNavigation().isDone()
                || mob.tickCount % 30 != 0) return;
        BlockPos remembered = BlockPos.containing(memory.x, memory.y, memory.z);
        if (!level.isLoaded(remembered) || mob.distanceToSqr(Vec3.atCenterOf(remembered)) > 72.0D * 72.0D) return;
        Personality profile = personality(mob);
        if (profile != null && profile.style() != Style.TERRITORIAL && profile.style() != Style.STATUE
                && profile.style() != Style.DOMINANT) {
            mob.getNavigation().moveTo(memory.x, memory.y, memory.z, profile.speed() * 0.88D);
        }
    }

    /** Detect a player-built settlement from recent block placement and patrol its perimeter. */
    private static void pressureSettlement(Mob mob, ServerPlayer player, ServerLevel level) {
        Personality profile = personality(mob);
        if (profile == null || !(profile.style() == Style.SHADOW || profile.style() == Style.OBSERVE
                || profile.style() == Style.FLANK || profile.style() == Style.DECOY)
                || mob.getTarget() != null || mob.tickCount % 100 != 0 || !mob.getNavigation().isDone()) return;
        ArrayDeque<BuildMark> marks = BUILD_MARKS.get(level.dimension());
        if (marks == null) return;
        long now = level.getGameTime();
        double sx = 0, sy = 0, sz = 0;
        int count = 0;
        for (BuildMark mark : marks) {
            if (!mark.owner().equals(player.getUUID()) || now - mark.time() > BUILD_MEMORY
                    || Vec3.atCenterOf(mark.pos()).distanceToSqr(player.position()) > 28.0D * 28.0D) continue;
            sx += mark.pos().getX() + 0.5D;
            sy += mark.pos().getY();
            sz += mark.pos().getZ() + 0.5D;
            count++;
        }
        if (count < 7) return;
        Vec3 center = new Vec3(sx / count, sy / count, sz / count);
        double distance = mob.position().distanceToSqr(center);
        if (distance < 7.0D * 7.0D || distance > 52.0D * 52.0D) return;
        long slot = Math.floorDiv(now, 100L) + (mob.getUUID().getLeastSignificantBits() & 7L);
        double angle = (slot % 8L) * (Math.PI / 4.0D);
        double radius = 13.0D + ((mob.getUUID().getMostSignificantBits() & 3L) * 1.5D);
        Vec3 waypoint = center.add(Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius);
        if (level.isLoaded(BlockPos.containing(waypoint))) {
            mob.getNavigation().moveTo(waypoint.x, waypoint.y, waypoint.z, profile.speed() * 0.82D);
            BEHAVIOR_COOLDOWNS.put(mob.getUUID(), now + 100L);
        }
    }

    /** Rarely take up a position outside glass, making a creature visible in a window. */
    private static void tryWindowPosition(Mob mob, ServerPlayer player, ServerLevel level) {
        Personality profile = personality(mob);
        if (profile == null || !(profile.style() == Style.SHADOW || profile.style() == Style.OBSERVE
                || profile.style() == Style.FLANK || profile.style() == Style.DECOY)
                || mob.getTarget() != null || mob.tickCount % 40 != 0 || mob.distanceToSqr(player) < 64.0D
                || mob.distanceToSqr(player) > 36.0D * 36.0D || !mob.getNavigation().isDone()) return;
        if (mob.getRandom().nextInt(90) != 0) return;
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < 28; attempt++) {
            BlockPos glass = origin.offset(mob.getRandom().nextInt(17) - 8,
                    mob.getRandom().nextInt(5) - 2, mob.getRandom().nextInt(17) - 8);
            var block = level.getBlockState(glass).getBlock();
            if (block != Blocks.GLASS && block != Blocks.GLASS_PANE && block != Blocks.TINTED_GLASS
                    && block != Blocks.WHITE_STAINED_GLASS && block != Blocks.BLACK_STAINED_GLASS) continue;
            Vec3 outward = new Vec3(glass.getX() - player.getX(), 0, glass.getZ() - player.getZ());
            if (outward.lengthSqr() < 1.0D) outward = new Vec3(1, 0, 0);
            Vec3 outside = new Vec3(glass.getX() + 0.5D, glass.getY(), glass.getZ() + 0.5D)
                    .add(outward.normalize().scale(2.2D));
            if (level.isLoaded(BlockPos.containing(outside))) {
                mob.getNavigation().moveTo(outside.x, outside.y, outside.z, profile.speed() * 0.72D);
                BEHAVIOR_COOLDOWNS.put(mob.getUUID(), level.getGameTime() + 180L);
                return;
            }
        }
    }

    /** Only the Watcher-like species can perform this rare, brief visual disappearance. */
    private static void restoreExpiredDisappearance(Mob mob, ServerLevel level) {
        Disappearance state = DISAPPEAR_UNTIL.get(mob.getUUID());
        if (state != null && state.level() == level && level.getGameTime() >= state.expiresAt()) {
            mob.setInvisible(false);
            mob.removeTag(TEMPORARY_DISAPPEARANCE_TAG);
            DISAPPEAR_UNTIL.remove(mob.getUUID());
        }
    }

    private static void maybeDisappearWhenWatched(Mob mob, ServerPlayer player, ServerLevel level) {
        Personality profile = personality(mob);
        if (profile == null || !profile.disappearWhenWatched() || mob.isInvisible()
                || mob.distanceToSqr(player) < 100.0D || mob.distanceToSqr(player) > 42.0D * 42.0D
                || !player.hasLineOfSight(mob) || !isLookingAt(player, mob)
                || mob.tickCount % 20 != 0 || mob.getRandom().nextInt(24) != 0
                || level.getGameTime() < BEHAVIOR_COOLDOWNS.getOrDefault(mob.getUUID(), 0L)) return;
        mob.setInvisible(true);
        mob.addTag(TEMPORARY_DISAPPEARANCE_TAG);
        DISAPPEAR_UNTIL.put(mob.getUUID(), new Disappearance(level, level.getGameTime() + 14L));
        BEHAVIOR_COOLDOWNS.put(mob.getUUID(), level.getGameTime() + 420L);
    }

    private static boolean isLookingAt(Player player, Mob mob) {
        Vec3 toMob = mob.getEyePosition().subtract(player.getEyePosition()).normalize();
        return player.getLookAngle().dot(toMob) > 0.965D;
    }

    private static void cleanup(Iterable<ServerLevel> levels) {
        Set<UUID> active = new HashSet<>();
        for (ServerLevel level : levels) {
            long now = level.getGameTime();
            for (ServerPlayer player : level.players()) {
                if (!eligible(player)) continue;
                for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(128.0D),
                        candidate -> personality(candidate) != null)) active.add(mob.getUUID());
            }
            ArrayDeque<BuildMark> marks = BUILD_MARKS.get(level.dimension());
            if (marks != null) marks.removeIf(mark -> now - mark.time() > BUILD_MEMORY);
            for (var entry : new ArrayList<>(DISAPPEAR_UNTIL.entrySet())) {
                if (entry.getValue() <= now) DISAPPEAR_UNTIL.remove(entry.getKey());
            }
        }
        MEMORIES.keySet().removeIf(id -> !active.contains(id));
        BEHAVIOR_COOLDOWNS.keySet().removeIf(id -> !active.contains(id));
    }

    private static void restoreTrackedDisappearances(Iterable<ServerLevel> levels, boolean force) {
        for (ServerLevel level : levels) {
            var iterator = DISAPPEAR_UNTIL.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                Disappearance state = entry.getValue();
                if (state.level() != level) continue;
                Entity entity = level.getEntity(entry.getKey());
                if (entity instanceof Mob mob && (force || level.getGameTime() >= state.expiresAt())) {
                    mob.setInvisible(false);
                    mob.removeTag(TEMPORARY_DISAPPEARANCE_TAG);
                    iterator.remove();
                } else if (entity == null && level.getGameTime() > state.expiresAt() + 24_000L) {
                    // If the entity is unloaded, its saved marker restores it when it is loaded again.
                    iterator.remove();
                }
            }
        }
    }

    public static void clear(Iterable<ServerLevel> levels) {
        restoreTrackedDisappearances(levels, true);
        clear();
    }

    public static void clear() {
        MEMORIES.clear();
        BEHAVIOR_COOLDOWNS.clear();
        DISAPPEAR_UNTIL.clear();
        BUILD_MARKS.clear();
    }

    private enum Style { SHADOW, PURSUIT, OBSERVE, FLANK, AMBUSH, SUBTERRANEAN, TERRITORIAL,
        DROP_AMBUSH, STATUE, CHARGE, CALLER, INEVITABLE, PRESENCE, PATROL, DOMINANT, PILGRIM, DECOY }
    private record Personality(Style style, double speed, double minimumRange, double maximumRange,
                               int cooldown, boolean disappearWhenWatched) {}
    private record BuildMark(BlockPos pos, UUID owner, long time, boolean doorway) {}
    private record Disappearance(ServerLevel level, long expiresAt) {}
    private static final class Memory {
        private double x, y, z;
        private long lastSeen;
        private UUID target;
    }
}
