package com.relaymann.theoutlands.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class StructureInteriorDressing {
    private StructureInteriorDressing() {}

    public static void apply(String id, WorldGenLevel level, int x, int surfaceY, int z,
                             RandomSource random, BoundingBox clip) {
        int floorY = switch (id) {
            case "bunker" -> surfaceY - 14;
            case "archive" -> surfaceY - 27;
            case "research_facility" -> surfaceY - 12;
            case "forgotten_station" -> surfaceY - 10;
            case "null_chamber" -> surfaceY - 20;
            case "hollow_crypt" -> surfaceY - 24;
            case "mining_operation" -> surfaceY - 18;
            case "subterranean_complex" -> surfaceY - 26;
            default -> surfaceY;
        };

        int detailCount = switch (id) {
            case "forsaken_settlement", "last_outpost" -> 22;
            case "research_facility", "bunker", "forgotten_station" -> 14;
            case "archive", "hollow_crypt", "apostle_cathedral" -> 12;
            case "shrine", "null_chamber", "boundary_monument" -> 9;
            case "mining_operation", "subterranean_complex" -> 16;
            case "ranger_station", "observation_tower", "ruined_settlement" -> 18;
            default -> 8;
        };
        for (int i = 0; i < detailCount; i++) {
            int px = x + 17 + random.nextInt(14);
            int pz = z + 17 + random.nextInt(14);
            BlockPos pos = new BlockPos(px, floorY + 1, pz);
            if (!clip.isInside(pos) || !clip.isInside(pos.below())) continue;
            if (!level.getBlockState(pos).isAir()
                    || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) continue;

            var detail = switch (id) {
                case "archive" -> switch (random.nextInt(6)) {
                    case 0 -> Blocks.BOOKSHELF.defaultBlockState();
                    case 1 -> Blocks.LECTERN.defaultBlockState();
                    case 2 -> Blocks.COBWEB.defaultBlockState();
                    case 3 -> Blocks.BARREL.defaultBlockState();
                    case 4 -> Blocks.CANDLE.defaultBlockState();
                    default -> Blocks.MOSS_CARPET.defaultBlockState();
                };
                case "research_facility" -> switch (random.nextInt(7)) {
                    case 0 -> Blocks.IRON_BARS.defaultBlockState();
                    case 1 -> Blocks.BARREL.defaultBlockState();
                    case 2 -> Blocks.LECTERN.defaultBlockState();
                    case 3 -> Blocks.COBWEB.defaultBlockState();
                    case 4 -> Blocks.CAULDRON.defaultBlockState();
                    case 5 -> Blocks.BLAST_FURNACE.defaultBlockState();
                    default -> Blocks.CHAIN.defaultBlockState();
                };
                case "apostle_cathedral", "shrine", "null_chamber", "boundary_monument" ->
                        switch (random.nextInt(6)) {
                            case 0 -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
                            case 1 -> Blocks.CHAIN.defaultBlockState();
                            case 2 -> Blocks.SOUL_LANTERN.defaultBlockState();
                            case 3 -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
                            case 4 -> Blocks.CANDLE.defaultBlockState();
                            default -> Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
                        };
                case "bunker", "forgotten_station" -> switch (random.nextInt(7)) {
                    case 0 -> Blocks.IRON_BARS.defaultBlockState();
                    case 1 -> Blocks.BARREL.defaultBlockState();
                    case 2 -> Blocks.COBWEB.defaultBlockState();
                    case 3 -> Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
                    case 4 -> Blocks.CHAIN.defaultBlockState();
                    case 5 -> Blocks.SMOKER.defaultBlockState();
                    default -> Blocks.CAULDRON.defaultBlockState();
                };
                case "forsaken_settlement", "settlement", "last_outpost", "outpost" -> switch (random.nextInt(16)) {
                    case 0 -> Blocks.CHEST.defaultBlockState();
                    case 1 -> Blocks.CRAFTING_TABLE.defaultBlockState();
                    case 2 -> Blocks.CAMPFIRE.defaultBlockState();
                    case 3 -> Blocks.OAK_FENCE.defaultBlockState();
                    case 4 -> Blocks.BARREL.defaultBlockState();
                    case 5 -> Blocks.WHITE_BED.defaultBlockState();
                    case 6 -> Blocks.FURNACE.defaultBlockState();
                    case 7 -> Blocks.COBWEB.defaultBlockState();
                    case 8 -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
                    case 9 -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                    case 10 -> Blocks.BONE_BLOCK.defaultBlockState();
                    case 11 -> Blocks.TORCH.defaultBlockState();
                    case 12 -> Blocks.CARTOGRAPHY_TABLE.defaultBlockState();
                    case 13 -> Blocks.COMPOSTER.defaultBlockState();
                    case 14 -> Blocks.GRINDSTONE.defaultBlockState();
                    default -> Blocks.DEAD_BUSH.defaultBlockState();
                };
                case "mining_operation", "subterranean_complex" -> switch (random.nextInt(7)) {
                    case 0 -> Blocks.RAIL.defaultBlockState();
                    case 1 -> Blocks.CHAIN.defaultBlockState();
                    case 2 -> Blocks.COBWEB.defaultBlockState();
                    case 3 -> Blocks.BARREL.defaultBlockState();
                    case 4 -> Blocks.CAULDRON.defaultBlockState();
                    case 5 -> Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
                    default -> Blocks.SOUL_LANTERN.defaultBlockState();
                };
                case "ranger_station", "observation_tower", "ruined_settlement" -> switch (random.nextInt(8)) {
                    case 0 -> Blocks.BARREL.defaultBlockState();
                    case 1 -> Blocks.COBWEB.defaultBlockState();
                    case 2 -> Blocks.CAMPFIRE.defaultBlockState();
                    case 3 -> Blocks.CARTOGRAPHY_TABLE.defaultBlockState();
                    case 4 -> Blocks.COMPOSTER.defaultBlockState();
                    case 5 -> Blocks.MOSS_CARPET.defaultBlockState();
                    case 6 -> Blocks.DEAD_BUSH.defaultBlockState();
                    default -> Blocks.LANTERN.defaultBlockState();
                };
                case "hollow_crypt" -> switch (random.nextInt(6)) {
                    case 0 -> Blocks.BONE_BLOCK.defaultBlockState();
                    case 1 -> Blocks.COBWEB.defaultBlockState();
                    case 2 -> Blocks.SOUL_LANTERN.defaultBlockState();
                    case 3 -> Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
                    case 4 -> Blocks.CHAIN.defaultBlockState();
                    default -> Blocks.DEAD_BUSH.defaultBlockState();
                };
                default -> switch (random.nextInt(6)) {
                    case 0 -> Blocks.BARREL.defaultBlockState();
                    case 1 -> Blocks.COBWEB.defaultBlockState();
                    case 2 -> Blocks.BOOKSHELF.defaultBlockState();
                    case 3 -> Blocks.LANTERN.defaultBlockState();
                    case 4 -> Blocks.MOSS_CARPET.defaultBlockState();
                    default -> Blocks.CANDLE.defaultBlockState();
                };
            };
            level.setBlock(pos, detail, 2);
        }
    }
}
