package com.relaymann.theoutlands.event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Interim, short-lived watcher silhouette; dedicated Null model and AI are a later milestone. */
public final class NullWatcherManager {
    private static final long LIFETIME = 260L;
    private static final Map<UUID, ActiveWatcher> ACTIVE = new HashMap<>();
    private NullWatcherManager() {}

    public static void spawn(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double yaw = Math.toRadians(player.getYRot());
        double angle = yaw + (player.getRandom().nextDouble() - 0.5D) * 0.9D;
        double distance = 20.0D + player.getRandom().nextDouble() * 9.0D;
        double x = player.getX() - Math.sin(angle) * distance;
        double z = player.getZ() + Math.cos(angle) * distance;
        int bx = (int) Math.floor(x), bz = (int) Math.floor(z);
        if (!level.hasChunkAt(new BlockPos(bx, player.blockPosition().getY(), bz))) return;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
        BlockPos pos = new BlockPos(bx, y, bz);
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()
                || !level.getBlockState(pos.above(2)).isAir()) return;
        Entity entity = EntityType.ENDERMAN.create(level);
        if (!(entity instanceof EnderMan watcher)) return;
        watcher.moveTo(x + 0.5D, y, z + 0.5D, player.getYRot() + 180.0F, 0.0F);
        watcher.setNoAi(true);
        watcher.setInvulnerable(true);
        watcher.setSilent(true);
        watcher.setCustomName(Component.literal("Null"));
        watcher.setCustomNameVisible(false);
        watcher.addTag("theoutlands_null_watcher");
        if (level.addFreshEntity(watcher)) ACTIVE.put(watcher.getUUID(),
                new ActiveWatcher(level, watcher.getUUID(), level.getGameTime() + LIFETIME));
    }

    /** Visits only watcher UUIDs created by this system, never all loaded entities. */
    public static void tick(ServerLevel level) {
        Iterator<ActiveWatcher> it = ACTIVE.values().iterator();
        while (it.hasNext()) {
            ActiveWatcher watcher = it.next();
            if (watcher.level() != level) continue;
            Entity entity = level.getEntity(watcher.id());
            if (entity == null || level.getGameTime() >= watcher.expiresAt()) {
                if (entity != null) entity.discard();
                it.remove();
            }
        }
    }
    public static void clear() {
        for (ActiveWatcher watcher : ACTIVE.values()) {
            Entity entity = watcher.level().getEntity(watcher.id());
            if (entity != null) entity.discard();
        }
        ACTIVE.clear();
    }
    private record ActiveWatcher(ServerLevel level, UUID id, long expiresAt) {}
}
