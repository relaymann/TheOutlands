package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.worldgen.OutlandsStructureFeature;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import com.relaymann.theoutlands.worldgen.OutlandsWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Quiet, player-private audio details while exploring an Outlands structure. */
public final class StructureAmbienceManager {
    private static final Map<UUID,Long> NEXT_CUE=new HashMap<>();
    private StructureAmbienceManager() {}

    public static void tick(ServerLevel level) {
        if(!level.dimension().equals(Level.OVERWORLD)
                || !OutlandsConfig.OUTLANDS_STRUCTURES_ENABLED.get()
                || !OutlandsConfig.HORROR_AUDIO_ENABLED.get()
                || level.getGameTime()%20L!=0L) return;
        long now=level.getGameTime();
        for(ServerPlayer player:level.players()) {
            if(!player.isAlive()||player.isCreative()||player.isSpectator()
                    ||player.isSleeping()||player.isPassenger()) continue;
            long next=NEXT_CUE.getOrDefault(player.getUUID(),0L);
            if(now<next) continue;
            var start=level.structureManager().getStructureWithPieceAt(player.blockPosition(),
                    holder -> holder.value() instanceof com.relaymann.theoutlands.worldgen.OutlandsStructure);
            if(start==null||!start.isValid()) continue;
            var bounds=start.getBoundingBox();
            if(!bounds.isInside(player.blockPosition())) continue;
            BlockPos center=new BlockPos((bounds.minX()+bounds.maxX())/2,
                    (bounds.minY()+bounds.maxY())/2,(bounds.minZ()+bounds.maxZ())/2);
            double dx=center.getX()-level.getSharedSpawnPos().getX();
            double dz=center.getZ()-level.getSharedSpawnPos().getZ();
            double distance=Math.sqrt(dx*dx+dz*dz);
            int cx=Math.floorDiv(center.getX(),16),cz=Math.floorDiv(center.getZ(),16);
            long seed=com.relaymann.theoutlands.worldgen.StructurePlacementPolicy.chunkSeed(level.getSeed(),cx,cz);
            String biome=level.getBiome(new BlockPos(center.getX(),bounds.maxY()-8,center.getZ()))
                    .unwrapKey().map(k->k.location().getPath()).orElse("");
            boolean forest=biome.contains("forest")||biome.contains("taiga")||biome.contains("grove")||biome.contains("wooded");
            String id=OutlandsStructureFeature.chooseLayout(seed,distance,forest,
                    Math.max(1_000.0D,OutlandsConfig.STRUCTURE_MIN_DISTANCE.get()),
                    OutlandsConfig.STRUCTURE_CHANCE_MULTIPLIER.get());
            if(id==null) continue;
            if(player.getRandom().nextInt(90)!=0) continue;
            play(player,id,bounds,now);
            NEXT_CUE.put(player.getUUID(),now+2_400L+player.getRandom().nextInt(2_401));
        }
    }

    private static void play(ServerPlayer player,String id,net.minecraft.world.level.levelgen.structure.BoundingBox b,long now) {
        double x=b.minX()+4+player.getRandom().nextDouble()*Math.max(1,b.getXSpan()-8);
        double z=b.minZ()+4+player.getRandom().nextDouble()*Math.max(1,b.getZSpan()-8);
        double y=Math.max(player.getY()-8,Math.min(player.getY()+8,b.maxY()-2));
        switch(id) {
            case "research_facility" -> send(player,OutlandsSounds.STRUCTURE_RESEARCH.get(),x,y,z,0.24F,0.62F);
            case "apostle_cathedral" -> send(player,OutlandsSounds.STRUCTURE_CATHEDRAL.get(),x,y,z,0.24F,0.52F);
            case "forsaken_settlement" -> send(player,OutlandsSounds.STRUCTURE_SETTLEMENT.get(),x,y,z,0.30F,0.78F);
            case "last_outpost" -> send(player,OutlandsSounds.STRUCTURE_OUTPOST.get(),x,y,z,0.24F,0.7F);
            case "forgotten_station" -> send(player,OutlandsSounds.STRUCTURE_STATION.get(),x,y,z,0.22F,0.58F);
            case "null_chamber" -> send(player,OutlandsSounds.STRUCTURE_NULL_CHAMBER.get(),x,y,z,0.12F,0.36F);
            case "hollow_crypt" -> send(player,OutlandsSounds.STRUCTURE_CRYPT.get(),x,y,z,0.22F,0.48F);
            case "boundary_monument" -> send(player,OutlandsSounds.STRUCTURE_MONUMENT.get(),x,y,z,0.20F,0.64F);
            case "cabin" -> send(player,OutlandsSounds.STRUCTURE_CABIN.get(),x,y,z,0.20F,0.75F);
            case "tower" -> send(player,OutlandsSounds.STRUCTURE_TOWER.get(),x,y,z,0.18F,0.55F);
            case "bunker" -> send(player,OutlandsSounds.STRUCTURE_BUNKER.get(),x,y,z,0.20F,0.44F);
            case "shrine" -> send(player,OutlandsSounds.STRUCTURE_SHRINE.get(),x,y,z,0.18F,0.62F);
            case "observatory" -> send(player,OutlandsSounds.STRUCTURE_OBSERVATORY.get(),x,y,z,0.16F,0.84F);
            case "archive" -> send(player,OutlandsSounds.STRUCTURE_ARCHIVE.get(),x,y,z,0.18F,0.42F);
            default -> { return; }
        }
    }

    private static void send(ServerPlayer player,SoundEvent sound,double x,double y,double z,float volume,float pitch) {
        player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                SoundSource.AMBIENT,x,y,z,volume,pitch,player.getRandom().nextLong()));
    }

    public static void clear(ServerPlayer player){NEXT_CUE.remove(player.getUUID());}
    public static void clear(){NEXT_CUE.clear();}
}
