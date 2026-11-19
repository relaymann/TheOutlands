package com.relaymann.theoutlands.horror;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/** Persistent, server-authoritative, per-player encounter, behavior, and place memory. */
public final class HorrorMemory {
    private static final String ROOT = "theoutlands_memory";
    private static final String ENCOUNTERS = "encounters";
    private static final String DISCOVERIES = "discoveries";
    private static final String LOCATIONS = "visited_cells";
    private static final int MAX_RECENT_ENCOUNTERS = 32;
    private static final int MAX_VISITED_CELLS = 24;
    private static final int CELL_SIZE = 32;

    private HorrorMemory() {}

    public static void recordEncounter(ServerPlayer player, String eventId) {
        recordEncounter(player.getPersistentData(), eventId, player.serverLevel().getGameTime());
    }

    static void recordEncounter(CompoundTag persistentData, String eventId, long gameTime) {
        if (persistentData == null || !HorrorMemoryPolicy.isValidId(eventId)) return;
        CompoundTag memory = getMemory(persistentData);
        writeStringList(memory, ENCOUNTERS,
                HorrorMemoryPolicy.appendEncounter(readStringList(memory, ENCOUNTERS), eventId, MAX_RECENT_ENCOUNTERS));
        memory.putInt("total_encounters", memory.getInt("total_encounters") + 1);
        memory.putLong("last_encounter_game_time", gameTime);
        persistentData.put(ROOT, memory);
    }

    public static void recordDiscovery(ServerPlayer player, String discoveryId) {
        recordDiscovery(player.getPersistentData(), discoveryId);
    }

    static void recordDiscovery(CompoundTag persistentData, String discoveryId) {
        if (persistentData == null || !HorrorMemoryPolicy.isValidId(discoveryId)) return;
        CompoundTag memory = getMemory(persistentData);
        writeStringList(memory, DISCOVERIES,
                HorrorMemoryPolicy.addUnique(readStringList(memory, DISCOVERIES), discoveryId));
        persistentData.put(ROOT, memory);
    }

    public static boolean hasEncountered(ServerPlayer player, String eventId) {
        return readStringList(getMemory(player.getPersistentData()), ENCOUNTERS).contains(eventId);
    }

    static boolean hasEncountered(CompoundTag persistentData, String eventId) {
        return readStringList(getMemory(persistentData), ENCOUNTERS).contains(eventId);
    }

    public static boolean hasDiscovered(ServerPlayer player, String discoveryId) {
        return readStringList(getMemory(player.getPersistentData()), DISCOVERIES).contains(discoveryId);
    }

    static boolean hasDiscovered(CompoundTag persistentData, String discoveryId) {
        return readStringList(getMemory(persistentData), DISCOVERIES).contains(discoveryId);
    }

    public static int totalEncounters(ServerPlayer player) {
        return getMemory(player.getPersistentData()).getInt("total_encounters");
    }

    static int totalEncounters(CompoundTag persistentData) {
        return getMemory(persistentData).getInt("total_encounters");
    }

    public static long lastEncounterGameTime(ServerPlayer player) {
        return getMemory(player.getPersistentData()).getLong("last_encounter_game_time");
    }

    public static List<String> recentEncounters(ServerPlayer player) {
        return readStringList(getMemory(player.getPersistentData()), ENCOUNTERS);
    }

    public static List<String> discoveries(ServerPlayer player) {
        return readStringList(getMemory(player.getPersistentData()), DISCOVERIES);
    }

    /**
     * Records a coarse 32-block cell. Returns true when the player has visited it
     * before. The bounded list deliberately avoids storing a trail of exact coordinates.
     */
    public static boolean recordLocation(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        CompoundTag memory = getMemory(data);
        BlockPos cell = cellFor(player.blockPosition());
        long packed = cell.asLong();
        ListTag old = memory.getList(LOCATIONS, Tag.TAG_LONG);
        boolean repeated = old.contains(LongTag.valueOf(packed));
        if (old.isEmpty() || ((LongTag) old.get(old.size() - 1)).getAsLong() != packed) {
            ListTag updated = new ListTag();
            for (int i = 0; i < old.size(); i++) {
                long value = ((LongTag) old.get(i)).getAsLong();
                if (value != packed) updated.add(LongTag.valueOf(value));
            }
            updated.add(LongTag.valueOf(packed));
            while (updated.size() > MAX_VISITED_CELLS) updated.remove(0);
            memory.put(LOCATIONS, updated);
        }
        data.put(ROOT, memory);
        return repeated;
    }

