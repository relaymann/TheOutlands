package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.NightmareMode;
import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.NightmareBalancePolicy;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.OutlandsEntities;
import com.relaymann.theoutlands.worldgen.OutlandsStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class NightmareModeManager {
    private static final Map<UUID, Long> NEXT_EVENT = new ConcurrentHashMap<>();
    private static final Map<ServerLevel, Long> BLOOD_MOON_UNTIL = new ConcurrentHashMap<>();
    private static final Map<ServerLevel, Long> BLOOD_MOON_STARTED = new ConcurrentHashMap<>();
    private static final Map<ServerLevel, Integer> BLOOD_MOON_STAGE = new ConcurrentHashMap<>();
    private static final Map<ServerLevel, Long> ASHFALL_UNTIL = new ConcurrentHashMap<>();
    private static final Map<String, StructureState> ACTIVE_STRUCTURES = new ConcurrentHashMap<>();
    private static final Map<UUID, PursuitMemory> PURSUIT_MEMORY = new ConcurrentHashMap<>();
    private static List<net.minecraft.world.entity.EntityType<? extends Mob>> threatTypes() {

        return List.of(OutlandsEntities.STALKER.get(), OutlandsEntities.HUNTER.get(),
                OutlandsEntities.LURKER.get(), OutlandsEntities.DREDGE.get(),
                OutlandsEntities.MIMIC.get(), OutlandsEntities.OBSERVER.get(),
                OutlandsEntities.HOWLER.get(), OutlandsEntities.PILGRIM.get(),
                OutlandsEntities.STILLBORN_STATUE.get(), OutlandsEntities.AGITATED_ONE.get(),
                OutlandsEntities.NIGHTMARE_WATCHER.get(), OutlandsEntities.NIGHTMARE_HOUND.get(),
                OutlandsEntities.NIGHTMARE_WARDENED.get(), OutlandsEntities.NIGHTMARE_FALSE_SURVIVOR.get(),
                OutlandsEntities.NIGHTMARE_UNBOUND.get());
    }

    private NightmareModeManager() {}

    public static boolean isBloodMoonActive(ServerLevel level) {
        return BLOOD_MOON_UNTIL.getOrDefault(level, 0L) > level.getGameTime();
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) NightmareMode.refresh(level);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 20 != 0) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            NightmareMode.refresh(level);
            if (!level.dimension().equals(Level.OVERWORLD)) continue;
            tickBloodMoonStages(level);
            tickAshfall(level);
            if (!NightmareMode.isEnabled(level)) continue;
            for (ServerPlayer player : level.players()) {
                if (!eligible(player)) continue;
                if (OutlandsConfig.ENTITY_ECOSYSTEM_ENABLED.get()) {
                    coordinateNightmarePack(player);
                    migrateNearbyThreats(player);
                }
                if (OutlandsConfig.OUTLANDS_STRUCTURES_ENABLED.get()) tickActiveStructure(player);
                if (!OutlandsConfig.EVENTS_ENABLED.get()) {
                    NEXT_EVENT.remove(player.getUUID());
                    continue;
                }
                long now = level.getGameTime();
                double dx = player.getX() - level.getSharedSpawnPos().getX();
                double dz = player.getZ() - level.getSharedSpawnPos().getZ();
                double distanceFromSpawn = Math.sqrt(dx * dx + dz * dz);
                Long next = NEXT_EVENT.get(player.getUUID());
                if (next == null) {
                    int delay = nextEventDelay(player, level.getDayTime(), distanceFromSpawn);
                    NEXT_EVENT.put(player.getUUID(), now + delay);
                    continue;
                }
                if (now < next) continue;
                triggerEvent(player);
                int delay = nextEventDelay(player, level.getDayTime(), distanceFromSpawn);
                NEXT_EVENT.put(player.getUUID(), now + delay);
            }
        }
        if (event.getServer().getTickCount() % 1200 == 0) {
            for (ServerLevel level : event.getServer().getAllLevels()) {
                long now = level.getGameTime();
                PURSUIT_MEMORY.entrySet().removeIf(entry ->
                        entry.getValue().dimension().equals(level.dimension())
                                && now - entry.getValue().lastSeen() > 3_600L);
            }
        }
    }

    private static int nextEventDelay(ServerPlayer player, long dayTime, double distanceFromSpawn) {
        int baseDelay = NightmareBalancePolicy.nextEventDelayTicks(
                dayTime, distanceFromSpawn, player.getRandom().nextLong());
        return NightmareBalancePolicy.scaleEventDelayTicks(baseDelay, OutlandsConfig.EVENT_FREQUENCY.get());
    }

    private static boolean eligible(ServerPlayer player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator()
                && !player.isSleeping() && !player.isPassenger() && !player.isInWaterOrBubble();
    }

    private static void triggerEvent(ServerPlayer player) {
        if (player.getRandom().nextInt(100) < 3) {
            hollowEncounter(player);
            return;
        }
        if (player.getRandom().nextInt(100) < 5) {
            thePursuit(player);
            return;
        }
        switch (player.getRandom().nextInt(7)) {
            case 0 -> bloodMoon(player);
            case 1 -> silence(player);
            case 2 -> unusualActivity(player);
            case 3 -> containmentFailure(player);
            case 4 -> ashfall(player);
            case 5 -> migrationSurge(player);
            default -> falseSurvivorEncounter(player);
        }
    }

    private static void hollowEncounter(ServerPlayer player) {
        play(player, SoundEvents.WARDEN_HEARTBEAT, SoundSource.AMBIENT, 0.14F, 0.36F);
        spawnThreat(player, OutlandsEntities.NIGHTMARE_WATCHER.get(), true);
        spawnThreat(player, OutlandsEntities.NIGHTMARE_FALSE_SURVIVOR.get(), true);
        spawnThreat(player, OutlandsEntities.NIGHTMARE_HOUND.get(), true);
    }

    private static void bloodMoon(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.setWeatherParameters(0, 2_400, true, true);
        BLOOD_MOON_UNTIL.put(level, level.getGameTime() + 2_400L);
        BLOOD_MOON_STARTED.put(level, level.getGameTime());
        BLOOD_MOON_STAGE.put(level, 0);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 3.0D,
                player.getZ(), 28, 2.2D, 1.2D, 2.2D, 0.015D);
        play(player, SoundEvents.WARDEN_ROAR, SoundSource.AMBIENT, 0.28F, 0.42F);
        spawnThreats(player, 1, true);
    }

    private static void ashfall(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.setWeatherParameters(0, 1_800, true, true);
        ASHFALL_UNTIL.put(level, level.getGameTime() + 1_800L);
        level.sendParticles(ParticleTypes.ASH, player.getX(), player.getY() + 3.0D,
                player.getZ(), 42, 9.0D, 4.0D, 9.0D, 0.015D);
        play(player, SoundEvents.WARDEN_HEARTBEAT, SoundSource.AMBIENT, 0.22F, 0.38F);
        spawnThreat(player, OutlandsEntities.NIGHTMARE_WATCHER.get(), true);
    }

    private static void migrationSurge(ServerPlayer player) {
        play(player, SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 0.22F, 0.7F);
        spawnThreat(player, OutlandsEntities.NIGHTMARE_HOUND.get(), true);
        spawnThreat(player, OutlandsEntities.NIGHTMARE_UNBOUND.get(), true);
        spawnThreats(player, 1, true);
    }

    private static void falseSurvivorEncounter(ServerPlayer player) {
        play(player, SoundEvents.WARDEN_HEARTBEAT, SoundSource.AMBIENT, 0.12F, 0.88F);
        spawnThreat(player, OutlandsEntities.NIGHTMARE_FALSE_SURVIVOR.get(), true);
    }

    private static void silence(ServerPlayer player) {
        if (!OutlandsConfig.HORROR_AUDIO_ENABLED.get()) return;
        player.connection.send(new ClientboundStopSoundPacket(null, SoundSource.AMBIENT));
        player.serverLevel().sendParticles(ParticleTypes.ASH, player.getX(), player.getY() + 1.0D,
                player.getZ(), 12, 0.7D, 0.8D, 0.7D, 0.002D);
    }

    private static void unusualActivity(ServerPlayer player) {
        play(player, SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.24F, 0.55F);
        spawnThreats(player, 2, true);
    }

    private static void thePursuit(ServerPlayer player) {
        play(player, SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.18F, 0.42F);
        spawnThreat(player, OutlandsEntities.STALKER.get(), true);
        spawnThreat(player, OutlandsEntities.STALKER.get(), true);
        spawnThreat(player, OutlandsEntities.HUNTER.get(), true);
    }

    private static void containmentFailure(ServerPlayer player) {
        play(player, SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 0.34F, 0.62F);
        player.serverLevel().sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0D,
                player.getZ(), 18, 1.1D, 0.7D, 1.1D, 0.01D);
        spawnThreats(player, 4, true);
    }

    private static void spawnThreats(ServerPlayer player, int count, boolean empowered) {
        List<net.minecraft.world.entity.EntityType<? extends Mob>> threats = threatTypes();

        int boundedCount = Math.min(2, Math.max(0, count));
        for (int i = 0; i < boundedCount; i++) {
            spawnThreat(player, threats.get(player.getRandom().nextInt(threats.size())), empowered);
        }
    }

    private static void spawnThreat(ServerPlayer player,
                                    net.minecraft.world.entity.EntityType<? extends Mob> type,
                                    boolean empowered) {
        ServerLevel level = player.serverLevel();
        double spawnDx = player.getX() - level.getSharedSpawnPos().getX();
        double spawnDz = player.getZ() - level.getSharedSpawnPos().getZ();
        double distanceFromSpawn = Math.hypot(spawnDx, spawnDz);
        if (!NightmareBalancePolicy.canStartNightmareSpawnEvent(distanceFromSpawn)) return;

        double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;

        double distance = 40.0D + player.getRandom().nextDouble() * 16.0D;
        int x = net.minecraft.util.Mth.floor(player.getX() + Math.cos(angle) * distance);
        int z = net.minecraft.util.Mth.floor(player.getZ() + Math.sin(angle) * distance);
        BlockPos column = new BlockPos(x, net.minecraft.util.Mth.floor(player.getY()), z);
        if (!level.hasChunkAt(column)) return;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos spawn = new BlockPos(x, y, z);
        if (!level.hasChunkAt(spawn)) return;
        BlockPos floor = spawn.below();
        if (!level.getFluidState(spawn).isEmpty()
                || !level.getBlockState(floor).isSolidRender(level, floor)) return;

        int activePack = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(128.0D),
                candidate -> candidate.isAlive()
                        && candidate.getTags().contains("theoutlands_nightmare_spawn")).size();
        if (activePack >= 10) return;

        Mob mob = type.create(level);
        if (mob == null) return;
        mob.moveTo(x + 0.5D, y, z + 0.5D, player.getRandom().nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawn), MobSpawnType.EVENT, null);
        if (!level.noCollision(mob, mob.getBoundingBox())) {
            mob.discard();
            return;
        }
        if (empowered) empower(mob);
        mob.addTag("theoutlands_nightmare_spawn");
        if (type == OutlandsEntities.STALKER.get()) mob.addTag("theoutlands_nightmare_stalker");
        mob.setPersistenceRequired();
        mob.setTarget(player);
        if (!level.addFreshEntity(mob)) mob.discard();
    }

    private static void empower(Mob mob) {
        boolean heavy = mob.getType() == OutlandsEntities.STALKER.get()
                || mob.getType() == OutlandsEntities.NIGHTMARE_WARDENED.get();
        AttributeInstance speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(Math.min(0.72D, speed.getBaseValue() * (heavy ? 1.22D : 1.16D)));
        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(health.getBaseValue() * (heavy ? 1.30D : 1.18D));
            mob.setHealth(mob.getMaxHealth());
        }
        AttributeInstance damage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) damage.setBaseValue(damage.getBaseValue() * (heavy ? 1.24D : 1.14D));
    }

    private static void play(ServerPlayer player, SoundEvent sound, SoundSource source,
                             float volume, float pitch) {
        if (!OutlandsConfig.HORROR_AUDIO_ENABLED.get()) return;
        player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), source,
                player.getX(), player.getY(), player.getZ(), volume, pitch,
                player.getRandom().nextLong()));
    }

    private static void coordinateNightmarePack(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        for (Mob ally : level.getEntitiesOfClass(Mob.class,
                player.getBoundingBox().inflate(160.0D),
                candidate -> candidate.isAlive()
                        && candidate.getTags().contains("theoutlands_nightmare_spawn"))) {
            net.minecraft.world.entity.LivingEntity target = ally.getTarget();
            ServerPlayer targetPlayer = target instanceof ServerPlayer current ? current : null;
            boolean targetUnavailable = NightmareBalancePolicy.shouldReplaceTarget(
                    target != null, target == null || target.isAlive(), targetPlayer != null,
                    targetPlayer == null || targetPlayer.serverLevel() == level,
                    targetPlayer != null && targetPlayer.isCreative(),
                    targetPlayer != null && targetPlayer.isSpectator());
            if (ally.distanceToSqr(player) <= 96.0D * 96.0D && targetUnavailable) {
                ally.setTarget(player);
            }
            if (ally.getTarget() == null || ally.getTarget() == player) {
                updatePursuitMemory(ally, player);
            }
        }
    }

    private static void updatePursuitMemory(Mob mob, ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        boolean currentlyVisible = mob.getTarget() == player
                && mob.hasLineOfSight(player)
                && mob.distanceToSqr(player) <= 128.0D * 128.0D;
        if (currentlyVisible) {
            PURSUIT_MEMORY.put(mob.getUUID(), new PursuitMemory(player.getUUID(), level.dimension(),
                    player.blockPosition().immutable(), now, now));
            return;
        }

        PursuitMemory memory = PURSUIT_MEMORY.get(mob.getUUID());
        if (mob.getTarget() == player && memory != null) mob.setTarget(null);
        if (memory == null || !memory.dimension().equals(level.dimension())
                || now - memory.lastSeen() > 3_600L) {
            PURSUIT_MEMORY.remove(mob.getUUID());
            return;
        }

        ServerPlayer remembered = level.getServer().getPlayerList().getPlayer(memory.player());
        if (remembered != null && remembered.serverLevel() == level
                && mob.distanceToSqr(remembered) <= 128.0D * 128.0D
                && mob.hasLineOfSight(remembered)) {
            mob.setTarget(remembered);
            PURSUIT_MEMORY.put(mob.getUUID(), new PursuitMemory(memory.player(), memory.dimension(),
                    remembered.blockPosition().immutable(), now, memory.lastPath()));
            return;
        }
        if (now - memory.lastPath() >= 40L && level.isLoaded(memory.lastPosition())
                && mob.getNavigation().isDone()) {
            mob.getNavigation().moveTo(memory.lastPosition().getX() + 0.5D,
                    memory.lastPosition().getY(), memory.lastPosition().getZ() + 0.5D, 0.92D);
            PURSUIT_MEMORY.put(mob.getUUID(), new PursuitMemory(memory.player(), memory.dimension(),
                    memory.lastPosition(), memory.lastSeen(), now));
        }
    }

    private static void migrateNearbyThreats(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        long clock = Math.floorMod(level.getDayTime(), 24_000L);
        if (clock < 13_000L || clock > 23_000L) return;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(112.0D),
                NightmareModeManager::isOutlandsThreat)) {
            if (!mob.isAlive() || mob.getTarget() != null || mob.isPassenger()
                    || mob.tickCount % 100 != 0 || mob.getRandom().nextInt(5) != 0) continue;
            double distance = mob.distanceToSqr(player);
            if (distance < 36.0D * 36.0D || distance > 112.0D * 112.0D) continue;
            Vec3 away = mob.position().subtract(player.position());
            if (away.lengthSqr() < 0.001D) away = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 destination = player.position().add(away.normalize().scale(20.0D));
            BlockPos target = BlockPos.containing(destination.x, destination.y, destination.z);
            if (level.isLoaded(target)) mob.getNavigation().moveTo(target.getX() + 0.5D,
                    target.getY(), target.getZ() + 0.5D, 0.82D);
        }
    }

    private static boolean isOutlandsThreat(Mob mob) {
        var type = mob.getType();
        return type == OutlandsEntities.NULL.get() || type == OutlandsEntities.FEAR.get()
                || type == OutlandsEntities.DEATH.get() || type == OutlandsEntities.NULL_APOSTLE.get()
                || type == OutlandsEntities.CAVE_JAR.get() || type == OutlandsEntities.STALKER.get()
                || type == OutlandsEntities.OBSERVER.get() || type == OutlandsEntities.HUNTER.get()
                || type == OutlandsEntities.BURROWER.get() || type == OutlandsEntities.MIMIC.get()
                || type == OutlandsEntities.STILLBORN_STATUE.get() || type == OutlandsEntities.AGITATED_ONE.get()
                || type == OutlandsEntities.LURKER.get() || type == OutlandsEntities.HOWLER.get()
                || type == OutlandsEntities.DREDGE.get() || type == OutlandsEntities.PILGRIM.get()
                || type == OutlandsEntities.CEILING_DWELLER.get() || type == OutlandsEntities.COLOSSUS.get()
                || type == OutlandsEntities.NIGHTMARE_WATCHER.get() || type == OutlandsEntities.NIGHTMARE_HOUND.get()
                || type == OutlandsEntities.NIGHTMARE_WARDENED.get()
                || type == OutlandsEntities.NIGHTMARE_FALSE_SURVIVOR.get()
                || type == OutlandsEntities.NIGHTMARE_UNBOUND.get();
    }

    private static void tickActiveStructure(ServerPlayer player) {
        if (!OutlandsConfig.OUTLANDS_STRUCTURES_ENABLED.get()) return;
        ServerLevel level = player.serverLevel();
        var start = level.structureManager().getStructureWithPieceAt(player.blockPosition(),
                holder -> holder.value() instanceof OutlandsStructure);
        if (start == null || !start.isValid()) return;
        var bounds = start.getBoundingBox();
        if (!bounds.isInside(player.blockPosition())) return;
        String key = level.dimension().location() + ":" + level.getSeed()
                + ":" + bounds.minX() + ":" + bounds.minZ();
        long now = level.getGameTime();
        StructureState state = ACTIVE_STRUCTURES.computeIfAbsent(key, ignored -> new StructureState(now, 0));
        long elapsed = now - state.startedAt();
        int stage = state.stage();
        if (stage == 0 && elapsed >= 240L) {
            level.sendParticles(ParticleTypes.ASH, bounds.minX() + 24.0D, bounds.maxY() - 6.0D,
                    bounds.minZ() + 24.0D, 24, 5.0D, 2.0D, 5.0D, 0.006D);
            play(player, SoundEvents.WARDEN_HEARTBEAT, SoundSource.AMBIENT, 0.18F, 0.45F);
            stage = 1;
        }
        if (stage == 1 && elapsed >= 600L) {
            spawnThreats(player, 2, true);
            stage = 2;
        }
        if (stage == 2 && elapsed >= 1_200L) {
            spawnThreat(player, OutlandsEntities.NIGHTMARE_WARDENED.get(), true);
            spawnThreat(player, OutlandsEntities.NIGHTMARE_UNBOUND.get(), true);
            stage = 3;
        }
        if (stage != state.stage()) {
            ACTIVE_STRUCTURES.put(key, new StructureState(state.startedAt(), stage));
            if (ACTIVE_STRUCTURES.size() > 512) {
                var oldest = ACTIVE_STRUCTURES.entrySet().stream()
                        .min(java.util.Comparator.comparingLong(entry -> entry.getValue().startedAt()))
                        .orElse(null);
                if (oldest != null) ACTIVE_STRUCTURES.remove(oldest.getKey());
            }
        }
    }

    private static void tickBloodMoonStages(ServerLevel level) {
        Long started = BLOOD_MOON_STARTED.get(level);
        Long until = BLOOD_MOON_UNTIL.get(level);
        if (started == null || until == null || level.getGameTime() >= until) {
            BLOOD_MOON_STARTED.remove(level);
            BLOOD_MOON_UNTIL.remove(level);
            BLOOD_MOON_STAGE.remove(level);
            return;
        }
        long elapsed = level.getGameTime() - started;
        int stage = elapsed >= 1_800L ? 3 : elapsed >= 1_200L ? 2 : elapsed >= 600L ? 1 : 0;
        int previous = BLOOD_MOON_STAGE.getOrDefault(level, 0);
        if (stage <= previous) return;
        BLOOD_MOON_STAGE.put(level, stage);
        ServerPlayer player = level.players().stream().filter(NightmareModeManager::eligible).findFirst().orElse(null);
        if (player == null) return;
        if (stage == 1) {
            spawnThreat(player, OutlandsEntities.NIGHTMARE_HOUND.get(), true);
        } else if (stage == 2) {
            spawnThreat(player, OutlandsEntities.NIGHTMARE_WARDENED.get(), true);
        } else {
            spawnThreat(player, OutlandsEntities.NIGHTMARE_UNBOUND.get(), true);
            spawnThreat(player, OutlandsEntities.NIGHTMARE_HOUND.get(), true);
        }
    }

    private static void tickAshfall(ServerLevel level) {
        Long until = ASHFALL_UNTIL.get(level);
        if (until == null) return;
        if (level.getGameTime() >= until) {
            ASHFALL_UNTIL.remove(level);
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (eligible(player)) {
                level.sendParticles(ParticleTypes.ASH, player.getX(), player.getY() + 2.5D,
                        player.getZ(), 18, 8.0D, 3.0D, 8.0D, 0.008D);
            }
        }
    }

    @SubscribeEvent
    public static void onGameModeChange(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !NightmareMode.isEnabled(player.serverLevel())) return;
        if (player.isAlive() && event.getNewGameMode() != net.minecraft.world.level.GameType.SURVIVAL) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        NEXT_EVENT.remove(playerId);
        PURSUIT_MEMORY.entrySet().removeIf(entry -> entry.getValue().player().equals(playerId));
    }

    private record StructureState(long startedAt, int stage) {}
    private record PursuitMemory(UUID player, ResourceKey<Level> dimension,
                                 BlockPos lastPosition, long lastSeen, long lastPath) {}

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        NEXT_EVENT.clear();
        BLOOD_MOON_UNTIL.clear();
        BLOOD_MOON_STARTED.clear();
        BLOOD_MOON_STAGE.clear();
        ASHFALL_UNTIL.clear();
        ACTIVE_STRUCTURES.clear();
        PURSUIT_MEMORY.clear();
        NightmareMode.clear();
    }
}
