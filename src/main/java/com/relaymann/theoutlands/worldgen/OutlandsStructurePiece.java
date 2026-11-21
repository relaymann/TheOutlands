package com.relaymann.theoutlands.worldgen;

import com.relaymann.theoutlands.OutlandsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.chunk.ChunkGenerator;

/** One persisted multi-chunk piece. Every chunk receives only its intersection of the same layout. */
public final class OutlandsStructurePiece extends StructurePiece {
    private final int anchorX;
    private final int surfaceY;
    private final int anchorZ;
    private final int size;
    private final long seed;

    public OutlandsStructurePiece(int x,int y,int z,int size,long seed) {
        super(OutlandsWorldgen.OUTLANDS_PIECE.get(),0,
                new BoundingBox(x,y-40,z,x+size-1,y+12,z+size-1));
        this.anchorX=x;this.surfaceY=y;this.anchorZ=z;this.size=size;this.seed=seed;
    }

    public OutlandsStructurePiece(CompoundTag tag) {
        super(OutlandsWorldgen.OUTLANDS_PIECE.get(),tag);
        this.anchorX=tag.getInt("OutlandsX");
        this.surfaceY=tag.getInt("OutlandsY");
        this.anchorZ=tag.getInt("OutlandsZ");
        this.size=tag.getInt("OutlandsSize");
        this.seed=tag.getLong("OutlandsSeed");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag tag) {
        tag.putInt("OutlandsX",anchorX);tag.putInt("OutlandsY",surfaceY);tag.putInt("OutlandsZ",anchorZ);
        tag.putInt("OutlandsSize",size);tag.putLong("OutlandsSeed",seed);
    }

    @Override
    public void postProcess(WorldGenLevel level,StructureManager structures,ChunkGenerator generator,
                            RandomSource random,BoundingBox box,ChunkPos chunkPos,BlockPos pivot) {
        if(!OutlandsConfig.OUTLANDS_STRUCTURES_ENABLED.get()) return;
        // Never overwrite another mod or vanilla structure in this chunk.
        if(!structures.startsForStructure(chunkPos, candidate -> !(candidate instanceof OutlandsStructure)).isEmpty()) return;
        if(!hasSuitableSurface(level,box,surfaceY)) return;
        BlockPos spawn=level.getLevel().getSharedSpawnPos();
        double dx=(anchorX+size/2.0D)-spawn.getX(),dz=(anchorZ+size/2.0D)-spawn.getZ();
        double distance=Math.sqrt(dx*dx+dz*dz);
        String biome=level.getBiome(new BlockPos(anchorX+size/2,surfaceY,anchorZ+size/2))
                .unwrapKey().map(key->key.location().getPath()).orElse("");
        boolean forest=biome.contains("forest")||biome.contains("taiga")
                ||biome.contains("grove")||biome.contains("wooded");
        String layout=OutlandsStructureFeature.chooseLayout(seed,distance,forest,
                Math.max(1_000.0D,OutlandsConfig.STRUCTURE_MIN_DISTANCE.get()),
                OutlandsConfig.STRUCTURE_CHANCE_MULTIPLIER.get());
        if(layout==null) return;
        OutlandsStructureFeature.generateLayout(layout,level,anchorX,surfaceY,anchorZ,
                RandomSource.create(seed ^ 0x6A09E667F3BCC909L),box);
        int centerChunkX=Math.floorDiv(anchorX+size/2,16);
        int centerChunkZ=Math.floorDiv(anchorZ+size/2,16);
        if(chunkPos.x==centerChunkX&&chunkPos.z==centerChunkZ) {
            int encounterY=switch(layout) {
                case "bunker" -> surfaceY-12; case "archive" -> surfaceY-25;
                case "research_facility" -> surfaceY-10; case "forgotten_station" -> surfaceY-8;
                case "null_chamber" -> surfaceY-18; case "hollow_crypt" -> surfaceY-22;
                default -> surfaceY+1;
            };
            StructureEncounterManager.trySpawn(layout,level,
                    new BlockPos(anchorX+size/2,encounterY,anchorZ+size/2),distance,seed);
        }
    }

    /** Reject wet, wooded, or unexpectedly uneven chunk intersections instead of cutting through them. */
    private static boolean hasSuitableSurface(WorldGenLevel level,BoundingBox box,int expectedY) {
        for(int x=box.minX();x<=box.maxX();x+=4) {
            for(int z=box.minZ();z<=box.maxZ();z+=4) {
                int top=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);
                if(top<=level.getMinBuildHeight()||Math.abs((top-1)-expectedY)>5) return false;
                var surface=level.getBlockState(new BlockPos(x,top-1,z));
                if(!surface.getFluidState().isEmpty()||surface.is(BlockTags.LOGS)) return false;
            }
        }
        return true;
    }
}
