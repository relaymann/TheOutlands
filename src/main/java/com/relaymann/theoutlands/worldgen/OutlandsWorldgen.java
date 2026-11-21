package com.relaymann.theoutlands.worldgen;

import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Native structure and piece registries for the additive Outlands structure system. */
public final class OutlandsWorldgen {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.STRUCTURE_TYPE,TheOutlands.MOD_ID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.STRUCTURE_PIECE,TheOutlands.MOD_ID);

    public static final DeferredHolder<StructureType<?>,StructureType<OutlandsStructure>> OUTLANDS_STRUCTURE_TYPE =
            STRUCTURE_TYPES.register("outlands_multi_piece", () -> new StructureType<OutlandsStructure>() {
                @Override public com.mojang.serialization.MapCodec<OutlandsStructure> codec() {
                    return OutlandsStructure.CODEC;
                }
            });
    public static final DeferredHolder<StructurePieceType,StructurePieceType.ContextlessType> OUTLANDS_PIECE =
            STRUCTURE_PIECES.register("outlands_multi_piece", () -> tag -> new OutlandsStructurePiece(tag));

    private OutlandsWorldgen() {}
}
