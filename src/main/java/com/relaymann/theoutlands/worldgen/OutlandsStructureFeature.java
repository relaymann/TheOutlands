package com.relaymann.theoutlands.worldgen;

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
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;

/** Deterministic, chunk-clipped layout builders used by the native structure piece. */
public final class OutlandsStructureFeature {
    private OutlandsStructureFeature() {}

    public static String chooseLayout(long seed, double distance, boolean forest,
                                      double configuredMinimum, double multiplier) {
        if (distance < configuredMinimum || multiplier <= 0.0D) return null;
        double scale = StructurePlacementPolicy.CELL_CHUNKS * StructurePlacementPolicy.CELL_CHUNKS;
        double[] weights = new double[14];
        weights[0] = forest ? weight(distance, 1_000, 5_500, multiplier, scale) : 0;
        weights[1] = weight(distance, 1_800, 7_000, multiplier, scale);
        weights[2] = weight(distance, 2_500, 8_500, multiplier, scale);
        weights[3] = weight(distance, 15_000, 14_000, multiplier, scale);
        weights[4] = forest ? weight(distance, 22_000, 20_000, multiplier, scale) : 0;
        weights[5] = weight(distance, 50_000, 32_000, multiplier, scale);
        weights[6] = weight(distance, 3_000, 10_000, multiplier, scale);
        weights[7] = weight(distance, 12_000, 16_000, multiplier, scale);
        weights[8] = weight(distance, 2_000, 8_000, multiplier, scale);
        weights[9] = weight(distance, 4_000, 12_000, multiplier, scale);
        weights[10] = weight(distance, 5_000, 15_000, multiplier, scale);
        weights[11] = weight(distance, 25_000, 28_000, multiplier, scale);
        weights[12] = weight(distance, 35_000, 30_000, multiplier, scale);
        weights[13] = weight(distance, 18_000, 22_000, multiplier, scale);
        double total=0.0D; for(double w:weights) total+=w;
        if(total<=0.0D) return null;
        RandomSource random=RandomSource.create(seed);
        if(random.nextDouble()>=Math.min(1.0D,total)) return null;
        double pick=random.nextDouble()*total;
        String[] ids={"cabin","tower","bunker","shrine","observatory","archive",
                "research_facility","apostle_cathedral","forsaken_settlement","last_outpost",
                "forgotten_station","null_chamber","hollow_crypt","boundary_monument"};
        for(int i=0;i<weights.length;i++){pick-=weights[i];if(pick<0.0D)return ids[i];}
        return ids[ids.length-1];
    }

    private static double weight(double distance,int minimum,int denominator,double multiplier,double scale) {
        return distance>=minimum ? scale*multiplier/denominator : 0.0D;
    }

    public static void generateLayout(String id, WorldGenLevel level, int x, int surfaceY, int z,
                                      RandomSource random, BoundingBox clip) {
        ACTIVE_CLIP.set(clip);
        try {
            int coreX=x+19, coreZ=z+19;
            switch(id) {
                case "cabin" -> cabin(level,coreX,surfaceY,coreZ,random);
                case "tower" -> tower(level,coreX,surfaceY,coreZ,random);
                case "bunker" -> bunker(level,coreX,surfaceY,coreZ,random);
                case "shrine" -> shrine(level,coreX,surfaceY,coreZ,random);
                case "observatory" -> observatory(level,coreX,surfaceY,coreZ,random);
                case "archive" -> archive(level,coreX,surfaceY,coreZ,random);
                case "research_facility" -> researchFacility(level,coreX,surfaceY,coreZ,random);
                case "apostle_cathedral" -> apostleCathedral(level,coreX,surfaceY,coreZ,random);
                case "forsaken_settlement" -> forsakenSettlement(level,coreX,surfaceY,coreZ,random);
                case "last_outpost" -> lastOutpost(level,coreX,surfaceY,coreZ,random);
                case "forgotten_station" -> forgottenStation(level,coreX,surfaceY,coreZ,random);
                case "null_chamber" -> nullChamber(level,coreX,surfaceY,coreZ,random);
                case "hollow_crypt" -> hollowCrypt(level,coreX,surfaceY,coreZ,random);
                case "boundary_monument" -> boundaryMonument(level,coreX,surfaceY,coreZ,random);
                default -> { return; }
            }
            expand(id,level,x,surfaceY,z);
            hiddenCache(id,level,x,surfaceY,z,random);
        } finally {
            ACTIVE_CLIP.remove();
        }
    }

