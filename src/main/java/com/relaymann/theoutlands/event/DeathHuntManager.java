package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.entity.DeathEntity;
import com.relaymann.theoutlands.entity.OutlandsEntities;
import com.relaymann.theoutlands.horror.AlmightyTerritory;
import com.relaymann.theoutlands.horror.DistanceProgression;
import com.relaymann.theoutlands.horror.HorrorMemory;
import com.relaymann.theoutlands.item.OutlandsItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/** Spawns Death as a persistent, target-aware hunter and advances the death.jar clue chain. */
public final class DeathHuntManager {
    private static final long RESPAWN_DELAY = 12_000L;
    private DeathHuntManager() {}

    public static void evaluate(ServerPlayer player) {
        updateClueInventory(player);
        var level = player.serverLevel();
        if (DistanceProgression.horizontalDistanceFromWorldSpawn(player)
                < OutlandsConfig.DEATH_FIRST_APPEARANCE_DISTANCE.get()
                || AlmightyTerritory.contains(level, player.getX(), player.getZ())) return;

        long now = level.getGameTime();
        if (now - HorrorMemory.lastDeathSpawn(player) < RESPAWN_DELAY) return;
        if (!level.getEntitiesOfClass(DeathEntity.class,
                new AABB(player.blockPosition()).inflate(512.0D), DeathEntity::isAlive).isEmpty()) return;

        double yaw = Math.toRadians(player.getYRot());
        double distance = 19.0D + player.getRandom().nextDouble() * 7.0D;
        double x = player.getX() + Math.sin(yaw) * distance;
        double z = player.getZ() - Math.cos(yaw) * distance;
        BlockPos column = BlockPos.containing(x, player.getY(), z);
        if (!level.hasChunkAt(column)) return;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
        BlockPos spawn = new BlockPos(column.getX(), y, column.getZ());
        if (!level.hasChunkAt(spawn)) return;

        DeathEntity death = OutlandsEntities.DEATH.get().create(level);
        if (death == null) return;
        death.moveTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D,
                player.getYRot() + 180.0F, 0.0F);
        death.setHuntTarget(player);
        if (!level.noCollision(death, death.getBoundingBox()) || !level.addFreshEntity(death)) return;

        boolean firstDeathEncounter = !HorrorMemory.hasDiscovered(player, "death_first_sighting");
        HorrorMemory.recordDeathEncounter(player, now);
        HorrorMemory.recordDiscovery(player, "death_first_sighting");
        if (firstDeathEncounter) HorrorMemory.setLastAggressiveJumpscare(player, now);
        dropNextClue(player, death);
    }

    private static void updateClueInventory(ServerPlayer player) {
        java.util.stream.Stream.of(player.getInventory().items, player.getInventory().armor, player.getInventory().offhand)
                .flatMap(java.util.Collection::stream).forEach(stack -> {
            if (stack.is(OutlandsItems.DEATH_JAR.get())) HorrorMemory.recordDiscovery(player, "death_jar_found");
            if (stack.is(OutlandsItems.DEATH_MANIFEST.get())) HorrorMemory.recordDiscovery(player, "death_manifest_found");
            if (stack.is(OutlandsItems.ALMIGHTY_FRAGMENT.get())) HorrorMemory.recordDiscovery(player, "almighty_fragment_found");
        });
    }

    private static void dropNextClue(ServerPlayer player, DeathEntity death) {
        if (!HorrorMemory.hasDiscovered(player, "death_jar_found")
                && !HorrorMemory.hasDiscovered(player, "death_jar_dropped")) {
            if (drop(death, OutlandsItems.DEATH_JAR.get()))
                HorrorMemory.recordDiscovery(player, "death_jar_dropped");
        } else if (HorrorMemory.hasDiscovered(player, "death_jar_found")
                && !HorrorMemory.hasDiscovered(player, "death_manifest_found")
                && !HorrorMemory.hasDiscovered(player, "death_manifest_dropped")) {
            if (drop(death, OutlandsItems.DEATH_MANIFEST.get()))
                HorrorMemory.recordDiscovery(player, "death_manifest_dropped");
        } else if (HorrorMemory.hasDiscovered(player, "death_manifest_found")
                && !HorrorMemory.hasDiscovered(player, "almighty_fragment_found")
                && !HorrorMemory.hasDiscovered(player, "almighty_fragment_dropped")) {
            if (drop(death, OutlandsItems.ALMIGHTY_FRAGMENT.get()))
                HorrorMemory.recordDiscovery(player, "almighty_fragment_dropped");
        }
    }

    private static boolean drop(DeathEntity death, net.minecraft.world.item.Item item) {
        ItemEntity clue = new ItemEntity(death.level(), death.getX(), death.getY() + 0.8D, death.getZ(),
                new ItemStack(item));
        clue.setDefaultPickUpDelay();
        clue.setUnlimitedLifetime();
        clue.setInvulnerable(true);
        return death.level().addFreshEntity(clue);
    }

    /** Kept as a lifecycle hook; spawn timing is stored in persistent player data. */
    public static void clear() {}
}
