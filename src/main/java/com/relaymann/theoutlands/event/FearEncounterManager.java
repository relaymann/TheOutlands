package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.entity.FearEntity;
import com.relaymann.theoutlands.entity.OutlandsEntities;
import com.relaymann.theoutlands.horror.AlmightyTerritory;
import com.relaymann.theoutlands.horror.DistanceProgression;
import com.relaymann.theoutlands.horror.FearEncounterPolicy;
import com.relaymann.theoutlands.horror.HorrorMemory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Owns Fear's first meeting and bounded, player-specific manifestations. */
public final class FearEncounterManager {
    private static final String INTRODUCTION_DISCOVERY = "fear_first_introduction";
    private static final long LIFETIME_TICKS = 160L;
    private static final Map<UUID, ActiveManifestation> ACTIVE = new HashMap<>();

    private FearEncounterManager() {}

    public static void evaluate(ServerPlayer player) {
        if (!player.serverLevel().dimension().equals(Level.OVERWORLD)) return;
        double distance = DistanceProgression.horizontalDistanceFromWorldSpawn(player);
        boolean almighty = AlmightyTerritory.contains(player.serverLevel(), player.getX(), player.getZ());
        if (FearEncounterPolicy.shouldIntroduce(distance, OutlandsConfig.FEAR_FIRST_APPEARANCE_DISTANCE.get(),
                HorrorMemory.hasDiscovered(player, INTRODUCTION_DISCOVERY), almighty)) {
            introduce(player);
        }
    }

    private static void introduce(ServerPlayer player) {
        FearEntity fear = spawnManifestation(player);
        if (fear == null) return;
        HorrorMemory.recordDiscovery(player, INTRODUCTION_DISCOVERY);
        Component line = Component.literal("You are the first person to reach me. You are worthy of my fear.");
        for (ServerPlayer audience : player.serverLevel().players()) {
            if (audience.distanceToSqr(player) <= 64.0D * 64.0D) {
                HorrorMemory.recordDiscovery(audience, INTRODUCTION_DISCOVERY);
                audience.sendSystemMessage(line);
            }
        }
    }

    /** Reuses Fear's registered entity for later presence events without replaying the introduction. */
    public static void showPresence(ServerPlayer player) {
        spawnManifestation(player);
    }

    private static FearEntity spawnManifestation(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        UUID playerId = player.getUUID();
        if (ACTIVE.values().stream().anyMatch(active ->
                active.level == level && active.playerId.equals(playerId))) return null;

        float yaw = player.getYRot();
        double angle = Math.toRadians(yaw);
        double x = player.getX() - Math.sin(angle) * 3.5D;
        double z = player.getZ() + Math.cos(angle) * 3.5D;
        double y = player.getY() + 1.6D;
        if (!level.hasChunkAt(BlockPos.containing(x, y, z))) return null;

        FearEntity fear = OutlandsEntities.FEAR.get().create(level);
        if (fear == null) return null;
        fear.moveTo(x, y, z, yaw + 180.0F, 0.0F);
        fear.setInvulnerable(true);
        fear.setSilent(true);
        fear.setNoAi(true);
        fear.setNoGravity(true);
        fear.addTag("theoutlands_fear_manifestation");
        if (!level.addFreshEntity(fear)) return null;

        ACTIVE.put(fear.getUUID(), new ActiveManifestation(level, fear.getUUID(), playerId,
                level.getGameTime() + LIFETIME_TICKS));
        return fear;
    }

    public static void tick(ServerLevel level) {
        Iterator<Map.Entry<UUID, ActiveManifestation>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            ActiveManifestation active = iterator.next().getValue();
            if (active.level != level) continue;
            Entity entity = level.getEntity(active.entityId);
            if (entity == null || level.getGameTime() >= active.expiresAt) {
                if (entity != null) entity.discard();
                iterator.remove();
            }
        }
    }

    public static void clear() {
        for (ActiveManifestation active : ACTIVE.values()) {
            Entity entity = active.level.getEntity(active.entityId);
            if (entity != null) entity.discard();
        }
        ACTIVE.clear();
    }

    private record ActiveManifestation(ServerLevel level, UUID entityId, UUID playerId, long expiresAt) {}
}
