package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Persistent, bounded world memory for the Outlands ecosystem.
 * Player activity is stored in Overworld SavedData; each creature stores its
 * own last genuine sighting in entity persistent data so investigations survive
 * chunk unloads and restarts without forced targeting or teleportation.
 */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class WorldMemoryManager {
    private static final String ROOT = "theoutlands_world_memory";
    private static final String DATA_ID = "theoutlands_world_memory";
    private static final int MAX_RECORDS = 512;
    private static final long RECORD_LIFETIME = 24_000L;
    private static final long ENTITY_MEMORY_LIFETIME = 900L;
    private static final SavedData.Factory<MemoryData> FACTORY =
            new SavedData.Factory<>(MemoryData::new, MemoryData::load);

    private WorldMemoryManager() {}

    private static MemoryData data(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_ID);
    }

    private static boolean eligible(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    private static boolean isOutlands(Mob mob) {
        return mob.getType().builtInRegistryHolder().unwrapKey()
                .map(key -> key.location().getNamespace().equals(TheOutlands.MOD_ID)).orElse(false);
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getPlayer() instanceof ServerPlayer player) || !eligible(player)) return;
        record(level, event.getPos(), "disturbance", 1);
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof ServerPlayer player) || !eligible(player)) return;
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getKey(level.getBlockState(event.getPos()).getBlock()).getPath();
        if (isTrap(path)) record(level, event.getPos(), "trap", 2);
        if (isDefense(path)) record(level, event.getPos(), "defense", 1);
        if (isLight(path)) record(level, event.getPos(), "light", 1);
    }

    @SubscribeEvent
    public static void onBlockUsed(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof ServerPlayer player) || !eligible(player)) return;
        var state = level.getBlockState(event.getPos());
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        if (path.contains("door")) record(level, event.getPos(), "door", 2);
        if (path.contains("lever") || path.contains("button") || path.contains("pressure_plate")
                || path.contains("tripwire")) record(level, event.getPos(), "trap", 2);
        if (isLight(path)) record(level, event.getPos(), "light", 1);
    }

    @SubscribeEvent
    public static void onOutlandsDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)
                || !(event.getEntity() instanceof Mob mob) || !isOutlands(mob)) return;
        record(level, mob.blockPosition(), "encounter", 3);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 100 != 0
                || !eligible(player) || !player.isCrouching() || player.isPassenger()
                || player.getDeltaMovement().horizontalDistanceSqr() > 0.0009D) return;
        ServerLevel level = player.serverLevel();
        if (level.canSeeSky(player.blockPosition())) return;
        CompoundTag tag = player.getPersistentData();
        long now = level.getGameTime();
        if (now - tag.getLong(ROOT + "_hide_cooldown") < 1_200L) return;
        tag.putLong(ROOT + "_hide_cooldown", now);
        tag.putLong(ROOT + "_hide_pos", player.blockPosition().asLong());
        tag.putString(ROOT + "_hide_dimension", level.dimension().location().toString());
        record(level, player.blockPosition(), "hide", 2);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 20 != 0
                || !OutlandsConfig.ENTITY_INTELLIGENCE_ENABLED.get()) return;
        for (ServerLevel level : event.getServer().getAllLevels()) tick(level);
    }

    private static void tick(ServerLevel level) {
        MemoryData memory = data(level);
        long now = level.getGameTime();
        Set<UUID> processed = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            if (!eligible(player)) continue;
            List<Mob> mobs = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(48.0D),
                    mob -> mob.isAlive() && isOutlands(mob));
            for (Mob mob : mobs) {
                if (!processed.add(mob.getUUID())) continue;
                if (mob.getTarget() instanceof Player target && eligible(target)) {
                    rememberSighting(mob, target, level, now);
                    CompoundTag tag = mob.getPersistentData();
                    if (now - tag.getLong(ROOT + "_last_site_record") >= 1_200L) {
                        record(level, target.blockPosition(), "encounter", 1);
                        tag.putLong(ROOT + "_last_site_record", now);
                    }
                } else {
                    investigateRememberedSite(mob, level, memory, now);
                }
                reactToDoorsLightsTrapsAndDefenses(mob, level, memory);
                shiftTerritoryTowardActivity(mob, level, memory, now);
            }
        }
        if (now % 1_200L == 0L) memory.prune(level.dimension().location().toString(), now);
    }

    private static void rememberSighting(Mob mob, Player player, ServerLevel level, long now) {
        CompoundTag tag = mob.getPersistentData();
        tag.putDouble(ROOT + "_last_x", player.getX());
        tag.putDouble(ROOT + "_last_y", player.getY());
        tag.putDouble(ROOT + "_last_z", player.getZ());
        tag.putString(ROOT + "_last_dimension", level.dimension().location().toString());
        tag.putLong(ROOT + "_last_seen", now);
        if (player.isCrouching() && !level.canSeeSky(player.blockPosition())) {
            tag.putLong(ROOT + "_hide_pos", player.blockPosition().asLong());
            tag.putLong(ROOT + "_hide_time", now);
            tag.putString(ROOT + "_hide_dimension", level.dimension().location().toString());
        }
    }

    private static void investigateRememberedSite(Mob mob, ServerLevel level, MemoryData memory, long now) {
        if (!mob.getNavigation().isDone() || mob.tickCount % 100 != 0) return;
        CompoundTag tag = mob.getPersistentData();
        String dimension = level.dimension().location().toString();
        long lastSeen = tag.getLong(ROOT + "_last_seen");
        if (!dimension.equals(tag.getString(ROOT + "_last_dimension"))
                || lastSeen <= 0L || now - lastSeen > ENTITY_MEMORY_LIFETIME) return;

        BlockPos remembered = BlockPos.containing(tag.getDouble(ROOT + "_last_x"),
                tag.getDouble(ROOT + "_last_y"), tag.getDouble(ROOT + "_last_z"));
        long hideTime = tag.getLong(ROOT + "_hide_time");
        if (dimension.equals(tag.getString(ROOT + "_hide_dimension"))
                && hideTime >= lastSeen - 100L && hideTime <= now) {
            remembered = BlockPos.of(tag.getLong(ROOT + "_hide_pos"));
        }
        Record doorway = memory.nearest(level, mob.blockPosition(), "door", 12.0D);
        if (doorway != null && doorway.time() >= lastSeen - 100L
                && doorway.pos().distSqr(remembered) <= 100.0D) remembered = doorway.pos();
        if (!level.isLoaded(remembered) || mob.distanceToSqr(Vec3.atCenterOf(remembered)) < 9.0D
                || mob.distanceToSqr(Vec3.atCenterOf(remembered)) > 64.0D * 64.0D) return;
        mob.getNavigation().moveTo(remembered.getX() + 0.5D, remembered.getY(),
                remembered.getZ() + 0.5D, 0.78D);
    }

    private static void reactToDoorsLightsTrapsAndDefenses(Mob mob, ServerLevel level, MemoryData memory) {
        if (!mob.getNavigation().isDone() || mob.tickCount % 60 != 0) return;
        if (mob.getTarget() != null) {
            Record obstacle = memory.nearest(level, mob.blockPosition(), "trap", 7.0D);
            if (obstacle == null) obstacle = memory.nearest(level, mob.blockPosition(), "defense", 5.0D);
            if (obstacle != null) {
                Vec3 away = mob.position().subtract(Vec3.atCenterOf(obstacle.pos()));
                if (away.lengthSqr() < 0.01D) away = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 detour = mob.position().add(away.normalize().scale(7.0D));
                BlockPos destination = BlockPos.containing(detour);
                if (level.isLoaded(destination) && level.noCollision(mob, mob.getBoundingBox().move(
                        detour.x - mob.getX(), detour.y - mob.getY(), detour.z - mob.getZ()))) {
                    mob.getNavigation().moveTo(detour.x, detour.y, detour.z, 0.82D);
                }
            }
            return;
        }
        String entityId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                .getKey(mob.getType()).getPath();
        if (!(entityId.contains("stalker") || entityId.contains("lurker")
                || entityId.contains("observer") || entityId.contains("mimic"))) return;
        Record light = memory.nearest(level, mob.blockPosition(), "light", 18.0D);
        if (light == null) return;
        Vec3 away = mob.position().subtract(Vec3.atCenterOf(light.pos()));
        if (away.lengthSqr() < 0.01D) away = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 shadow = mob.position().add(away.normalize().scale(6.0D));
        if (level.isLoaded(BlockPos.containing(shadow))) {
            mob.getNavigation().moveTo(shadow.x, shadow.y, shadow.z, 0.68D);
        }
    }

    private static void shiftTerritoryTowardActivity(Mob mob, ServerLevel level, MemoryData memory, long now) {
        if (mob.getTarget() != null || mob.tickCount % 200 != 0 || !mob.getNavigation().isDone()) return;
        CompoundTag tag = mob.getPersistentData();
        if (now - tag.getLong(ROOT + "_territory_shift") < 1_200L) return;
        Record active = memory.strongest(level, mob.blockPosition(), 36.0D);
        if (active == null || active.pressure() < 4
                || mob.distanceToSqr(Vec3.atCenterOf(active.pos())) < 8.0D * 8.0D) return;
        tag.putLong(ROOT + "_territory_shift", now);
        tag.putLong(ROOT + "_territory_pos", active.pos().asLong());
        BlockPos anchor = active.pos();
        mob.getNavigation().moveTo(anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D, 0.72D);
    }

    private static boolean isTrap(String path) {
        return path.contains("pressure_plate") || path.contains("tripwire") || path.contains("tnt")
                || path.contains("dispenser") || path.contains("cobweb")
                || path.contains("sweet_berry_bush") || path.contains("powder_snow");
    }

    private static boolean isDefense(String path) {
        return path.contains("fence") || path.contains("wall") || path.contains("iron_bars")
                || path.contains("door") || path.contains("trapdoor");
    }

    private static boolean isLight(String path) {
        return path.contains("torch") || path.contains("lantern") || path.contains("lamp")
                || path.contains("glowstone") || path.contains("shroomlight");
    }

    private static void record(ServerLevel level, BlockPos pos, String kind, int strength) {
        data(level).add(level.dimension().location().toString(), pos.immutable(), kind,
                level.getGameTime(), strength);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getPersistentData().remove(ROOT + "_hide_cooldown");
        }
    }

    private record Record(String dimension, BlockPos pos, String kind, long time, int pressure) {}

    private static final class MemoryData extends SavedData {
        private final List<Record> records = new ArrayList<>();
        private MemoryData() {}

        private static MemoryData load(CompoundTag tag, HolderLookup.Provider registries) {
            MemoryData data = new MemoryData();
            ListTag list = tag.getList("records", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                String dimension = entry.getString("dimension");
                String kind = entry.getString("kind");
                if (!dimension.isBlank() && !kind.isBlank()) {
                    data.records.add(new Record(dimension, BlockPos.of(entry.getLong("pos")),
                            kind, entry.getLong("time"), Math.max(1, entry.getInt("pressure"))));
                }
            }
            while (data.records.size() > MAX_RECORDS) data.records.remove(0);
            return data;
        }

        private void add(String dimension, BlockPos pos, String kind, long time, int strength) {
            for (int i = records.size() - 1; i >= 0; i--) {
                Record old = records.get(i);
                if (old.dimension().equals(dimension) && old.kind().equals(kind)
                        && old.pos().distSqr(pos) <= 36.0D) {
                    records.set(i, new Record(dimension, old.pos(), kind, time,
                            Math.min(16, old.pressure() + strength)));
                    setDirty();
                    return;
                }
            }
            records.add(new Record(dimension, pos, kind, time, strength));
            while (records.size() > MAX_RECORDS) records.remove(0);
            setDirty();
        }

        private Record nearest(ServerLevel level, BlockPos origin, String kind, double range) {
            String dimension = level.dimension().location().toString();
            double max = range * range;
            return records.stream().filter(record -> record.dimension().equals(dimension)
                            && record.kind().equals(kind) && level.getGameTime() - record.time() <= RECORD_LIFETIME
                            && record.pos().distSqr(origin) <= max && level.isLoaded(record.pos()))
                    .min(Comparator.comparingDouble(record -> record.pos().distSqr(origin))).orElse(null);
        }

        private Record strongest(ServerLevel level, BlockPos origin, double range) {
            String dimension = level.dimension().location().toString();
            double max = range * range;
            return records.stream().filter(record -> record.dimension().equals(dimension)
                            && (record.kind().equals("encounter") || record.kind().equals("hide")
                            || record.kind().equals("door") || record.kind().equals("disturbance"))
                            && level.getGameTime() - record.time() <= RECORD_LIFETIME
                            && record.pos().distSqr(origin) <= max && level.isLoaded(record.pos()))
                    .max(Comparator.comparingInt(Record::pressure)
                            .thenComparingLong(Record::time)).orElse(null);
        }

        private void prune(String dimension, long now) {
            if (records.removeIf(record -> record.dimension().equals(dimension)
                    && now - record.time() > RECORD_LIFETIME)) setDirty();
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            ListTag list = new ListTag();
            for (Record record : records) {
                CompoundTag entry = new CompoundTag();
                entry.putString("dimension", record.dimension());
                entry.putLong("pos", record.pos().asLong());
                entry.putString("kind", record.kind());
                entry.putLong("time", record.time());
                entry.putInt("pressure", record.pressure());
                list.add(entry);
            }
            tag.put("records", list);
            return tag;
        }
    }
}
