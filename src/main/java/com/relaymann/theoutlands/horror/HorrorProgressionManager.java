package com.relaymann.theoutlands.horror;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.event.HorrorEventExecutor;
import com.relaymann.theoutlands.event.FearEncounterManager;
import com.relaymann.theoutlands.event.NullWatcherManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Server-side derived progression, behavioral memory, and player-local encounter state. */
public final class HorrorProgressionManager {
    private static final Map<UUID, HorrorSnapshot> SNAPSHOTS = new ConcurrentHashMap<>();
    private static final Map<UUID, HorrorDirectorState> STATES = new ConcurrentHashMap<>();
    private static final Map<UUID, HorrorDecision> DECISIONS = new ConcurrentHashMap<>();
    private HorrorProgressionManager() {}

    public static void update(ServerPlayer player) {
        UUID id = player.getUUID();
        HorrorSnapshot snapshot = DistanceProgression.createSnapshot(player);
        FearEncounterManager.evaluate(player);
        HorrorMemory.recordBehaviorSample(player);
        boolean revisited = HorrorMemory.recordLocation(player);
        HorrorDirectorState state = STATES.computeIfAbsent(id, ignored -> new HorrorDirectorState());
        var level = player.serverLevel();
        var key = level.registryAccess().registryOrThrow(Registries.BIOME).getKey(level.getBiome(player.blockPosition()).value());
        long time = level.getDayTime() % 24000L;
        int nearby = level.getEntitiesOfClass(ServerPlayer.class,
                new AABB(player.blockPosition()).inflate(48.0D),
                other -> !other.getUUID().equals(id)).size();
        float health = player.getMaxHealth() <= 0.0F ? 1.0F : player.getHealth() / player.getMaxHealth();
        int encounterCount = HorrorMemory.totalEncounters(player);
        boolean hasSeenNull = HorrorMemory.hasDiscovered(player, "null_first_sighting");
        boolean hasMetFear = HorrorMemory.hasDiscovered(player, "fear_first_introduction");
        double borderDistance = AlmightyTerritory.distanceInsideBorder(level, player.getX(), player.getZ());
        boolean nearAlmighty = Double.isFinite(borderDistance)
                && borderDistance >= -128.0D
                && borderDistance <= OutlandsConfig.ALMIGHTY_TERRITORY_DEPTH.get() + 1_200.0D
                && level.getGameTime() - HorrorMemory.lastAlmightySign(player) >= 24_000L;
        HorrorContext context = new HorrorContext(key == null ? "unknown" : key.toString(),
                time >= 13000L && time <= 23000L, level.isRaining(), level.isThundering(),
                nearby, health, encounterCount, hasSeenNull, hasMetFear,
                HorrorMemory.lookBackCount(player), HorrorMemory.pauseCount(player), nearAlmighty);

        // A return to a remembered area can trigger a one-player-only distortion.
        long now = level.getGameTime();
        if (revisited && snapshot.escalationValue() > 0.0D
                && now - HorrorMemory.lastFamiliarDistortion(player) >= 6_000L
                && player.getRandom().nextInt(4) == 0) {
            HorrorEventExecutor.executeFamiliarPlaceDistortion(player);
            HorrorMemory.setLastFamiliarDistortion(player, now);
        }

        HorrorSettings settings = new HorrorSettings(OutlandsConfig.HORROR_INTENSITY.get(),
                OutlandsConfig.EVENT_FREQUENCY.get(), OutlandsConfig.EVENTS_ENABLED.get());
        HorrorDecision decision = HorrorDirector.evaluate(snapshot, state, context,
                level.getGameTime(), player.getRandom().nextDouble(), settings);
        SNAPSHOTS.put(id, snapshot);
        DECISIONS.put(id, decision);
        if (decision.hasSelectedEvent()) HorrorEventExecutor.execute(player, decision);
    }

    public static Optional<HorrorSnapshot> getSnapshot(UUID id) { return Optional.ofNullable(SNAPSHOTS.get(id)); }
    public static Optional<HorrorDecision> getLastDecision(UUID id) { return Optional.ofNullable(DECISIONS.get(id)); }
    public static void remove(UUID id) { SNAPSHOTS.remove(id); STATES.remove(id); DECISIONS.remove(id); }
    public static void clear() { SNAPSHOTS.clear(); STATES.clear(); DECISIONS.clear(); NullWatcherManager.clear(); }
}
