package com.relaymann.theoutlands.horror;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds derived progression snapshots and transient encounter pacing state for online players.
 *
 * <p>All writes are made from server gameplay events. World mutations remain server-thread-only.</p>
 */
public final class HorrorProgressionManager {
    private static final Map<UUID, HorrorSnapshot> SNAPSHOTS = new ConcurrentHashMap<>();
    private static final Map<UUID, HorrorDirectorState> DIRECTOR_STATES = new ConcurrentHashMap<>();
    private static final Map<UUID, HorrorDecision> LAST_DECISIONS = new ConcurrentHashMap<>();

    private HorrorProgressionManager() {
    }

    public static void update(ServerPlayer player) {
        UUID playerId = player.getUUID();
        HorrorSnapshot snapshot = DistanceProgression.createSnapshot(player);
        HorrorDirectorState state = DIRECTOR_STATES.computeIfAbsent(playerId, ignored -> new HorrorDirectorState());
        HorrorDecision decision = HorrorDirector.evaluate(
                snapshot,
                state,
                player.serverLevel().getGameTime(),
                player.getRandom().nextDouble()
        );

        SNAPSHOTS.put(playerId, snapshot);
        LAST_DECISIONS.put(playerId, decision);
    }

    public static Optional<HorrorSnapshot> getSnapshot(UUID playerId) {
        return Optional.ofNullable(SNAPSHOTS.get(playerId));
    }

    public static Optional<HorrorDecision> getLastDecision(UUID playerId) {
        return Optional.ofNullable(LAST_DECISIONS.get(playerId));
    }

    public static void remove(UUID playerId) {
        SNAPSHOTS.remove(playerId);
        DIRECTOR_STATES.remove(playerId);
        LAST_DECISIONS.remove(playerId);
    }

    public static void clear() {
        SNAPSHOTS.clear();
        DIRECTOR_STATES.clear();
        LAST_DECISIONS.clear();
    }
}
