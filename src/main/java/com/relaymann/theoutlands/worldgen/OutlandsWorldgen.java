package com.relaymann.theoutlands.worldgen;

import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Native structure and piece registries for the additive Outlands structure system. */
public final class OutlandsWorldgen {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE,TheOutlands.MOD_ID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE,TheOutlands.MOD_ID);

    public static final DeferredRegister.DeferredHolder<StructureType<?>,StructureType<OutlandsStructure>> OUTLANDS_STRUCTURE_TYPE =
            STRUCTURE_TYPES.register("outlands_multi_piece",()->()->OutlandsStructure.CODEC);
    public static final DeferredRegister.DeferredHolder<StructurePieceType,StructurePieceType.ContextlessType> OUTLANDS_PIECE =
            STRUCTURE_PIECES.register("outlands_multi_piece",()->OutlandsStructurePiece::new);

    private OutlandsWorldgen() {}
}
