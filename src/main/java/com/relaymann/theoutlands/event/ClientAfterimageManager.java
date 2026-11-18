package com.relaymann.theoutlands.event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Sends temporary block-state lies to one client only. The logical server world is never
 * changed; every fake state is restored from the live world state after a short interval.
 */
public final class ClientAfterimageManager {
    private static final Map<UUID, Map<BlockPos, PendingRestore>> PENDING = new HashMap<>();
    private ClientAfterimageManager() {}

    public static void showTerrainAfterimage(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
        int centerX = (int) Math.floor(player.getX() + Math.cos(angle) * 13.0D);
        int centerZ = (int) Math.floor(player.getZ() + Math.sin(angle) * 13.0D);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int x = centerX + dx;
                int z = centerZ + dz;
                if (!level.hasChunkAt(new BlockPos(x, player.blockPosition().getY(), z))) continue;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                BlockPos pos = new BlockPos(x, y, z);
                BlockState original = level.getBlockState(pos);
                if (original.isAir() || original.hasBlockEntity()) continue;
                BlockState ghost = ((dx + dz) & 1) == 0
                        ? Blocks.MOSS_BLOCK.defaultBlockState()
                        : Blocks.ROOTED_DIRT.defaultBlockState();
                sendFake(player, pos, ghost, 28);
            }
        }
    }

    /** A distant, blocky human outline briefly occupies the edge of the player's view. */
    public static void showSilhouette(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double yaw = Math.toRadians(player.getYRot());
        double side = player.getRandom().nextBoolean() ? 1.0D : -1.0D;
        double distance = 15.0D + player.getRandom().nextDouble() * 7.0D;
        int x = (int) Math.floor(player.getX() + Math.sin(yaw) * distance
                + Math.cos(yaw) * side * 3.0D);
        int z = (int) Math.floor(player.getZ() - Math.cos(yaw) * distance
                + Math.sin(yaw) * side * 3.0D);
        if (!level.hasChunkAt(new BlockPos(x, player.blockPosition().getY(), z))) return;
        int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockState shadow = Blocks.BLACK_STAINED_GLASS.defaultBlockState();
        int[][] shape = {{0,1},{0,2},{0,3},{-1,2},{1,2},{0,0},{-1,0},{1,0}};
        for (int[] part : shape) {
            BlockPos pos = new BlockPos(x + part[0], ground + part[1], z);
            if (level.hasChunkAt(pos) && level.getBlockState(pos).isAir()) sendFake(player, pos, shadow, 34);
        }
    }

    private static void sendFake(ServerPlayer player, BlockPos pos, BlockState fake, int duration) {
        Map<BlockPos, PendingRestore> blocks = PENDING.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>());
        if (blocks.containsKey(pos)) return;
        ServerLevel level = player.serverLevel();
        blocks.put(pos.immutable(), new PendingRestore(level, level.getGameTime() + duration));
        player.connection.send(new ClientboundBlockUpdatePacket(pos, fake));
    }

    public static void tick(ServerLevel level) {
        Iterator<Map.Entry<UUID, Map<BlockPos, PendingRestore>>> players = PENDING.entrySet().iterator();
        while (players.hasNext()) {
            Map.Entry<UUID, Map<BlockPos, PendingRestore>> entry = players.next();
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            Iterator<Map.Entry<BlockPos, PendingRestore>> blocks = entry.getValue().entrySet().iterator();
            while (blocks.hasNext()) {
                Map.Entry<BlockPos, PendingRestore> block = blocks.next();
                PendingRestore pending = block.getValue();
                if (pending.level != level) continue;
                if (player == null || player.serverLevel() != level) {
                    blocks.remove();
                    continue;
                }
                if (level.getGameTime() >= pending.expiresAt) {
                    if (level.hasChunkAt(block.getKey())) {
                        player.connection.send(new ClientboundBlockUpdatePacket(
                                block.getKey(), level.getBlockState(block.getKey())));
                    }
                    blocks.remove();
                }
            }
            if (entry.getValue().isEmpty()) players.remove();
        }
    }

    public static void clear(ServerPlayer player) {
        Map<BlockPos, PendingRestore> blocks = PENDING.remove(player.getUUID());
        if (blocks == null) return;
        for (BlockPos pos : blocks.keySet()) {
            PendingRestore pending = blocks.get(pos);
            if (pending.level == player.serverLevel() && pending.level.hasChunkAt(pos)) {
                player.connection.send(new ClientboundBlockUpdatePacket(pos, pending.level.getBlockState(pos)));
            }
        }
    }

    public static void clear() { PENDING.clear(); }
    private record PendingRestore(ServerLevel level, long expiresAt) {}
}
