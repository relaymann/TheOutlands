package com.relaymann.theoutlands.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Short-lived, server-authoritative sound stimuli. Noise is deliberately transient:
 * entities remember the location they investigate, not a permanent global sound log.
 */
public final class CreatureNoise {
    public enum Kind { FOOTSTEP, SPRINT, DOOR, BLOCK_BREAK, ALARM }

    public record Stimulus(long id, Vec3 position, UUID source, Kind kind, long createdAt, float strength) {}

    private static final Map<ServerLevel, List<Stimulus>> ACTIVE = new WeakHashMap<>();
    private static long nextId;

    private CreatureNoise() {}

    public static void emit(ServerLevel level, Vec3 position, Player source, Kind kind, float strength) {
        if (level == null || position == null) return;
        List<Stimulus> stimuli = ACTIVE.computeIfAbsent(level, ignored -> new ArrayList<>());
        stimuli.add(new Stimulus(++nextId, position, source == null ? null : source.getUUID(),
                kind, level.getGameTime(), Math.max(0.0F, Math.min(1.0F, strength))));
        prune(level, stimuli);
    }

    public static Stimulus hear(ServerLevel level, Vec3 listener, double hearingRange, long afterId) {
        List<Stimulus> stimuli = ACTIVE.computeIfAbsent(level, ignored -> new ArrayList<>());
        prune(level, stimuli);
        Stimulus best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Stimulus stimulus : stimuli) {
            if (stimulus.id() <= afterId || level.getGameTime() - stimulus.createdAt() > 50L) continue;
            double distanceSqr = listener.distanceToSqr(stimulus.position());
            double effectiveRange = hearingRange * (0.35D + stimulus.strength() * 0.65D);
            if (distanceSqr > effectiveRange * effectiveRange) continue;
            double score = stimulus.strength() - Math.sqrt(distanceSqr) / Math.max(1.0D, hearingRange);
            if (score > bestScore) {
                best = stimulus;
                bestScore = score;
            }
        }
        return best;
    }

    private static void prune(ServerLevel level, List<Stimulus> stimuli) {
        long now = level.getGameTime();
        Iterator<Stimulus> iterator = stimuli.iterator();
        while (iterator.hasNext()) {
            if (now - iterator.next().createdAt() > 50L) iterator.remove();
        }
        if (stimuli.size() > 512) stimuli.subList(0, stimuli.size() - 512).clear();
    }
}
