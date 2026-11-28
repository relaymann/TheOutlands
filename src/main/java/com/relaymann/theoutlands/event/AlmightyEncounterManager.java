package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.item.OutlandsItems;
import com.relaymann.theoutlands.entity.AlmightyEntity;
import com.relaymann.theoutlands.entity.OutlandsEntities;
import com.relaymann.theoutlands.horror.AlmightyTerritory;
import com.relaymann.theoutlands.horror.HorrorMemory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public final class AlmightyEncounterManager {
    private AlmightyEncounterManager() {}

    public static void evaluate(ServerPlayer player) {
        boolean carriesLastPage = java.util.stream.Stream.of(player.getInventory().items,
                        player.getInventory().armor, player.getInventory().offhand)
                .flatMap(java.util.Collection::stream)
                .anyMatch(stack -> stack.is(OutlandsItems.THE_LAST_PAGE.get()));
        if (carriesLastPage) HorrorMemory.recordDiscovery(player, "the_last_page_recovered");
        var level = player.serverLevel();
        double depth = AlmightyTerritory.distanceInsideBorder(level, player.getX(), player.getZ());
        if (!Double.isFinite(depth) || depth < 0.0D
                || depth > OutlandsConfig.ALMIGHTY_TERRITORY_DEPTH.get()) return;

        var nearby = level.getEntitiesOfClass(AlmightyEntity.class,
                new AABB(player.blockPosition()).inflate(256.0D), AlmightyEntity::isAlive);
        if (!nearby.isEmpty()) {
            HorrorMemory.recordDiscovery(player, "almighty_encounter_started");
            var existingBoss = nearby.get(0);
            if (!(existingBoss.getTarget() instanceof net.minecraft.world.entity.player.Player currentTarget)
                    || !currentTarget.isAlive() || currentTarget.isCreative() || currentTarget.isSpectator()
                    || !AlmightyTerritory.contains(level, currentTarget.getX(), currentTarget.getZ())) {
                existingBoss.setTarget(player);
            }
            return;
        }
        if (HorrorMemory.hasDiscovered(player, "almighty_encounter_started")) return;

        var border = level.getWorldBorder();
        double dx = border.getCenterX() - player.getX();
        double dz = border.getCenterZ() - player.getZ();
        double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        double x = player.getX() + dx / length * 18.0D;
        double z = player.getZ() + dz / length * 18.0D;
        BlockPos column = BlockPos.containing(x, player.getY(), z);
        if (!level.hasChunkAt(column)) return;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
        BlockPos spawn = new BlockPos(column.getX(), y, column.getZ());
        if (!AlmightyTerritory.contains(level, spawn.getX() + 0.5D, spawn.getZ() + 0.5D)) return;

        AlmightyEntity boss = OutlandsEntities.ALMIGHTY.get().create(level);
        if (boss == null) return;
        boss.moveTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D,
                player.getYRot() + 180.0F, 0.0F);
        boss.setTarget(player);
        if (!level.noCollision(boss, boss.getBoundingBox()) || !level.addFreshEntity(boss)) return;

        HorrorMemory.recordDiscovery(player, "almighty_encounter_started");
        if (OutlandsConfig.HORROR_AUDIO_ENABLED.get())
            level.playSound(null, spawn, OutlandsSounds.ALMIGHTY_PRESENCE.get(), SoundSource.HOSTILE, 1.0F, 0.35F);
    }
}
