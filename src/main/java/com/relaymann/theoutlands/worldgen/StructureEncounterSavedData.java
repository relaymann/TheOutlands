package com.relaymann.theoutlands.worldgen;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

public final class StructureEncounterSavedData extends SavedData {
    private static final String DATA_ID = "theoutlands_structure_encounters";
    private static final String SITES_TAG = "resolved_sites";
    private static final SavedData.Factory<StructureEncounterSavedData> FACTORY =
            new SavedData.Factory<>(StructureEncounterSavedData::new, StructureEncounterSavedData::load);

    private final Set<String> resolvedSites = new HashSet<>();

    private StructureEncounterSavedData() {}

    public static StructureEncounterSavedData get(ServerLevel level) {

        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_ID);
    }

    public static StructureEncounterSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        StructureEncounterSavedData data = new StructureEncounterSavedData();
        ListTag sites = tag.getList(SITES_TAG, Tag.TAG_STRING);
        for (int i = 0; i < sites.size(); i++) {
            data.resolvedSites.add(sites.getString(i));
        }
        return data;
    }

    public boolean hasResolved(String siteKey) {
        return resolvedSites.contains(siteKey);
    }

    public void markResolved(String siteKey) {
        if (resolvedSites.add(siteKey)) setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag sites = new ListTag();
        for (String site : resolvedSites) sites.add(StringTag.valueOf(site));
        tag.put(SITES_TAG, sites);
        return tag;
    }
}
