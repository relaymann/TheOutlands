package com.relaymann.theoutlands.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Small, deterministic interior dressing pass shared by all eighteen sites. */
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
            default -> surfaceY;
        };

        for (int i = 0; i < 5; i++) {
            int px = x + 17 + random.nextInt(14);
            int pz = z + 17 + random.nextInt(14);
            BlockPos pos = new BlockPos(px, floorY + 1, pz);
            if (!clip.isInside(pos) || !clip.isInside(pos.below())) continue;
            if (!level.getBlockState(pos).isAir()
                    || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) continue;

            var detail = switch (id) {
                case "archive" -> switch (random.nextInt(4)) {
                    case 0 -> Blocks.BOOKSHELF.defaultBlockState();
                    case 1 -> Blocks.LECTERN.defaultBlockState();
                    case 2 -> Blocks.COBWEB.defaultBlockState();
                    default -> Blocks.BARREL.defaultBlockState();
                };
                case "research_facility" -> switch (random.nextInt(4)) {
                    case 0 -> Blocks.IRON_BARS.defaultBlockState();
                    case 1 -> Blocks.BARREL.defaultBlockState();
                    case 2 -> Blocks.LECTERN.defaultBlockState();
                    default -> Blocks.COBWEB.defaultBlockState();
                };
                case "apostle_cathedral", "shrine", "null_chamber", "boundary_monument" ->
                        switch (random.nextInt(4)) {
                            case 0 -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
                            case 1 -> Blocks.CHAIN.defaultBlockState();
                            case 2 -> Blocks.SOUL_LANTERN.defaultBlockState();
                            default -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
                        };
                case "bunker", "forgotten_station" -> switch (random.nextInt(4)) {
                    case 0 -> Blocks.IRON_BARS.defaultBlockState();
                    case 1 -> Blocks.BARREL.defaultBlockState();
                    case 2 -> Blocks.COBWEB.defaultBlockState();
                    default -> Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
                };
                case "forsaken_settlement", "settlement", "last_outpost", "outpost" -> switch (random.nextInt(8)) {
                    case 0 -> Blocks.CHEST.defaultBlockState();
                    case 1 -> Blocks.CRAFTING_TABLE.defaultBlockState();
                    case 2 -> Blocks.CAMPFIRE.defaultBlockState();
                    case 3 -> Blocks.OAK_FENCE.defaultBlockState();
                    case 4 -> Blocks.BARREL.defaultBlockState();
                    case 5 -> Blocks.WHITE_BED.defaultBlockState();
                    case 6 -> Blocks.FURNACE.defaultBlockState();
                    default -> Blocks.COBWEB.defaultBlockState();
                };
                case "hollow_crypt" -> switch (random.nextInt(4)) {
                    case 0 -> Blocks.BONE_BLOCK.defaultBlockState();
                    case 1 -> Blocks.COBWEB.defaultBlockState();
                    case 2 -> Blocks.SOUL_LANTERN.defaultBlockState();
                    default -> Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
                };
                default -> switch (random.nextInt(4)) {
                    case 0 -> Blocks.BARREL.defaultBlockState();
                    case 1 -> Blocks.COBWEB.defaultBlockState();
                    case 2 -> Blocks.BOOKSHELF.defaultBlockState();
                    default -> Blocks.LANTERN.defaultBlockState();
                };
            };
            level.setBlock(pos, detail, 2);
        }
    }
}
