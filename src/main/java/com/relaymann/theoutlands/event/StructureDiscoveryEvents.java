package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.horror.HorrorMemory;
import com.relaymann.theoutlands.worldgen.OutlandsStructure;
import com.relaymann.theoutlands.worldgen.OutlandsStructureFeature;
import com.relaymann.theoutlands.worldgen.StructurePlacementPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Persists structure discoveries privately when a player opens an Outlands site cache. */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class StructureDiscoveryEvents {
    private StructureDiscoveryEvents() {}

    @SubscribeEvent
    public static void onStructureCacheOpened(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)
                || !OutlandsConfig.OUTLANDS_STRUCTURES_ENABLED.get()) return;
        var state = level.getBlockState(event.getPos());
        if (!state.is(Blocks.CHEST) && !state.is(Blocks.TRAPPED_CHEST)) return;
        var start = level.structureManager().getStructureWithPieceAt(event.getPos(),
                holder -> holder.value() instanceof OutlandsStructure);
        if (start == null || !start.isValid()) return;
        var bounds = start.getBoundingBox();
        BlockPos center = new BlockPos(bounds.minX()+24,bounds.maxY()-8,bounds.minZ()+24);
        double dx=center.getX()-level.getSharedSpawnPos().getX(), dz=center.getZ()-level.getSharedSpawnPos().getZ();
        double distance=Math.sqrt(dx*dx+dz*dz);
        int chunkX=Math.floorDiv(center.getX(),16), chunkZ=Math.floorDiv(center.getZ(),16);
        long seed=StructurePlacementPolicy.chunkSeed(level.getSeed(),chunkX,chunkZ);
        String biome=level.getBiome(center).unwrapKey().map(k->k.location().getPath()).orElse("");
        boolean forest=biome.contains("forest")||biome.contains("taiga")||biome.contains("grove")||biome.contains("wooded");
        String layout=OutlandsStructureFeature.chooseLayout(seed,distance,forest,
                Math.max(1_000.0D,OutlandsConfig.STRUCTURE_MIN_DISTANCE.get()),OutlandsConfig.STRUCTURE_CHANCE_MULTIPLIER.get());
        if(layout==null)return;
        HorrorMemory.recordDiscovery(player, "structure_" + layout);
        BlockPos hiddenCacheChest = new BlockPos(bounds.minX() + 39, bounds.maxY() - 49, bounds.minZ() + 6);
        if (event.getPos().equals(hiddenCacheChest)) {
            HorrorMemory.recordDiscovery(player, "hidden_lower_chamber");
            HorrorMemory.recordDiscovery(player, "shared_structure_mark");
        }
    }
}
