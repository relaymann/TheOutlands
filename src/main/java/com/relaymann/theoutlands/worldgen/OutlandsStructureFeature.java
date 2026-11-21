package com.relaymann.theoutlands.worldgen;

import com.relaymann.theoutlands.OutlandsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Rare additive structures; vanilla terrain and structure generation remain untouched. */
public final class OutlandsStructureFeature extends Feature<NoneFeatureConfiguration> {
    public OutlandsStructureFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c) {
        if (!OutlandsConfig.OUTLANDS_STRUCTURES_ENABLED.get()) return false;
        WorldGenLevel level = c.level();
        int chunkX = Math.floorDiv(c.origin().getX(), 16);
        int chunkZ = Math.floorDiv(c.origin().getZ(), 16);
        long worldSeed = level.getLevel().getSeed();
        int spacing = OutlandsConfig.STRUCTURE_SPACING_CHUNKS.get();
        if (!StructurePlacementPolicy.isSpacedAnchor(worldSeed, chunkX, chunkZ, spacing)) return false;

        RandomSource random = RandomSource.create(StructurePlacementPolicy.chunkSeed(worldSeed, chunkX, chunkZ));
        int x = chunkX * 16 + 3;
        int z = chunkZ * 16 + 3;
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        BlockPos sharedSpawn = level.getLevel().getSharedSpawnPos();
        double dx = x - sharedSpawn.getX();
        double dz = z - sharedSpawn.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        double minimumDistance = Math.max(1_000.0D, OutlandsConfig.STRUCTURE_MIN_DISTANCE.get());
        if (distance < minimumDistance || !stable(level, x, z, y)) return false;

        String biome = level.getBiome(new BlockPos(x, y, z)).unwrapKey()
                .map(key -> key.location().getPath()).orElse("");
        boolean forest = biome.contains("forest") || biome.contains("taiga")
                || biome.contains("grove") || biome.contains("wooded");
        if (biome.contains("ocean") || biome.contains("river") || biome.contains("beach")) return false;

        double multiplier = OutlandsConfig.STRUCTURE_CHANCE_MULTIPLIER.get();
        if (multiplier <= 0.0D) return false;
        double cellScale = StructurePlacementPolicy.CELL_CHUNKS * StructurePlacementPolicy.CELL_CHUNKS;
        double[] weights = new double[14];
        weights[0] = forest ? eligibleWeight(distance, minimumDistance, 1_000, 5_500, multiplier, cellScale) : 0;
        weights[1] = eligibleWeight(distance, minimumDistance, 1_800, 7_000, multiplier, cellScale);
        weights[2] = eligibleWeight(distance, minimumDistance, 2_500, 8_500, multiplier, cellScale);
        weights[3] = eligibleWeight(distance, minimumDistance, 15_000, 14_000, multiplier, cellScale);
        weights[4] = forest ? eligibleWeight(distance, minimumDistance, 22_000, 20_000, multiplier, cellScale) : 0;
        weights[5] = eligibleWeight(distance, minimumDistance, 50_000, 32_000, multiplier, cellScale);
        weights[6] = eligibleWeight(distance, minimumDistance, 3_000, 10_000, multiplier, cellScale);
        weights[7] = eligibleWeight(distance, minimumDistance, 12_000, 16_000, multiplier, cellScale);
        weights[8] = eligibleWeight(distance, minimumDistance, 2_000, 8_000, multiplier, cellScale);
        weights[9] = eligibleWeight(distance, minimumDistance, 4_000, 12_000, multiplier, cellScale);
        weights[10] = eligibleWeight(distance, minimumDistance, 5_000, 15_000, multiplier, cellScale);
        weights[11] = eligibleWeight(distance, minimumDistance, 25_000, 28_000, multiplier, cellScale);
        weights[12] = eligibleWeight(distance, minimumDistance, 35_000, 30_000, multiplier, cellScale);
        weights[13] = eligibleWeight(distance, minimumDistance, 18_000, 22_000, multiplier, cellScale);

        double totalWeight = 0.0D;
        for (double weight : weights) totalWeight += weight;
        if (totalWeight <= 0.0D || random.nextDouble() >= Math.min(1.0D, totalWeight)) return false;
        double choice = random.nextDouble() * totalWeight;
        int selected = weights.length - 1;
        for (int i = 0; i < weights.length; i++) {
            choice -= weights[i];
            if (choice < 0.0D) { selected = i; break; }
        }
        switch (selected) {
            case 0 -> cabin(level, x, y, z, random);
            case 1 -> tower(level, x, y, z, random);
            case 2 -> bunker(level, x, y, z, random);
            case 3 -> shrine(level, x, y, z, random);
            case 4 -> observatory(level, x, y, z, random);
            case 5 -> archive(level, x, y, z, random);
            case 6 -> researchFacility(level, x, y, z, random);
            case 7 -> apostleCathedral(level, x, y, z, random);
            case 8 -> forsakenSettlement(level, x, y, z, random);
            case 9 -> lastOutpost(level, x, y, z, random);
            case 10 -> forgottenStation(level, x, y, z, random);
            case 11 -> nullChamber(level, x, y, z, random);
            case 12 -> hollowCrypt(level, x, y, z, random);
            case 13 -> boundaryMonument(level, x, y, z, random);
            default -> { return false; }
        }
        return true;
    }

    private static double eligibleWeight(double distance, double configuredMinimum, double structureMinimum,
                                         int denominator, double multiplier, double cellScale) {
        return distance >= Math.max(configuredMinimum, structureMinimum)
                ? cellScale * multiplier / denominator : 0.0D;
    }

    private static boolean stable(WorldGenLevel level, int x, int z, int surfaceY) {
        if (surfaceY <= level.getMinBuildHeight() + 32 || surfaceY >= level.getMaxBuildHeight() - 16) return false;
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        for (int dx = 0; dx < 9; dx++) {
            for (int dz = 0; dz < 9; dz++) {
                int height = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x + dx, z + dz);
                lowest = Math.min(lowest, height);
                highest = Math.max(highest, height);
                BlockPos surface = new BlockPos(x + dx, height - 1, z + dz);
                BlockState top = level.getBlockState(surface);
                if (top.isAir() || !top.getFluidState().isEmpty()
                        || top.is(net.minecraft.tags.BlockTags.LEAVES)
                        || top.is(net.minecraft.tags.BlockTags.LOGS)) return false;
            }
        }
        return highest - lowest <= 2;
    }

    private static void p(WorldGenLevel l,int x,int y,int z,BlockState s){l.setBlock(new BlockPos(x,y,z),s,2);}
    private static void air(WorldGenLevel l,int x,int y,int z){p(l,x,y,z,Blocks.AIR.defaultBlockState());}
    private static void chest(WorldGenLevel l,int x,int y,int z,String loot,RandomSource r){
        BlockPos pos=new BlockPos(x,y,z);l.setBlock(pos,Blocks.CHEST.defaultBlockState(),2);
        if(l.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity c)
            c.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,ResourceLocation.parse("minecraft:chests/"+loot)),r.nextLong());
    }
    private static BlockState log(){return Blocks.SPRUCE_LOG.defaultBlockState()
            .setValue(RotatedPillarBlock.AXIS,Direction.Axis.Y);}

    // A forest cabin with a broken-looking roof, cold hearth and abandoned supplies.
    private static void cabin(WorldGenLevel l,int x,int y,int z,RandomSource r){
        for(int a=0;a<7;a++)for(int b=0;b<7;b++){
            p(l,x+a,y,z+b,Blocks.COBBLESTONE.defaultBlockState());
            for(int h=1;h<=3;h++){
                boolean edge=a==0||a==6||b==0||b==6;
                if(!edge)air(l,x+a,y+h,z+b);
                else if(h==2&&(a==1||a==5)&&(b==0||b==6))
                    p(l,x+a,y+h,z+b,Blocks.GLASS_PANE.defaultBlockState());
                else if(!(b==0&&a==3&&h<=2))
                    p(l,x+a,y+h,z+b,(a==0||a==6)&&(b==0||b==6)?log():Blocks.DARK_OAK_PLANKS.defaultBlockState());
                else air(l,x+a,y+h,z+b);
            }
            p(l,x+a,y+4,z+b,Blocks.SPRUCE_SLAB.defaultBlockState());
        }
        for(int a=1;a<6;a++){
            p(l,x+a,y+5,z,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
            p(l,x+a,y+5,z+6,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));
        }
        p(l,x+2,y+1,z+2,Blocks.LANTERN.defaultBlockState());
        p(l,x+5,y+1,z+4,Blocks.BARREL.defaultBlockState());
        for(int h=1;h<=3;h++)p(l,x+5,y+h,z+5,Blocks.COBBLESTONE.defaultBlockState());
        chest(l,x+4,y+1,z+4,"abandoned_mineshaft",r);
    }

    // A narrow timber tower with an observation deck and stored arrows.
    private static void tower(WorldGenLevel l,int x,int y,int z,RandomSource r){
        for(int a=0;a<5;a++)for(int b=0;b<5;b++){
            p(l,x+a,y,z+b,Blocks.STONE_BRICKS.defaultBlockState());
            if(a==0||a==4||b==0||b==4)for(int h=1;h<=10;h++)
                p(l,x+a,y+h,z+b,Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Y));
        }
        for(int a=0;a<5;a++)for(int b=0;b<5;b++){
            p(l,x+a,y+7,z+b,Blocks.OAK_PLANKS.defaultBlockState());
            if(a==0||a==4||b==0||b==4)p(l,x+a,y+8,z+b,Blocks.OAK_FENCE.defaultBlockState());
        }
        for(int h=1;h<=6;h++)p(l,x+2,y+h,z+1,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
        p(l,x+2,y+8,z+2,Blocks.LANTERN.defaultBlockState());
        chest(l,x+3,y+8,z+3,"pillager_outpost",r);
    }

    // A buried bunker with a narrow surface shaft; no blocks outside its footprint are changed.
    private static void bunker(WorldGenLevel l,int x,int surface,int z,RandomSource r){
        int y=surface-14;
        for(int a=0;a<9;a++)for(int b=0;b<7;b++){
            p(l,x+a,y,z+b,Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            p(l,x+a,y+4,z+b,Blocks.STONE_BRICKS.defaultBlockState());
            for(int h=1;h<4;h++){
                boolean edge=a==0||a==8||b==0||b==6;
                if(!edge)air(l,x+a,y+h,z+b);
                else p(l,x+a,y+h,z+b,Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
            }
        }
        p(l,x+2,y+2,z,Blocks.IRON_BARS.defaultBlockState());
        p(l,x+6,y+2,z,Blocks.IRON_BARS.defaultBlockState());
        p(l,x+2,y+1,z+2,Blocks.LANTERN.defaultBlockState());
        p(l,x+6,y+1,z+2,Blocks.LANTERN.defaultBlockState());
        for(int h=y+4;h<surface;h++){
            air(l,x+4,h,z+1);
            p(l,x+3,h,z+1,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
        }
        air(l,x+4,surface,z+1);
        p(l,x+3,surface,z+1,Blocks.STONE_BRICKS.defaultBlockState());
        p(l,x+5,surface,z+1,Blocks.STONE_BRICKS.defaultBlockState());
        p(l,x+4,surface,z+2,Blocks.IRON_TRAPDOOR.defaultBlockState());
        chest(l,x+6,y+1,z+4,"abandoned_mineshaft",r);
    }

    // A Fear-associated shrine marked by crying obsidian and silent blackstone pillars.
    private static void shrine(WorldGenLevel l,int x,int y,int z,RandomSource r){
        for(int a=0;a<7;a++)for(int b=0;b<7;b++)
            p(l,x+a,y,z+b,(a+b)%3==0?Blocks.CRACKED_STONE_BRICKS.defaultBlockState():Blocks.STONE_BRICKS.defaultBlockState());
        int[][] c={{1,1},{5,1},{1,5},{5,5}};
        for(int[] q:c)for(int h=1;h<=3;h++)p(l,x+q[0],y+h,z+q[1],Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        p(l,x+3,y+1,z+3,Blocks.CRYING_OBSIDIAN.defaultBlockState());
        p(l,x+3,y+2,z+3,Blocks.SOUL_LANTERN.defaultBlockState());
        chest(l,x+5,y+1,z+3,"ancient_city",r);
    }

    // A remote Null observatory: a ring, four featureless pillars and no obvious entrance.
    private static void observatory(WorldGenLevel l,int x,int y,int z,RandomSource r){
        for(int a=0;a<9;a++)for(int b=0;b<9;b++){
            int ax=a-4,az=b-4;
            if(ax*ax+az*az<=16)p(l,x+a,y,z+b,Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        }
        int[][] c={{1,1},{7,1},{1,7},{7,7}};
        for(int[] q:c){
            for(int h=1;h<=4;h++)p(l,x+q[0],y+h,z+q[1],Blocks.POLISHED_BLACKSTONE.defaultBlockState());
            p(l,x+q[0],y+5,z+q[1],Blocks.CHAIN.defaultBlockState());
        }
        p(l,x+4,y+1,z+4,Blocks.CRYING_OBSIDIAN.defaultBlockState());
        p(l,x+4,y+2,z+4,Blocks.SOUL_LANTERN.defaultBlockState());
        chest(l,x+4,y+1,z+6,"ancient_city",r);
    }

    // A deep Death archive with bookshelves, iron bars and a single cache.
    private static void archive(WorldGenLevel l,int x,int surface,int z,RandomSource r){
        int y=surface-27;
        for(int a=0;a<9;a++)for(int b=0;b<7;b++){
            p(l,x+a,y,z+b,Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            p(l,x+a,y+4,z+b,Blocks.DEEPSLATE_BRICKS.defaultBlockState());
            for(int h=1;h<4;h++){
                boolean edge=a==0||a==8||b==0||b==6;
                if(!edge)air(l,x+a,y+h,z+b);
                else p(l,x+a,y+h,z+b,Blocks.DEEPSLATE_BRICKS.defaultBlockState());
            }
        }
        p(l,x+2,y+1,z+2,Blocks.BOOKSHELF.defaultBlockState());
        p(l,x+6,y+1,z+2,Blocks.BOOKSHELF.defaultBlockState());
        p(l,x+2,y+1,z+4,Blocks.SOUL_LANTERN.defaultBlockState());
        p(l,x+6,y+1,z+4,Blocks.SOUL_LANTERN.defaultBlockState());
        p(l,x+4,y+2,z+3,Blocks.IRON_BARS.defaultBlockState());
        for(int h=y+4;h<surface;h++){
            air(l,x+4,h,z+1);
            p(l,x+3,h,z+1,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
        }
        air(l,x+4,surface,z+1);
        p(l,x+3,surface,z+1,Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        p(l,x+5,surface,z+1,Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        p(l,x+4,surface,z+2,Blocks.IRON_TRAPDOOR.defaultBlockState());
        chest(l,x+4,y+1,z+3,"ancient_city",r);
    }
    // Research Facility: buried test rooms, glass observation bays and containment cells.
    private static void researchFacility(WorldGenLevel l,int x,int surface,int z,RandomSource r){
        int y=surface-12;
        for(int a=0;a<9;a++)for(int b=0;b<7;b++){
            p(l,x+a,y,z+b,Blocks.POLISHED_ANDESITE.defaultBlockState());
            p(l,x+a,y+4,z+b,Blocks.IRON_BLOCK.defaultBlockState());
            for(int h=1;h<4;h++){
                boolean edge=a==0||a==8||b==0||b==6;
                if(!edge)air(l,x+a,y+h,z+b);
                else p(l,x+a,y+h,z+b,(h==2&&(a==0||a==8))?Blocks.GLASS.defaultBlockState():Blocks.IRON_BLOCK.defaultBlockState());
            }
        }
        // Two containment bays divided by a reinforced central corridor.
        for(int h=1;h<=3;h++){p(l,x+4,y+h,z+1,Blocks.IRON_BARS.defaultBlockState());p(l,x+4,y+h,z+5,Blocks.IRON_BARS.defaultBlockState());}
        p(l,x+2,y+1,z+2,Blocks.REDSTONE_LAMP.defaultBlockState());
        p(l,x+6,y+1,z+2,Blocks.REDSTONE_LAMP.defaultBlockState());
        p(l,x+2,y+1,z+4,Blocks.LECTERN.defaultBlockState());
        p(l,x+6,y+1,z+4,Blocks.CRYING_OBSIDIAN.defaultBlockState());
        for(int h=y+4;h<surface;h++){air(l,x+4,h,z+1);p(l,x+3,h,z+1,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));}
        air(l,x+4,surface,z+1);p(l,x+3,surface,z+1,Blocks.IRON_TRAPDOOR.defaultBlockState());
        chest(l,x+7,y+1,z+3,"stronghold_corridor",r);
    }

    // Apostle Cathedral: a compact ruined sanctuary with seven sealed niches around an altar.
    private static void apostleCathedral(WorldGenLevel l,int x,int y,int z,RandomSource r){
        for(int a=0;a<9;a++)for(int b=0;b<9;b++){
            boolean edge=a==0||a==8||b==0||b==8;
            p(l,x+a,y,z+b,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            if(edge)for(int h=1;h<=5;h++)p(l,x+a,y+h,z+b,Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
        }
        for(int a=2;a<=6;a+=2)for(int h=1;h<=4;h++){
            p(l,x+a,y+h,z+1,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            p(l,x+a,y+h,z+7,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        }
        for(int h=1;h<=3;h++){p(l,x+1,y+h,z+1,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());p(l,x+7,y+h,z+1,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());}
        p(l,x+4,y+1,z+4,Blocks.CRYING_OBSIDIAN.defaultBlockState());
        p(l,x+4,y+2,z+4,Blocks.SOUL_LANTERN.defaultBlockState());
        for(int a=1;a<=7;a++)if(a!=4)p(l,x+a,y+1,z+7,Blocks.IRON_BARS.defaultBlockState());
        chest(l,x+4,y+1,z+2,"woodland_mansion",r);
    }

    // Forsaken Settlement: a tightly clustered pair of ruined homes around an empty well.
    private static void forsakenSettlement(WorldGenLevel l,int x,int y,int z,RandomSource r){
        // Shared square of broken road and the dry well at its center.
        for(int a=0;a<9;a++)for(int b=0;b<9;b++)if(a==4||b==4)p(l,x+a,y,z+b,Blocks.COBBLESTONE.defaultBlockState());
        for(int a=2;a<=6;a++)for(int b=2;b<=6;b++)if(a==2||a==6||b==2||b==6)
            p(l,x+a,y+1,z+b,Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        for(int h=1;h<=3;h++)p(l,x+4,y+h,z+4,Blocks.COBBLESTONE.defaultBlockState());
        air(l,x+4,y+3,z+4);
        // Two roofless, partially collapsed houses at opposite corners.
        for(int[] o:new int[][]{{0,0},{6,6}}){
            for(int a=0;a<3;a++)for(int b=0;b<3;b++){
                p(l,x+o[0]+a,y,z+o[1]+b,Blocks.OAK_PLANKS.defaultBlockState());
                if(a==0||a==2||b==0||b==2){
                    for(int h=1;h<=2;h++)if(!(a==1&&b==0))p(l,x+o[0]+a,y+h,z+o[1]+b,Blocks.OAK_LOG.defaultBlockState());
                }
            }
            p(l,x+o[0]+1,y+3,z+o[1]+1,Blocks.SPRUCE_PLANKS.defaultBlockState());
        }
        p(l,x+1,y+1,z+1,Blocks.BARREL.defaultBlockState());
        p(l,x+7,y+1,z+7,Blocks.COBWEB.defaultBlockState());
        chest(l,x+2,y+1,z+1,"village/village_taiga_house",r);
    }

    // Last Outpost: a barricaded perimeter, lookout corners and an abandoned command post.
    private static void lastOutpost(WorldGenLevel l,int x,int y,int z,RandomSource r){
        for(int a=0;a<9;a++)for(int b=0;b<9;b++){
            if(a==0||a==8||b==0||b==8){
                p(l,x+a,y,z+b,Blocks.COBBLESTONE_WALL.defaultBlockState());
                if((a+b)%3==0)p(l,x+a,y+1,z+b,Blocks.OAK_FENCE.defaultBlockState());
            }else p(l,x+a,y,z+b,Blocks.COARSE_DIRT.defaultBlockState());
        }
        int[][] posts={{1,1},{7,1},{1,7},{7,7}};
        for(int[] q:posts)for(int h=1;h<=4;h++)p(l,x+q[0],y+h,z+q[1],Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Y));
        for(int a=3;a<=5;a++)for(int b=3;b<=5;b++)if(a==3||a==5||b==3||b==5)
            p(l,x+a,y+1,z+b,Blocks.SPRUCE_PLANKS.defaultBlockState());
        p(l,x+4,y+1,z+4,Blocks.CRAFTING_TABLE.defaultBlockState());
        p(l,x+4,y+2,z+4,Blocks.LANTERN.defaultBlockState());
        for(int a=2;a<=6;a++)p(l,x+a,y+1,z+2,Blocks.SPRUCE_FENCE.defaultBlockState());
        chest(l,x+6,y+1,z+6,"bastion_treasure",r);
    }

    // Forgotten Station: a buried platform with a short track section that ends at a sealed wall.
    // Forgotten Station: underground railway stop, waiting room and sealed forbidden tunnel.
    private static void forgottenStation(WorldGenLevel l,int x,int surface,int z,RandomSource r){
        int y=surface-10;
        for(int a=0;a<9;a++)for(int b=0;b<9;b++){
            p(l,x+a,y,z+b,Blocks.POLISHED_ANDESITE.defaultBlockState());
            p(l,x+a,y+4,z+b,Blocks.STONE_BRICKS.defaultBlockState());
            for(int h=1;h<4;h++){boolean edge=a==0||a==8||b==0||b==8;
                if(!edge)air(l,x+a,y+h,z+b);else p(l,x+a,y+h,z+b,Blocks.CRACKED_STONE_BRICKS.defaultBlockState());}
        }
        for(int a=0;a<9;a++){p(l,x+a,y+1,z+3,Blocks.RAIL.defaultBlockState());p(l,x+a,y+1,z+5,Blocks.RAIL.defaultBlockState());}
        for(int h=1;h<=3;h++)for(int b=2;b<=6;b++)p(l,x+8,y+h,z+b,Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        for(int a=1;a<=3;a++){p(l,x+a,y+1,z+1,Blocks.SPRUCE_SLAB.defaultBlockState());p(l,x+a,y+1,z+7,Blocks.SPRUCE_SLAB.defaultBlockState());}
        for(int a=1;a<=3;a++)p(l,x+a,y+1,z+2,Blocks.OAK_PLANKS.defaultBlockState());
        p(l,x+2,y+2,z+2,Blocks.LANTERN.defaultBlockState());p(l,x+2,y+1,z+6,Blocks.BARREL.defaultBlockState());
        p(l,x+4,y+1,z+1,Blocks.COBWEB.defaultBlockState());
        for(int h=y+4;h<surface;h++){air(l,x+4,h,z+1);p(l,x+3,h,z+1,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));}
        air(l,x+4,surface,z+1);p(l,x+3,surface,z+1,Blocks.IRON_TRAPDOOR.defaultBlockState());
        chest(l,x+2,y+1,z+7,"mineshaft",r);
    }

    // Null Chamber: exceptionally rare, almost empty, with four silent pillars and one marker.
    private static void nullChamber(WorldGenLevel l,int x,int surface,int z,RandomSource r){
        int y=surface-20;
        for(int a=0;a<7;a++)for(int b=0;b<7;b++){
            p(l,x+a,y,z+b,Blocks.POLISHED_DEEPSLATE.defaultBlockState());p(l,x+a,y+4,z+b,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            for(int h=1;h<4;h++){boolean edge=a==0||a==6||b==0||b==6;
                if(!edge)air(l,x+a,y+h,z+b);else p(l,x+a,y+h,z+b,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());}
        }
        int[][] pillars={{1,1},{5,1},{1,5},{5,5}};
        for(int[] q:pillars)for(int h=1;h<=3;h++)p(l,x+q[0],y+h,z+q[1],Blocks.BLACKSTONE.defaultBlockState());
        p(l,x+3,y+1,z+3,Blocks.CRYING_OBSIDIAN.defaultBlockState());
        p(l,x+3,y+2,z+3,Blocks.BLACK_CONCRETE.defaultBlockState());p(l,x+3,y+3,z+3,Blocks.BLACK_CONCRETE.defaultBlockState());
        for(int h=y+4;h<surface;h++){air(l,x+3,h,z+1);p(l,x+2,h,z+1,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));}
        air(l,x+3,surface,z+1);p(l,x+2,surface,z+1,Blocks.IRON_TRAPDOOR.defaultBlockState());
        chest(l,x+5,y+1,z+3,"ancient_city",r);
    }

    // Hollow Crypt: connected burial halls, side corridors, empty alcoves and a sealed central tomb.
    private static void hollowCrypt(WorldGenLevel l,int x,int surface,int z,RandomSource r){
        int y=surface-24;
        for(int a=0;a<9;a++)for(int b=0;b<9;b++){
            p(l,x+a,y,z+b,Blocks.DEEPSLATE_BRICKS.defaultBlockState());p(l,x+a,y+4,z+b,Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            for(int h=1;h<4;h++){boolean edge=a==0||a==8||b==0||b==8;
                if(!edge)air(l,x+a,y+h,z+b);else p(l,x+a,y+h,z+b,Blocks.DEEPSLATE_BRICKS.defaultBlockState());}
        }
        for(int a:new int[]{1,7})for(int b:new int[]{1,7}){
            p(l,x+a,y+1,z+b,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            p(l,x+a,y+2,z+b,Blocks.SOUL_LANTERN.defaultBlockState());
            p(l,x+a,y+1,z+(b==1?b+1:b-1),Blocks.IRON_BARS.defaultBlockState());
        }
        for(int b=2;b<=6;b++)p(l,x+4,y+1,z+b,Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState());
        for(int a=3;a<=5;a++)for(int b=3;b<=5;b++)p(l,x+a,y+1,z+b,Blocks.CRYING_OBSIDIAN.defaultBlockState());
        p(l,x+4,y+2,z+4,Blocks.IRON_BARS.defaultBlockState());
        for(int h=y+4;h<surface;h++){air(l,x+4,h,z+1);p(l,x+3,h,z+1,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));}
        air(l,x+4,surface,z+1);p(l,x+3,surface,z+1,Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        p(l,x+5,surface,z+1,Blocks.DEEPSLATE_BRICKS.defaultBlockState());p(l,x+4,surface,z+2,Blocks.IRON_TRAPDOOR.defaultBlockState());
        chest(l,x+7,y+1,z+4,"ancient_city",r);
    }

    // Boundary Monument: towering broken concentric rings and an empty freestanding frame.
    private static void boundaryMonument(WorldGenLevel l,int x,int y,int z,RandomSource r){
        for(int a=0;a<9;a++)for(int b=0;b<9;b++){
            int dx=a-4,dz=b-4,d2=dx*dx+dz*dz;
            if((d2>=9&&d2<=20)||(d2>=2&&d2<=4))
                if(!((a==0&&b==4)||(a==8&&b==4)||(a==4&&b==0)))
                    p(l,x+a,y,z+b,Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
        }
        int[][] posts={{1,1},{7,1},{1,7},{7,7}};
        for(int i=0;i<posts.length;i++){int[] q=posts[i];int top=i==2?8:6;
            for(int h=1;h<=top;h++)if(!(i==1&&h==3)&&!(i==3&&h==4))
                p(l,x+q[0],y+h,z+q[1],Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());}
        for(int h=1;h<=5;h++){p(l,x+3,y+h,z+4,Blocks.CHISELED_STONE_BRICKS.defaultBlockState());p(l,x+5,y+h,z+4,Blocks.CHISELED_STONE_BRICKS.defaultBlockState());}
        for(int a=3;a<=5;a++)if(a!=4)p(l,x+a,y+5,z+4,Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        p(l,x+4,y+1,z+4,Blocks.CRYING_OBSIDIAN.defaultBlockState());air(l,x+4,y+2,z+4);
        chest(l,x+2,y+1,z+4,"ruined_portal",r);
    }


}
