package com.relaymann.theoutlands.block;

import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Durable, placeable survival defenses. */
public final class OutlandsBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, TheOutlands.MOD_ID);
    public static final DeferredHolder<Block, Block> REINFORCED_BARRICADE =
            BLOCKS.register("reinforced_barricade", () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD).strength(14.0F, 1200.0F)
                    .sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> REINFORCED_DOOR =
            BLOCKS.register("reinforced_door", () -> new DoorBlock(BlockSetType.IRON, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(18.0F, 1600.0F)
                    .sound(SoundType.NETHERITE_BLOCK).noOcclusion().requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> SPIKE_TRAP =
            BLOCKS.register("spike_trap", () -> new SurvivalDefenseBlock(SurvivalDefenseBlock.Kind.SPIKES,
                    BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F, 6.0F)
                            .sound(SoundType.METAL).noOcclusion()));
    public static final DeferredHolder<Block, Block> PERIMETER_ALARM =
            BLOCKS.register("perimeter_alarm", () -> new SurvivalDefenseBlock(SurvivalDefenseBlock.Kind.ALARM,
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(2.5F, 5.0F)
                            .sound(SoundType.METAL).noOcclusion()));
    private OutlandsBlocks() {}
}
