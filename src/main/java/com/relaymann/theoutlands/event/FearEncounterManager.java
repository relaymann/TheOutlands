package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.entity.FearEntity;
import com.relaymann.theoutlands.entity.OutlandsEntities;
import com.relaymann.theoutlands.horror.AlmightyTerritory;
import com.relaymann.theoutlands.horror.DistanceProgression;
import com.relaymann.theoutlands.horror.FearEncounterPolicy;
import com.relaymann.theoutlands.horror.HorrorMemory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FearEncounterManager {
    private static final String INTRODUCTION_DISCOVERY = "fear_first_introduction";
    private static final long LIFETIME_TICKS = 160L;
    private static final double AUDIENCE_RADIUS_SQR = 64.0D * 64.0D;
    private static final Map<UUID, ActiveManifestation> ACTIVE = new HashMap<>();

    private FearEncounterManager() {}

    public static void evaluate(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || player.isCreative()
                || !player.serverLevel().dimension().equals(Level.OVERWORLD)) return;
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

        List<ServerPlayer> audience = new ArrayList<>();
        for (ServerPlayer candidate : player.serverLevel().players()) {
            if (candidate.isAlive() && !candidate.isSpectator() && !candidate.isCreative()
                    && candidate.distanceToSqr(player) <= AUDIENCE_RADIUS_SQR) audience.add(candidate);
        }
        if (audience.stream().noneMatch(candidate -> candidate.getUUID().equals(player.getUUID()))) {
            audience.add(player);
        }

        for (ServerPlayer witness : audience) {
            HorrorMemory.recordDiscovery(witness, INTRODUCTION_DISCOVERY);
            HorrorMemory.setLastAggressiveJumpscare(witness, witness.serverLevel().getGameTime());
        }
    }

    public static void showPresence(ServerPlayer player) {
        spawnManifestation(player);
    }

    private static FearEntity spawnManifestation(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        UUID playerId = player.getUUID();
        if (ACTIVE.values().stream().anyMatch(active ->
                active.level == level && active.playerId.equals(playerId))) return null;

        double yawRadians = Math.toRadians(player.getYRot());
        double forwardX = -Math.sin(yawRadians);
        double forwardZ = Math.cos(yawRadians);
        double x = player.getX() + forwardX * 4.0D;
        double z = player.getZ() + forwardZ * 4.0D;
        double y = player.getY() + 2.8D;
        BlockPos position = BlockPos.containing(x, y, z);
        if (!level.hasChunkAt(position) || !level.hasChunkAt(position.below())) return null;

        FearEntity fear = OutlandsEntities.FEAR.get().create(level);
        if (fear == null) return null;

        double dx = player.getX() - x;
        double dz = player.getZ() - z;
        double horizontalDistance = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        double dy = player.getEyeY() - (y + fear.getEyeHeight());
        float facingYaw = (float) (Math.toDegrees(Math.atan2(-dx, dz)));
        float facingPitch = (float) -Math.toDegrees(Math.atan2(dy, horizontalDistance));

        fear.moveTo(x, y, z, facingYaw, facingPitch);
        fear.setYHeadRot(facingYaw);
        fear.setYBodyRot(facingYaw);
        fear.setInvulnerable(true);
        fear.setSilent(true);
        fear.setNoAi(true);
        fear.setNoGravity(true);
        fear.setTemporaryExpiresAt(level.getGameTime() + LIFETIME_TICKS);
        fear.addTag("theoutlands_fear_manifestation");
        if (!level.noCollision(fear, fear.getBoundingBox())) return null;
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
