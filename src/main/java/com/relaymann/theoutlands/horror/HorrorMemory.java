package com.relaymann.theoutlands.horror;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistent per-player encounter and discovery history.
 *
 * <p>Data is stored in NeoForge's entity persistent compound, which is saved with
 * the player. The encounter list is intentionally bounded while the total counter
 * and unique discovery list are retained.</p>
 */
public final class HorrorMemory {
    private static final String ROOT = "theoutlands_memory";
    private static final String ENCOUNTERS = "encounters";
    private static final String DISCOVERIES = "discoveries";
    private static final int MAX_RECENT_ENCOUNTERS = 32;

    private HorrorMemory() {
    }

    public static void recordEncounter(ServerPlayer player, String eventId) {
        recordEncounter(player.getPersistentData(), eventId, player.serverLevel().getGameTime());
    }

    static void recordEncounter(CompoundTag persistentData, String eventId, long gameTime) {
        if (persistentData == null || eventId == null || eventId.isBlank()) return;
        CompoundTag memory = getOrCreateMemory(persistentData);
        ListTag encounters = getOrCreateList(memory, ENCOUNTERS);
        encounters.add(StringTag.valueOf(eventId));
        while (encounters.size() > MAX_RECENT_ENCOUNTERS) encounters.remove(0);
        memory.putInt("total_encounters", memory.getInt("total_encounters") + 1);
        memory.putLong("last_encounter_game_time", gameTime);
        persistentData.put(ROOT, memory);
    }

    public static void recordDiscovery(ServerPlayer player, String discoveryId) {
        recordDiscovery(player.getPersistentData(), discoveryId);
    }

    static void recordDiscovery(CompoundTag persistentData, String discoveryId) {
        if (persistentData == null || discoveryId == null || discoveryId.isBlank()) return;
        CompoundTag memory = getOrCreateMemory(persistentData);
        ListTag discoveries = getOrCreateList(memory, DISCOVERIES);
        StringTag value = StringTag.valueOf(discoveryId);
        if (!discoveries.contains(value)) discoveries.add(value);
        persistentData.put(ROOT, memory);
    }

    public static boolean hasEncountered(ServerPlayer player, String eventId) {
        return hasEncountered(player.getPersistentData(), eventId);
    }

    static boolean hasEncountered(CompoundTag persistentData, String eventId) {
        return readStringList(persistentData, ENCOUNTERS).contains(eventId);
    }

    public static boolean hasDiscovered(ServerPlayer player, String discoveryId) {
        return hasDiscovered(player.getPersistentData(), discoveryId);
    }

    static boolean hasDiscovered(CompoundTag persistentData, String discoveryId) {
        return readStringList(persistentData, DISCOVERIES).contains(discoveryId);
    }

    public static int totalEncounters(ServerPlayer player) {
        return totalEncounters(player.getPersistentData());
    }

    static int totalEncounters(CompoundTag persistentData) {
        CompoundTag memory = getMemory(persistentData);
        return memory.getInt("total_encounters");
    }

    public static long lastEncounterGameTime(ServerPlayer player) {
        return getMemory(player.getPersistentData()).getLong("last_encounter_game_time");
    }

    public static List<String> recentEncounters(ServerPlayer player) {
        return readStringList(player.getPersistentData(), ENCOUNTERS);
    }

    public static List<String> discoveries(ServerPlayer player) {
        return readStringList(player.getPersistentData(), DISCOVERIES);
    }

    /** Copies story memory when NeoForge replaces the player entity during respawn. */
    public static void copy(ServerPlayer original, ServerPlayer replacement) {
        CompoundTag originalMemory = getMemory(original.getPersistentData());
        if (!originalMemory.isEmpty()) replacement.getPersistentData().put(ROOT, originalMemory.copy());
    }

    private static CompoundTag getMemory(CompoundTag persistentData) {
        if (persistentData == null || !persistentData.contains(ROOT, Tag.TAG_COMPOUND)) return new CompoundTag();
        return persistentData.getCompound(ROOT);
    }

    private static CompoundTag getOrCreateMemory(CompoundTag persistentData) {
        return getMemory(persistentData);
    }

    private static ListTag getOrCreateList(CompoundTag memory, String key) {
        if (memory.contains(key, Tag.TAG_LIST)) return memory.getList(key, Tag.TAG_STRING);
        ListTag list = new ListTag();
        memory.put(key, list);
        return list;
    }

    private static List<String> readStringList(CompoundTag persistentData, String key) {
        CompoundTag memory = getMemory(persistentData);
        ListTag list = memory.getList(key, Tag.TAG_STRING);
        List<String> values = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) values.add(list.getString(i));
        return List.copyOf(values);
    }
}
