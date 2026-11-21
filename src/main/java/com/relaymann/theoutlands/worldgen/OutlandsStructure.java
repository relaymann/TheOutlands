package com.relaymann.theoutlands.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.relaymann.theoutlands.OutlandsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.Structure.GenerationContext;
import net.minecraft.world.level.levelgen.structure.Structure.GenerationStub;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

/** One data-driven structure set whose starts are expanded into chunk-safe native pieces. */
public final class OutlandsStructure extends Structure {
    public static final MapCodec<OutlandsStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(Structure.settingsCodec(instance),
                    Codec.INT.fieldOf("size").forGetter(s -> s.size))
                    .apply(instance, OutlandsStructure::new));

    private final int size;

    public OutlandsStructure(Structure.StructureSettings settings, int size) {
        super(settings);
        this.size = Math.max(32, Math.min(64, size));
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!OutlandsConfig.OUTLANDS_STRUCTURES_ENABLED.get()) return Optional.empty();
        int chunkX=context.chunkPos().x, chunkZ=context.chunkPos().z;
        if (!StructurePlacementPolicy.isSpacedAnchor(context.seed(),chunkX,chunkZ,
                OutlandsConfig.STRUCTURE_SPACING_CHUNKS.get())) return Optional.empty();

        int x=context.chunkPos().getMinBlockX()-(size-16)/2;
        int z=context.chunkPos().getMinBlockZ()-(size-16)/2;
        int sampleStep=Math.max(8,size/3);
        int min=Integer.MAX_VALUE,max=Integer.MIN_VALUE;
        for(int sx=0;sx<3;sx++)for(int sz=0;sz<3;sz++){
            int x1=x+sx*sampleStep, z1=z+sz*sampleStep;
            int x2=Math.min(x+size-1,x1+sampleStep-1);
            int z2=Math.min(z+size-1,z1+sampleStep-1);
            int height=Structure.getMeanFirstOccupiedHeight(context,x1,x2,z1,z2);
            min=Math.min(min,height);max=Math.max(max,height);
        }
        if(max-min>5) return Optional.empty();
        int surfaceY=Structure.getMeanFirstOccupiedHeight(context,x,x+size-1,z,z+size-1);
        if(surfaceY<=context.heightAccessor().getMinBuildHeight()+32
                ||surfaceY>=context.heightAccessor().getMaxBuildHeight()-16) return Optional.empty();

        long pieceSeed=StructurePlacementPolicy.chunkSeed(context.seed(),chunkX,chunkZ);
        BlockPos origin=new BlockPos(x,surfaceY,z);
        return Optional.of(new GenerationStub(origin,builder ->
                builder.addPiece(new OutlandsStructurePiece(x,surfaceY,z,size,pieceSeed))));
    }

    @Override
    public StructureType<?> type() {
        return OutlandsWorldgen.OUTLANDS_STRUCTURE_TYPE.get();
    }
}
