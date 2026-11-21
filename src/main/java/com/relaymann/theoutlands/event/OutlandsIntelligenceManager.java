package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

/**
 * Lightweight server-authoritative intelligence layer shared by the existing
 * roster. It remembers recent physical disturbances and player habits, then
 * lets curious creatures investigate without spawning, teleporting, or
 * modifying blocks. Individual temperaments are stable for each entity UUID.
 */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class OutlandsIntelligenceManager {
    private static final int DISTURBANCE_LIFETIME = 600;
    private static final int MAX_DISTURBANCES_PER_DIMENSION = 96;
    private static final Map<ResourceKey<Level>, ArrayDeque<Disturbance>> DISTURBANCES = new HashMap<>();
    private static final Map<UUID, PlayerPattern> PLAYER_PATTERNS = new HashMap<>();
    private static final Map<UUID, Long> INVESTIGATION_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Territory> TERRITORIES = new HashMap<>();
    private static long lastCleanup;

    private OutlandsIntelligenceManager() {}

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getPlayer() instanceof ServerPlayer player)
                || !eligible(player)) return;
        record(level, event.getPos(), player.getUUID(), "break");
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof ServerPlayer player) || !eligible(player)) return;
        record(level, event.getPos(), player.getUUID(), "place");
    }

    @SubscribeEvent
    public static void onOutlandsCreatureDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)
                || !(event.getEntity() instanceof Mob mob) || !isOutlandsCreature(mob)) return;
        // Nearby creatures can investigate a real death site. No death message,
        // fake remains, block edits, or forced encounter is produced.
        record(level, mob.blockPosition(), mob.getUUID(), "death");
    }

    @SubscribeEvent
    public static void onDoorUsed(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ServerPlayer player)
                || !eligible(player)) return;
        var block = level.getBlockState(event.getPos()).getBlock();
        if (block instanceof DoorBlock || block instanceof TrapDoorBlock) {
            record(level, event.getPos(), player.getUUID(), "door");
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!OutlandsConfig.ENTITY_INTELLIGENCE_ENABLED.get()
                || event.getServer().getTickCount() % 10 != 0) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            updatePlayerPatterns(level);
            Set<UUID> processed = new HashSet<>();
            for (ServerPlayer player : level.players()) {
                if (!eligible(player)) continue;
                var nearby = level.getEntitiesOfClass(Mob.class,
                        new AABB(player.blockPosition()).inflate(40.0D),
                        mob -> mob.isAlive() && isOutlandsCreature(mob));
                for (Mob mob : nearby) {
                    if (processed.add(mob.getUUID())) {
                        maintainTerritory(level, mob);
                        investigateDisturbances(level, mob);
                        adaptPursuit(mob);
                    }
                }
            }
        }
        if (event.getServer().getTickCount() - lastCleanup >= 1200) {
            cleanup(event.getServer().getAllLevels());
            lastCleanup = event.getServer().getTickCount();
        }
    }

    private static boolean eligible(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    private static boolean isOutlandsCreature(Mob mob) {
        return mob instanceof CaveJarEntity || mob instanceof StalkerEntity
                || mob instanceof ObserverEntity || mob instanceof HunterEntity
                || mob instanceof BurrowerEntity || mob instanceof MimicEntity
                || mob instanceof StillbornStatueEntity || mob instanceof AgitatedOneEntity
                || mob instanceof NullEntity || mob instanceof NullApostleEntity
                || mob instanceof FearEntity || mob instanceof DeathEntity
                || mob instanceof AlmightyEntity;
    }

    private static void record(ServerLevel level, BlockPos pos, UUID source, String kind) {
        ArrayDeque<Disturbance> records = DISTURBANCES.computeIfAbsent(level.dimension(), ignored -> new ArrayDeque<>());
        records.addLast(new Disturbance(pos.immutable(), source, level.getGameTime(), kind));
        while (records.size() > MAX_DISTURBANCES_PER_DIMENSION) records.removeFirst();
    }

    private static void updatePlayerPatterns(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (!eligible(player)) {
                PLAYER_PATTERNS.remove(player.getUUID());
                continue;
            }
            PlayerPattern pattern = PLAYER_PATTERNS.computeIfAbsent(player.getUUID(), ignored -> new PlayerPattern());
            pattern.samples++;
            if (player.isSprinting()) pattern.sprintSamples++;
            if (player.isCrouching()) pattern.sneakSamples++;
            if (pattern.samples > 200) {
                pattern.samples = Math.max(1, pattern.samples / 2);
                pattern.sprintSamples /= 2;
                pattern.sneakSamples /= 2;
            }
        }
    }

    private static void investigateDisturbances(ServerLevel level, Mob mob) {
        if (mob.getTarget() != null || mob.isVehicle() || mob.isPassenger()
                || !canInvestigate(mob) || mob.tickCount % 20 != 0) return;
        long now = level.getGameTime();
        long nextAllowed = INVESTIGATION_COOLDOWNS.getOrDefault(mob.getUUID(), 0L);
        if (now < nextAllowed) return;

        ArrayDeque<Disturbance> records = DISTURBANCES.get(level.dimension());
        if (records == null || records.isEmpty()) return;
        double bestDistance = 28.0D * 28.0D;
        Disturbance best = null;
        for (Iterator<Disturbance> iterator = records.descendingIterator(); iterator.hasNext();) {
            Disturbance disturbance = iterator.next();
            if (now - disturbance.gameTime() > DISTURBANCE_LIFETIME) continue;
            if (disturbance.source().equals(mob.getUUID())) continue;
            double distance = mob.distanceToSqr(Vec3.atCenterOf(disturbance.pos()));
            if (distance < bestDistance && level.isLoaded(disturbance.pos())) {
                bestDistance = distance;
                best = disturbance;
            }
        }
        if (best == null) return;

        Temperament temperament = temperament(mob);
        // A creature's curiosity and patience are deterministic traits, not
        // random stat rerolls every tick.
        if (mob.getRandom().nextFloat() > temperament.curiosity()) return;
        BlockPos point = best.pos();
        double speed = 0.72D + temperament.boldness() * 0.30D;
        mob.getNavigation().moveTo(point.getX() + 0.5D, point.getY(), point.getZ() + 0.5D, speed);
        INVESTIGATION_COOLDOWNS.put(mob.getUUID(), now + 100L + (long)(temperament.patience() * 180.0D));
    }

    private static void maintainTerritory(ServerLevel level, Mob mob) {
        int radius = territoryRadius(mob);
        if (radius == 0) return;
        Territory territory = TERRITORIES.computeIfAbsent(mob.getUUID(),
                ignored -> new Territory(mob.blockPosition().immutable(), radius));
        if (mob.getTarget() != null) return;

        // Home ranges are virtual ecological territories: they guide patrol and
        // return behavior but never place, remove, or disguise world blocks.
        double distance = mob.distanceToSqr(Vec3.atCenterOf(territory.center()));
        long now = level.getGameTime();
        if (distance > (double) territory.radius() * territory.radius() * 2.25D
                && now >= territory.nextReturn() && mob.getNavigation().isDone()) {
            BlockPos home = territory.center();
            mob.getNavigation().moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D,
                    0.72D + temperament(mob).patience() * 0.22D);
            TERRITORIES.put(mob.getUUID(), new Territory(home, territory.radius(), now + 100L));
        }
    }

    private static int territoryRadius(Mob mob) {
        if (mob instanceof StillbornStatueEntity) return 14;
        if (mob instanceof AgitatedOneEntity) return 24;
        if (mob instanceof CaveJarEntity) return 34;
        if (mob instanceof BurrowerEntity) return 28;
        return 0;
    }

    private static boolean canInvestigate(Mob mob) {
        // The major supernatural presences retain their own encounter logic.
        // This shared system is for independent creatures and ordinary apostles.
        return mob instanceof CaveJarEntity || mob instanceof StalkerEntity
                || mob instanceof HunterEntity || mob instanceof BurrowerEntity
                || mob instanceof MimicEntity || mob instanceof StillbornStatueEntity
                || mob instanceof AgitatedOneEntity || mob instanceof NullApostleEntity;
    }

    private static void adaptPursuit(Mob mob) {
        if (!(mob.getTarget() instanceof Player target) || !eligible(target)) return;
        PlayerPattern pattern = PLAYER_PATTERNS.get(target.getUUID());
        if (pattern == null || pattern.samples < 8) return;
        Temperament temperament = temperament(mob);
        double sprintRatio = (double) pattern.sprintSamples / pattern.samples;
        double sneakRatio = (double) pattern.sneakSamples / pattern.samples;

        // Persistent sprinters are more likely to be intercepted. Frequent
        // crouching makes patient hunters favor the last physical disturbance
        // rather than repeatedly charging the player's current position.
        if ((mob instanceof StalkerEntity || mob instanceof HunterEntity)
                && sprintRatio > 0.52D && temperament.boldness() > 0.35D
                && mob.distanceToSqr(target) > 100.0D && mob.tickCount % 50 == 0) {
            Vec3 lead = target.position().add(target.getDeltaMovement().scale(8.0D + temperament.boldness() * 8.0D));
            mob.getNavigation().moveTo(lead.x, lead.y, lead.z, 1.0D + temperament.boldness() * 0.12D);
        } else if (sneakRatio > 0.48D && temperament.patience() > 0.58D
                && mob.getTarget() == target && mob.tickCount % 80 == 0
                && mob.distanceToSqr(target) > 64.0D) {
            ArrayDeque<Disturbance> records = DISTURBANCES.get(((ServerLevel) mob.level()).dimension());
            if (records != null && !records.isEmpty()) {
                Disturbance last = records.peekLast();
                if (last != null && ((ServerLevel) mob.level()).getGameTime() - last.gameTime() < 240L) {
                    mob.getNavigation().moveTo(last.pos().getX() + 0.5D, last.pos().getY(),
                            last.pos().getZ() + 0.5D, 0.88D);
                }
            }
        }
    }

    private static Temperament temperament(Mob mob) {
        long bits = mob.getUUID().getMostSignificantBits() ^ mob.getUUID().getLeastSignificantBits();
        float curiosity = unit(bits);
        float patience = unit(Long.rotateLeft(bits, 17));
        float boldness = unit(Long.rotateLeft(bits, 39));
        return new Temperament(curiosity, patience, boldness);
    }

    private static float unit(long value) {
        return (float)((value >>> 40) & 0xFFFFFFL) / 16777215.0F;
    }

    private static void cleanup(Iterable<ServerLevel> levels) {
        Set<ResourceKey<Level>> activeDimensions = new HashSet<>();
        for (ServerLevel level : levels) activeDimensions.add(level.dimension());
        DISTURBANCES.keySet().removeIf(key -> !activeDimensions.contains(key));
        for (ServerLevel level : levels) {
            ArrayDeque<Disturbance> records = DISTURBANCES.get(level.dimension());
            if (records != null) {
                long gameTime = level.getGameTime();
                records.removeIf(record -> gameTime - record.gameTime() > DISTURBANCE_LIFETIME);
            }
        }
        Set<UUID> online = new HashSet<>();
        for (ServerLevel level : levels) for (ServerPlayer player : level.players()) online.add(player.getUUID());
        PLAYER_PATTERNS.keySet().removeIf(id -> !online.contains(id));
        INVESTIGATION_COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() < 0L);
        // Keep per-entity state bounded when creatures despawn or chunks unload.
        Set<UUID> activeMobs = new HashSet<>();
        for (ServerLevel level : levels) {
            for (ServerPlayer player : level.players()) {
                level.getEntitiesOfClass(Mob.class, new AABB(player.blockPosition()).inflate(96.0D),
                        OutlandsIntelligenceManager::isOutlandsCreature).forEach(mob -> activeMobs.add(mob.getUUID()));
            }
        }
        TERRITORIES.keySet().removeIf(id -> !activeMobs.contains(id));
        INVESTIGATION_COOLDOWNS.keySet().removeIf(id -> !activeMobs.contains(id));
    }

    public static void clear() {
        DISTURBANCES.clear();
        PLAYER_PATTERNS.clear();
        INVESTIGATION_COOLDOWNS.clear();
        TERRITORIES.clear();
        lastCleanup = 0L;
    }

    private record Disturbance(BlockPos pos, UUID source, long gameTime, String kind) {}
    private record Territory(BlockPos center, int radius, long nextReturn) {
        private Territory(BlockPos center, int radius) { this(center, radius, 0L); }
    }
    private record Temperament(float curiosity, float patience, float boldness) {}

    private static final class PlayerPattern {
        private int samples;
        private int sprintSamples;
        private int sneakSamples;
    }
}
