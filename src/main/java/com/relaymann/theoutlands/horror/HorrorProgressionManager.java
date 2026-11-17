package com.relaymann.theoutlands.horror;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the latest derived horror snapshot for online players.
 *
 * <p>Writes are made from server gameplay events. World mutations remain server-thread-only.</p>
 */
public final class HorrorProgressionManager {
    private static final Map<UUID, HorrorSnapshot> SNAPSHOTS = new ConcurrentHashMap<>();

    private HorrorProgressionManager() {
    }

    public static void update(ServerPlayer player) {
        SNAPSHOTS.put(player.getUUID(), DistanceProgression.createSnapshot(player));
    }

    public static Optional<HorrorSnapshot> getSnapshot(UUID playerId) {
        return Optional.ofNullable(SNAPSHOTS.get(playerId));
    }

    public static void remove(UUID playerId) {
        SNAPSHOTS.remove(playerId);
    }

    public static void clear() {
        SNAPSHOTS.clear();
    }
}
