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
 * <p>Data is stored in NeoForge's persistent player entity compound. The recent
 * encounter list is bounded while the lifetime counter and unique discoveries remain.</p>
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
        if (persistentData == null || !HorrorMemoryPolicy.isValidId(eventId)) return;
        CompoundTag memory = getMemory(persistentData);
        List<String> recent = readStringList(memory, ENCOUNTERS);
        writeStringList(memory, ENCOUNTERS,
                HorrorMemoryPolicy.appendEncounter(recent, eventId, MAX_RECENT_ENCOUNTERS));
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
        return hasEncountered(player.getPersistentData(), eventId);
    }

    static boolean hasEncountered(CompoundTag persistentData, String eventId) {
        return readStringList(getMemory(persistentData), ENCOUNTERS).contains(eventId);
    }

    public static boolean hasDiscovered(ServerPlayer player, String discoveryId) {
        return hasDiscovered(player.getPersistentData(), discoveryId);
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

    /** Copies story memory when NeoForge replaces the player entity during respawn. */
    public static void copy(ServerPlayer original, ServerPlayer replacement) {
        CompoundTag originalMemory = getMemory(original.getPersistentData());
        if (!originalMemory.isEmpty()) replacement.getPersistentData().put(ROOT, originalMemory.copy());
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