    private static void expand(String id,WorldGenLevel l,int x,int y,int z) {
        switch(id) {
            case "cabin" -> {
                yard(l,x,y,z,Blocks.OAK_FENCE.defaultBlockState());
                shed(l,x+3,y,z+3,Blocks.SPRUCE_PLANKS.defaultBlockState());
                pathX(l,x+10,y,z+23,18,Blocks.GRAVEL.defaultBlockState());
            }
            case "tower" -> {
                yard(l,x,y,z,Blocks.SPRUCE_FENCE.defaultBlockState());
                room(l,x+3,y,z+3,11,9,3,Blocks.COBBLESTONE.defaultBlockState(),Blocks.OAK_PLANKS.defaultBlockState(),Blocks.SPRUCE_PLANKS.defaultBlockState());
                pathX(l,x+14,y,z+23,16,Blocks.COARSE_DIRT.defaultBlockState());
            }
            case "bunker" -> {
                int d=y-14;
                room(l,x+2,d,z+18,12,10,4,Blocks.POLISHED_DEEPSLATE.defaultBlockState(),Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),Blocks.STONE_BRICKS.defaultBlockState());
                room(l,x+34,d,z+18,12,10,4,Blocks.POLISHED_DEEPSLATE.defaultBlockState(),Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),Blocks.STONE_BRICKS.defaultBlockState());
                hallX(l,x+14,d,z+22,20,Blocks.DEEPSLATE_BRICKS.defaultBlockState());
            }
            case "shrine" -> {
                courtyard(l,x,y,z,Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
                for(int[] q:new int[][]{{4,4},{40,4},{4,40},{40,40}})obelisk(l,x+q[0],y,z+q[1],5,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            }
            case "observatory" -> {
                ring(l,x,y,z,24,Blocks.DEEPSLATE_BRICKS.defaultBlockState());
                for(int[] q:new int[][]{{5,5},{40,5},{5,40},{40,40}})obelisk(l,x+q[0],y,z+q[1],7,Blocks.BLACKSTONE.defaultBlockState());
            }
            case "archive" -> {
                int d=y-27;
                room(l,x+2,d,z+18,12,10,5,Blocks.DEEPSLATE_BRICKS.defaultBlockState(),Blocks.BOOKSHELF.defaultBlockState(),Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                room(l,x+34,d,z+18,12,10,5,Blocks.DEEPSLATE_BRICKS.defaultBlockState(),Blocks.BOOKSHELF.defaultBlockState(),Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                hallX(l,x+14,d,z+22,20,Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState());
            }
            case "research_facility" -> {
                int d=y-12;
                room(l,x+2,d,z+18,12,12,4,Blocks.IRON_BLOCK.defaultBlockState(),Blocks.POLISHED_ANDESITE.defaultBlockState(),Blocks.IRON_BLOCK.defaultBlockState());
                room(l,x+34,d,z+18,12,12,4,Blocks.IRON_BLOCK.defaultBlockState(),Blocks.POLISHED_ANDESITE.defaultBlockState(),Blocks.IRON_BLOCK.defaultBlockState());
                room(l,x+18,d,z+2,12,12,4,Blocks.IRON_BLOCK.defaultBlockState(),Blocks.POLISHED_ANDESITE.defaultBlockState(),Blocks.IRON_BLOCK.defaultBlockState());
                room(l,x+18,d,z+34,12,12,4,Blocks.IRON_BLOCK.defaultBlockState(),Blocks.POLISHED_ANDESITE.defaultBlockState(),Blocks.IRON_BLOCK.defaultBlockState());
                hallX(l,x+14,d,z+23,20,Blocks.IRON_BLOCK.defaultBlockState());
                hallZ(l,x+23,d,z+14,20,Blocks.IRON_BLOCK.defaultBlockState());
            }
            case "apostle_cathedral" -> {
                room(l,x+3,y,z+3,13,38,8,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(),Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                room(l,x+32,y,z+3,13,38,8,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(),Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                hallZ(l,x+23,y,z+2,44,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                for(int a=8;a<40;a+=8)obelisk(l,x+22,y,z+a,7,Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());
            }
            case "forsaken_settlement" -> {
                house(l,x+2,y,z+2); house(l,x+35,y,z+2); house(l,x+2,y,z+35); house(l,x+35,y,z+35);
                pathX(l,x+8,y,z+23,32,Blocks.DIRT_PATH.defaultBlockState());
                pathZ(l,x+23,y,z+8,32,Blocks.DIRT_PATH.defaultBlockState());
                well(l,x+20,y,z+20);
            }
            case "last_outpost" -> {
                yard(l,x,y,z,Blocks.COBBLESTONE_WALL.defaultBlockState());
                for(int[] q:new int[][]{{3,3},{40,3},{3,40},{40,40}})watchPost(l,x+q[0],y,z+q[1]);
                room(l,x+18,y,z+18,12,12,4,Blocks.COBBLESTONE.defaultBlockState(),Blocks.SPRUCE_PLANKS.defaultBlockState(),Blocks.SPRUCE_PLANKS.defaultBlockState());
                pathX(l,x+12,y,z+23,6,Blocks.COARSE_DIRT.defaultBlockState());
            }
            case "forgotten_station" -> {
                int d=y-10;
                hallX(l,x+1,d,z+22,46,Blocks.POLISHED_ANDESITE.defaultBlockState());
                for(int a=1;a<47;a++){p(l,x+a,d+1,z+20,Blocks.RAIL.defaultBlockState());p(l,x+a,d+1,z+26,Blocks.RAIL.defaultBlockState());}
                room(l,x+2,d,z+3,12,12,4,Blocks.DEEPSLATE_BRICKS.defaultBlockState(),Blocks.STONE_BRICKS.defaultBlockState(),Blocks.POLISHED_ANDESITE.defaultBlockState());
                room(l,x+34,d,z+33,12,12,4,Blocks.DEEPSLATE_BRICKS.defaultBlockState(),Blocks.STONE_BRICKS.defaultBlockState(),Blocks.POLISHED_ANDESITE.defaultBlockState());
            }
            case "null_chamber" -> {
                int d=y-20;
                room(l,x+10,d,z+10,28,28,6,Blocks.BLACKSTONE.defaultBlockState(),Blocks.POLISHED_DEEPSLATE.defaultBlockState(),Blocks.BLACKSTONE.defaultBlockState());
                for(int[] q:new int[][]{{13,13},{34,13},{13,34},{34,34}})obelisk(l,x+q[0],d,z+q[1],5,Blocks.BLACKSTONE.defaultBlockState());
                p(l,x+23,d+1,z+23,Blocks.CRYING_OBSIDIAN.defaultBlockState());
            }
            case "hollow_crypt" -> {
                int d=y-24;
                for(int ix=0;ix<3;ix++)for(int iz=0;iz<3;iz++){
                    int rx=x+2+ix*15, rz=z+2+iz*15;
                    room(l,rx,d,rz,11,11,4,Blocks.DEEPSLATE_BRICKS.defaultBlockState(),Blocks.POLISHED_DEEPSLATE.defaultBlockState(),Blocks.DEEPSLATE_BRICKS.defaultBlockState());
                    p(l,rx+5,d+1,rz+5,Blocks.CRYING_OBSIDIAN.defaultBlockState());
                }
                hallX(l,x+2,d,z+23,44,Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState());
                hallZ(l,x+23,d,z+2,44,Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState());
            }
            case "boundary_monument" -> {
                ring(l,x,y,z,23,Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
                for(int[] q:new int[][]{{4,4},{40,4},{4,40},{40,40},{22,3},{22,42},{3,22},{42,22}})
                    obelisk(l,x+q[0],y,z+q[1],8,Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
            }
        }
    }

    private static void courtyard(WorldGenLevel l,int x,int y,int z,BlockState block) {
        for(int a=3;a<45;a++)for(int b=3;b<45;b++) {
            if(a==3||a==44||b==3||b==44||(a%8==0&&b%8==0)) p(l,x+a,y,z+b,block);
        }
    }
    private static void ring(WorldGenLevel l,int x,int y,int z,int radius,BlockState block) {
        int cx=x+24,cz=z+24;
        int outer=radius*radius,inner=Math.max(0,radius-2)*Math.max(0,radius-2);
        for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++) {
            int distance=dx*dx+dz*dz;
            if(distance<=outer&&distance>=inner) p(l,cx+dx,y,cz+dz,block);
        }
    }
    private static void yard(WorldGenLevel l,int x,int y,int z,BlockState fence) {
        for(int a=2;a<46;a++){p(l,x+a,y+1,z+2,fence);p(l,x+a,y+1,z+45,fence);p(l,x+2,y+1,z+a,fence);p(l,x+45,y+1,z+a,fence);}
        for(int a=22;a<=25;a++)air(l,x+a,y+1,z+2);
    }
    private static void room(WorldGenLevel l,int x,int y,int z,int w,int d,int h,BlockState wall,BlockState floor,BlockState roof) {
        for(int a=0;a<w;a++)for(int b=0;b<d;b++){
            p(l,x+a,y,z+b,floor);
            p(l,x+a,y+h,z+b,roof);
            for(int v=1;v<h;v++){
                boolean edge=a==0||a==w-1||b==0||b==d-1;
                if(edge)p(l,x+a,y+v,z+b,wall);else air(l,x+a,y+v,z+b);
            }
        }
        // Doorways are left open by the room caller where required.
    }
    private static void hallX(WorldGenLevel l,int x,int y,int z,int length,BlockState block) {
        for(int a=0;a<length;a++)for(int b=-2;b<=2;b++){
            p(l,x+a,y,z+b,block);
            if(Math.abs(b)==2)for(int h=1;h<=3;h++)p(l,x+a,y+h,z+b,block);
        }
    }
    private static void hallZ(WorldGenLevel l,int x,int y,int z,int length,BlockState block) {
        for(int b=0;b<length;b++)for(int a=-2;a<=2;a++){
            p(l,x+a,y,z+b,block);
            if(Math.abs(a)==2)for(int h=1;h<=3;h++)p(l,x+a,y+h,z+b,block);
        }
    }
    private static void pathX(WorldGenLevel l,int x,int y,int z,int length,BlockState block){for(int a=0;a<length;a++)for(int b=-1;b<=1;b++)p(l,x+a,y,z+b,block);}
    private static void pathZ(WorldGenLevel l,int x,int y,int z,int length,BlockState block){for(int b=0;b<length;b++)for(int a=-1;a<=1;a++)p(l,x+a,y,z+b,block);}
    private static void obelisk(WorldGenLevel l,int x,int y,int z,int h,BlockState block){for(int v=0;v<=h;v++)p(l,x,y+v,z,block);}
    private static void shed(WorldGenLevel l,int x,int y,int z,BlockState wall){room(l,x,y,z,10,8,3,wall,Blocks.COBBLESTONE.defaultBlockState(),Blocks.SPRUCE_SLAB.defaultBlockState());}
    private static void house(WorldGenLevel l,int x,int y,int z){
        room(l,x,y,z,9,9,4,Blocks.OAK_LOG.defaultBlockState(),Blocks.OAK_PLANKS.defaultBlockState(),Blocks.SPRUCE_PLANKS.defaultBlockState());
        p(l,x+4,y+1,z,Blocks.OAK_DOOR.defaultBlockState());
        p(l,x+2,y+2,z+2,Blocks.GLASS_PANE.defaultBlockState());
        p(l,x+6,y+2,z+6,Blocks.BARREL.defaultBlockState());
    }
    private static void well(WorldGenLevel l,int x,int y,int z){
        for(int a=0;a<5;a++)for(int b=0;b<5;b++)if(a==0||a==4||b==0||b==4)p(l,x+a,y+1,z+b,Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        air(l,x+2,y+1,z+2);p(l,x+2,y+2,z+2,Blocks.WATER.defaultBlockState());
    }
    private static void watchPost(WorldGenLevel l,int x,int y,int z){
        for(int h=1;h<=6;h++)p(l,x,y+h,z,Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Y));
        for(int a=-2;a<=2;a++)for(int b=-2;b<=2;b++)if(Math.abs(a)==2||Math.abs(b)==2)p(l,x+a,y+5,z+b,Blocks.SPRUCE_PLANKS.defaultBlockState());
    }

    private static final ThreadLocal<BoundingBox> ACTIVE_CLIP = new ThreadLocal<>();
    private static void p(WorldGenLevel l,int x,int y,int z,BlockState s){
        BoundingBox clip=ACTIVE_CLIP.get();
        if(clip!=null&&(x<clip.minX()||x>clip.maxX()||y<clip.minY()||y>clip.maxY()||z<clip.minZ()||z>clip.maxZ()))return;
        l.setBlock(new BlockPos(x,y,z),s,2);
    }
    private static void air(WorldGenLevel l,int x,int y,int z){p(l,x,y,z,Blocks.AIR.defaultBlockState());}
    private static void chest(WorldGenLevel l,int x,int y,int z,String loot,RandomSource r){
        BoundingBox clip=ACTIVE_CLIP.get();
        if(clip!=null&&(x<clip.minX()||x>clip.maxX()||y<clip.minY()||y>clip.maxY()||z<clip.minZ()||z>clip.maxZ()))return;
        BlockPos pos=new BlockPos(x,y,z);l.setBlock(pos,Blocks.CHEST.defaultBlockState(),2);
        if(l.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity c)
            c.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,ResourceLocation.parse(loot)),r.nextLong());
    }
    /** Concealed lower service chamber shared by every structure identity. */
    private static void hiddenCache(String id,WorldGenLevel l,int x,int surface,int z,RandomSource r) {
        int y=surface-38, rx=x+33, rz=z+2;
        room(l,rx,y,rz,9,7,4,Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(),Blocks.POLISHED_DEEPSLATE.defaultBlockState(),Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        for(int h=y+1;h<surface-1;h++){air(l,x+38,h,z+5);p(l,x+37,h,z+5,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));}
        p(l,x+38,surface-1,z+5,Blocks.IRON_TRAPDOOR.defaultBlockState());
        p(l,x+38,surface,z+5,Blocks.MOSS_CARPET.defaultBlockState());
        p(l,rx+2,y+1,rz+2,Blocks.COBWEB.defaultBlockState());p(l,rx+6,y+1,rz+2,Blocks.LECTERN.defaultBlockState());
        p(l,rx+6,y+1,rz+5,Blocks.SOUL_LANTERN.defaultBlockState());
        switch(id) {
            case "cabin" -> { p(l,rx+2,y+1,rz+4,Blocks.BARREL.defaultBlockState()); p(l,rx+3,y+1,rz+4,Blocks.CAMPFIRE.defaultBlockState()); }
            case "tower" -> { p(l,rx+2,y+1,rz+4,Blocks.TARGET.defaultBlockState()); p(l,rx+3,y+1,rz+4,Blocks.ARROW.defaultBlockState()); }
            case "bunker" -> { p(l,rx+2,y+1,rz+4,Blocks.IRON_BARS.defaultBlockState()); p(l,rx+3,y+1,rz+4,Blocks.REDSTONE_LAMP.defaultBlockState()); }
            case "shrine" -> { p(l,rx+2,y+1,rz+4,Blocks.CRYING_OBSIDIAN.defaultBlockState()); p(l,rx+3,y+1,rz+4,Blocks.BLACKSTONE.defaultBlockState()); }
            case "observatory" -> { p(l,rx+2,y+1,rz+4,Blocks.END_ROD.defaultBlockState()); p(l,rx+3,y+1,rz+4,Blocks.AMETHYST_BLOCK.defaultBlockState()); }
            case "archive" -> { p(l,rx+2,y+1,rz+4,Blocks.BOOKSHELF.defaultBlockState()); p(l,rx+3,y+1,rz+4,Blocks.LECTERN.defaultBlockState()); }
            case "research_facility" -> { p(l,rx+2,y+1,rz+4,Blocks.GLASS.defaultBlockState()); p(l,rx+3,y+1,rz+4,Blocks.IRON_BARS.defaultBlockState()); }
            case "apostle_cathedral" -> { p(l,rx+2,y+1,rz+4,Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState()); p(l,rx+3,y+1,rz+4,Blocks.CRYING_OBSIDIAN.defaultBlockState()); }
            case "forsaken_settlement" -> { p(l,rx+2,y+1,rz+4,Blocks.BARREL.defaultBlockState()); p(l,rx+3,y+1,rz+4,Blocks.WHITE_BED.defaultBlockState()); }
            case "last_outpost" -> { p(l,rx+2,y+1,rz+4,Blocks.COBBLESTONE_WALL.defaultBlockState()); p(l,rx+3,y+1,rz+4,Blocks.TARGET.defaultBlockState()); }
            case "forgotten_station" -> { p(l,rx+2,y+1,rz+4,Blocks.RAIL.defaultBlockState()); p(l,rx+3,y+1,rz+4,Blocks.MINECART.defaultBlockState()); }
            case "null_chamber" -> { p(l,rx+2,y+1,rz+4,Blocks.BLACK_CONCRETE.defaultBlockState()); p(l,rx+3,y+1,rz+4,Blocks.CRYING_OBSIDIAN.defaultBlockState()); }
            case "hollow_crypt" -> { p(l,rx+2,y+1,rz+4,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState()); p(l,rx+3,y+1,rz+4,Blocks.BONE_BLOCK.defaultBlockState()); }
            case "boundary_monument" -> { p(l,rx+2,y+1,rz+4,Blocks.CHISELED_STONE_BRICKS.defaultBlockState()); p(l,rx+3,y+1,rz+4,Blocks.CRYING_OBSIDIAN.defaultBlockState()); }
        }
        chest(l,rx+6,y+1,rz+4,"theoutlands:structures/hidden_cache",r);
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
        chest(l,x+4,y+1,z+4,"theoutlands:structures/cabin",r);
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
        chest(l,x+3,y+8,z+3,"theoutlands:structures/tower",r);
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
        chest(l,x+6,y+1,z+4,"theoutlands:structures/bunker",r);
    }

    // A Fear-associated shrine marked by crying obsidian and silent blackstone pillars.
    private static void shrine(WorldGenLevel l,int x,int y,int z,RandomSource r){
        for(int a=0;a<7;a++)for(int b=0;b<7;b++)
            p(l,x+a,y,z+b,(a+b)%3==0?Blocks.CRACKED_STONE_BRICKS.defaultBlockState():Blocks.STONE_BRICKS.defaultBlockState());
        int[][] c={{1,1},{5,1},{1,5},{5,5}};
        for(int[] q:c)for(int h=1;h<=3;h++)p(l,x+q[0],y+h,z+q[1],Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        p(l,x+3,y+1,z+3,Blocks.CRYING_OBSIDIAN.defaultBlockState());
        p(l,x+3,y+2,z+3,Blocks.SOUL_LANTERN.defaultBlockState());
        chest(l,x+5,y+1,z+3,"theoutlands:structures/shrine",r);
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
        chest(l,x+4,y+1,z+6,"theoutlands:structures/observatory",r);
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
        chest(l,x+4,y+1,z+3,"theoutlands:structures/archive",r);
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
        chest(l,x+7,y+1,z+3,"theoutlands:structures/research_facility",r);
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
        chest(l,x+4,y+1,z+2,"theoutlands:structures/apostle_cathedral",r);
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
        chest(l,x+2,y+1,z+1,"theoutlands:structures/forsaken_settlement",r);
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
        chest(l,x+6,y+1,z+6,"theoutlands:structures/last_outpost",r);
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
        chest(l,x+2,y+1,z+7,"theoutlands:structures/forgotten_station",r);
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
        chest(l,x+5,y+1,z+3,"theoutlands:structures/null_chamber",r);
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
        chest(l,x+7,y+1,z+4,"theoutlands:structures/hollow_crypt",r);
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
        chest(l,x+2,y+1,z+4,"theoutlands:structures/boundary_monument",r);
    }


}
