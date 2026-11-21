package com.relaymann.theoutlands.worldgen;

import com.relaymann.theoutlands.OutlandsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Rare additive structures; vanilla terrain and structure generation remain untouched. */
public final class OutlandsStructureFeature extends Feature<NoneFeatureConfiguration> {
    public OutlandsStructureFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c) {
        if (!OutlandsConfig.OUTLANDS_STRUCTURES_ENABLED.get()) return false;
        WorldGenLevel l=c.level(); RandomSource r=c.random();
        // Keep the largest 9x9 footprint inside the chunk being decorated.
        int x=(c.origin().getX() & ~15)+3, z=(c.origin().getZ() & ~15)+3;
        int y=l.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x,z);
        double dx=x-l.getLevelData().getXSpawn(), dz=z-l.getLevelData().getZSpawn();
        double dist=Math.sqrt(dx*dx+dz*dz);
        double min=Math.max(1_000.0D,OutlandsConfig.STRUCTURE_MIN_DISTANCE.get());
        if(dist<min||!stable(l,x,z,y)) return false;
        double mult=OutlandsConfig.STRUCTURE_CHANCE_MULTIPLIER.get();
        if(mult<=0) return false;
        String biome=l.getBiome(new BlockPos(x,y,z)).unwrapKey()
                .map(k->k.location().getPath()).orElse("");
        boolean forest=biome.contains("forest")||biome.contains("taiga")
                ||biome.contains("grove")||biome.contains("wooded");
        if(forest&&roll(r,7_000,mult)){cabin(l,x,y,z,r);return true;}
        if(dist>=Math.max(min,1_800)&&roll(r,9_000,mult)){tower(l,x,y,z,r);return true;}
        if(dist>=Math.max(min,2_500)&&roll(r,11_000,mult)){bunker(l,x,y,z,r);return true;}
        if(dist>=Math.max(min,15_000)&&roll(r,18_000,mult)){shrine(l,x,y,z,r);return true;}
        if(forest&&dist>=Math.max(min,22_000)&&roll(r,26_000,mult)){observatory(l,x,y,z,r);return true;}
        if(dist>=Math.max(min,50_000)&&roll(r,42_000,mult)){archive(l,x,y,z,r);return true;}
        return false;
    }

    private static boolean roll(RandomSource r,int n,double m){return r.nextInt(Math.max(1,(int)Math.ceil(n/m)))==0;}
    private static boolean stable(WorldGenLevel l,int x,int z,int y){
        int lo=Integer.MAX_VALUE,hi=Integer.MIN_VALUE;
        for(int a=0;a<9;a++)for(int b=0;b<9;b++){
            int h=l.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x+a,z+b);
            lo=Math.min(lo,h);hi=Math.max(hi,h);
            BlockState s=l.getBlockState(new BlockPos(x+a,h-1,z+b));
            if(s.isAir()||!s.getFluidState().isEmpty())return false;
        }
        return hi-lo<=2&&y>l.getMinBuildHeight()+32;
    }
    private static void p(WorldGenLevel l,int x,int y,int z,BlockState s){l.setBlock(new BlockPos(x,y,z),s,2);}
    private static void air(WorldGenLevel l,int x,int y,int z){p(l,x,y,z,Blocks.AIR.defaultBlockState());}
    private static void chest(WorldGenLevel l,int x,int y,int z,String loot,RandomSource r){
        BlockPos pos=new BlockPos(x,y,z);l.setBlock(pos,Blocks.CHEST.defaultBlockState(),2);
        if(l.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity c)
            c.setLootTable(ResourceLocation.parse("minecraft:chests/"+loot),r.nextLong());
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
}