    public static List<BlockPos> rememberedLocations(ServerPlayer player) {
        ListTag list = getMemory(player.getPersistentData()).getList(LOCATIONS, Tag.TAG_LONG);
        List<BlockPos> positions = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) positions.add(BlockPos.of(((LongTag) list.get(i)).getAsLong()));
        return List.copyOf(positions);
    }

    /** Samples only once per progression interval; tracks pauses, sharp look-backs and flight. */
    public static void recordBehaviorSample(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        CompoundTag memory = getMemory(data);
        float yaw = player.getYRot();
        if (memory.contains("behavior_yaw")) {
            float delta = Math.abs(yaw - memory.getFloat("behavior_yaw"));
            delta = Math.min(delta, 360.0F - delta);
            if (delta >= 115.0F) memory.putInt("look_backs", memory.getInt("look_backs") + 1);
        }
        memory.putFloat("behavior_yaw", yaw);
        double speed = player.getDeltaMovement().horizontalDistanceSqr();
        if (player.onGround() && speed < 0.0009D && !player.isPassenger()) {
            memory.putInt("pause_samples", Math.min(10_000, memory.getInt("pause_samples") + 1));
        }
        if (player.isSprinting() && speed > 0.02D) {
            memory.putInt("flight_samples", Math.min(10_000, memory.getInt("flight_samples") + 1));
        }
        memory.putInt("look_backs", Math.min(10_000, memory.getInt("look_backs")));
        data.put(ROOT, memory);
    }

    public static int lookBackCount(ServerPlayer player) {
        return getMemory(player.getPersistentData()).getInt("look_backs");
    }

    public static int pauseCount(ServerPlayer player) {
        return getMemory(player.getPersistentData()).getInt("pause_samples");
    }

    public static int flightCount(ServerPlayer player) {
        return getMemory(player.getPersistentData()).getInt("flight_samples");
    }

    public static long lastFamiliarDistortion(ServerPlayer player) {
        CompoundTag memory = getMemory(player.getPersistentData());
        return memory.contains("last_familiar_distortion") ? memory.getLong("last_familiar_distortion") : -6_000L;
    }

    public static void setLastFamiliarDistortion(ServerPlayer player, long time) {
        CompoundTag data = player.getPersistentData();
        CompoundTag memory = getMemory(data);
        memory.putLong("last_familiar_distortion", time);
        data.put(ROOT, memory);
    }

    public static long lastAlmightySign(ServerPlayer player) {
        CompoundTag memory = getMemory(player.getPersistentData());
        return memory.contains("last_almighty_sign") ? memory.getLong("last_almighty_sign") : -24_000L;
    }

    public static void setLastAlmightySign(ServerPlayer player, long time) {
        CompoundTag data = player.getPersistentData();
        CompoundTag memory = getMemory(data);
        memory.putLong("last_almighty_sign", time);
        data.put(ROOT, memory);
    }

    public static long lastDeathSpawn(ServerPlayer player) {
        CompoundTag memory = getMemory(player.getPersistentData());
        return memory.contains("last_death_spawn") ? memory.getLong("last_death_spawn") : -12_000L;
    }

    public static long lastAggressiveJumpscare(ServerPlayer player) {
        CompoundTag memory = getMemory(player.getPersistentData());
        return memory.contains("last_aggressive_jumpscare")
                ? memory.getLong("last_aggressive_jumpscare") : -36_000L;
    }

    public static void setLastAggressiveJumpscare(ServerPlayer player, long gameTime) {
        CompoundTag data = player.getPersistentData();
        CompoundTag memory = getMemory(data);
        memory.putLong("last_aggressive_jumpscare", gameTime);
        data.put(ROOT, memory);
    }

    public static void setLastDeathSpawn(ServerPlayer player, long time) {
        CompoundTag data = player.getPersistentData();
        CompoundTag memory = getMemory(data);
        memory.putLong("last_death_spawn", time);
        data.put(ROOT, memory);
    }

    private static final String HAUNTED_CELLS = "haunted_cells";
    private static final int MAX_HAUNTED_CELLS = 16;

    /** Marks a coarse visited cell as haunted; returns true if it was already marked. */
    public static boolean markHauntedLocation(ServerPlayer player, BlockPos position) {
        CompoundTag data = player.getPersistentData();
        CompoundTag memory = getMemory(data);
        long packed = cellFor(position).asLong();
        ListTag old = memory.getList(HAUNTED_CELLS, Tag.TAG_LONG);
        boolean existed = old.contains(LongTag.valueOf(packed));
        if (!existed) {
            ListTag updated = new ListTag();
            for (int i = Math.max(0, old.size() - MAX_HAUNTED_CELLS + 1); i < old.size(); i++)
                updated.add(old.get(i).copy());
            updated.add(LongTag.valueOf(packed));
            memory.put(HAUNTED_CELLS, updated);
        }
        data.put(ROOT, memory);
        return existed;
    }

    public static boolean isHauntedLocation(ServerPlayer player, BlockPos position) {
        long packed = cellFor(position).asLong();
        return getMemory(player.getPersistentData()).getList(HAUNTED_CELLS, Tag.TAG_LONG)
                .contains(LongTag.valueOf(packed));
    }

    public static void recordNullObservation(ServerPlayer player, long gameTime) {
        CompoundTag data = player.getPersistentData();
        CompoundTag memory = getMemory(data);
        memory.putInt("null_observations", Math.min(10_000, memory.getInt("null_observations") + 1));
        memory.putLong("last_null_observation", gameTime);
        data.put(ROOT, memory);
    }

    public static int nullObservationCount(ServerPlayer player) {
        return getMemory(player.getPersistentData()).getInt("null_observations");
    }

    /** Records a completed watcher appearance without storing exact player coordinates. */
    public static void recordNullStalking(ServerPlayer player, long gameTime) {
        CompoundTag data = player.getPersistentData();
        CompoundTag memory = getMemory(data);
        memory.putInt("null_stalking_count", Math.min(10_000, memory.getInt("null_stalking_count") + 1));
        memory.putLong("last_null_stalk", gameTime);
        data.put(ROOT, memory);
    }

    public static int nullStalkingCount(ServerPlayer player) {
        return getMemory(player.getPersistentData()).getInt("null_stalking_count");
    }

    public static long lastNullStalking(ServerPlayer player) {
        CompoundTag memory = getMemory(player.getPersistentData());
        return memory.contains("last_null_stalk") ? memory.getLong("last_null_stalk") : -24_000L;
    }

    public static int deathEncounterCount(ServerPlayer player) {
        return getMemory(player.getPersistentData()).getInt("death_encounters");
    }

    public static void recordDeathEncounter(ServerPlayer player, long gameTime) {
        CompoundTag data = player.getPersistentData();
        CompoundTag memory = getMemory(data);
        memory.putInt("death_encounters", Math.min(10_000, memory.getInt("death_encounters") + 1));
        memory.putLong("last_death_spawn", gameTime);
        data.put(ROOT, memory);
    }

    public static long lastNullApostleSpawn(ServerPlayer player) {
        CompoundTag memory = getMemory(player.getPersistentData());
        return memory.contains("last_null_apostle_spawn") ? memory.getLong("last_null_apostle_spawn") : -24_000L;
    }

    public static void setLastNullApostleSpawn(ServerPlayer player, long gameTime) {
        CompoundTag data = player.getPersistentData();
        CompoundTag memory = getMemory(data);
        memory.putLong("last_null_apostle_spawn", gameTime);
        data.put(ROOT, memory);
    }

    private static final String SLEEP_CELLS = "sleep_cells";
    private static final String BUILD_CELLS = "build_cells";
    private static final int MAX_CONTEXT_CELLS = 8;

    public static void recordSleepLocation(ServerPlayer player, BlockPos position) {
        recordContextCell(player, SLEEP_CELLS, position);
    }

    public static void recordBuildLocation(ServerPlayer player, BlockPos position) {
        recordContextCell(player, BUILD_CELLS, position);
    }

    private static void recordContextCell(ServerPlayer player, String key, BlockPos position) {
        CompoundTag data = player.getPersistentData();
        CompoundTag memory = getMemory(data);
        long packed = cellFor(position).asLong();
        ListTag old = memory.getList(key, Tag.TAG_LONG);
        if (old.contains(LongTag.valueOf(packed))) return;
        ListTag updated = new ListTag();
        for (int i = Math.max(0, old.size() - MAX_CONTEXT_CELLS + 1); i < old.size(); i++)
            updated.add(old.get(i).copy());
        updated.add(LongTag.valueOf(packed));
        memory.put(key, updated);
        data.put(ROOT, memory);
    }

    public static List<BlockPos> rememberedSleepLocations(ServerPlayer player) {
        return readPositionList(getMemory(player.getPersistentData()), SLEEP_CELLS);
    }

    public static List<BlockPos> rememberedBuildLocations(ServerPlayer player) {
        return readPositionList(getMemory(player.getPersistentData()), BUILD_CELLS);
    }

    private static List<BlockPos> readPositionList(CompoundTag memory, String key) {
        ListTag list = memory.getList(key, Tag.TAG_LONG);
        List<BlockPos> positions = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) positions.add(BlockPos.of(((LongTag) list.get(i)).getAsLong()));
        return List.copyOf(positions);
    }

    public static void copy(ServerPlayer original, ServerPlayer replacement) {
        CompoundTag originalMemory = getMemory(original.getPersistentData());
        if (!originalMemory.isEmpty()) replacement.getPersistentData().put(ROOT, originalMemory.copy());
    }

    private static BlockPos cellFor(BlockPos position) {
        return new BlockPos(Math.floorDiv(position.getX(), CELL_SIZE) * CELL_SIZE,
                Math.floorDiv(position.getY(), 16) * 16,
                Math.floorDiv(position.getZ(), CELL_SIZE) * CELL_SIZE);
    }

    private static CompoundTag getMemory(CompoundTag persistentData) {
        if (persistentData == null || !persistentData.contains(ROOT, Tag.TAG_COMPOUND)) return new CompoundTag();
        return persistentData.getCompound(ROOT);
    }

    private static List<String> readStringList(CompoundTag memory, String key) {
        ListTag list = memory.getList(key, Tag.TAG_STRING);
        List<String> values = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) values.add(list.getString(i));
        return values;
    }

    private static void writeStringList(CompoundTag memory, String key, List<String> values) {
        ListTag list = new ListTag();
        for (String value : values) list.add(StringTag.valueOf(value));
        memory.put(key, list);
    }
}
