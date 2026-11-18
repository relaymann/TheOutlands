package com.relaymann.theoutlands.horror;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.event.HorrorEventExecutor;\nimport com.relaymann.theoutlands.event.FearEncounterManager;
import com.relaymann.theoutlands.event.NullWatcherManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Server-side derived progression, local context, and transient encounter state. */
public final class HorrorProgressionManager {
    private static final Map<UUID, HorrorSnapshot> SNAPSHOTS = new ConcurrentHashMap<>();
    private static final Map<UUID, HorrorDirectorState> STATES = new ConcurrentHashMap<>();
    private static final Map<UUID, HorrorDecision> DECISIONS = new ConcurrentHashMap<>();
    private HorrorProgressionManager() {}

    public static void update(ServerPlayer player) {
        UUID id = player.getUUID();
        HorrorSnapshot snapshot = DistanceProgression.createSnapshot(player);\n        FearEncounterManager.evaluate(player);
        HorrorDirectorState state = STATES.computeIfAbsent(id, ignored -> new HorrorDirectorState());
        var level = player.serverLevel();
        var key = level.registryAccess().registryOrThrow(Registries.BIOME).getKey(level.getBiome(player.blockPosition()).value());
        long time = level.getDayTime() % 24000L;
        int nearby = level.getEntitiesOfClass(ServerPlayer.class,
                new AABB(player.blockPosition()).inflate(48.0D),
                other -> !other.getUUID().equals(id)).size();
        float health = player.getMaxHealth() <= 0.0F ? 1.0F : player.getHealth() / player.getMaxHealth();
        int encounterCount = HorrorMemory.totalEncounters(player);
        boolean hasSeenNull = HorrorMemory.hasEncountered(player, "distant_figure")
                || HorrorMemory.hasEncountered(player, "the_return");
        HorrorContext context = new HorrorContext(key == null ? "unknown" : key.toString(),
                time >= 13000L && time <= 23000L, level.isRaining(), level.isThundering(),
                nearby, health, encounterCount, hasSeenNull);
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
