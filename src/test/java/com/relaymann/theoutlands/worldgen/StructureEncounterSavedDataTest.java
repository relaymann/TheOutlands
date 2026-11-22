package com.relaymann.theoutlands.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StructureEncounterSavedDataTest {
    @Test
    void resolvedSitesSurviveSaveAndLoad() {
        StructureEncounterSavedData original = StructureEncounterSavedData.load(new CompoundTag(), null);
        original.markResolved("minecraft:overworld:12345:-480:960");

        CompoundTag saved = original.save(new CompoundTag(), null);
        StructureEncounterSavedData loaded = StructureEncounterSavedData.load(saved, null);

        assertTrue(loaded.hasResolved("minecraft:overworld:12345:-480:960"));
        assertFalse(loaded.hasResolved("minecraft:overworld:12345:-464:960"));
    }

    @Test
    void duplicateSiteMarksDoNotCreateDuplicateEntries() {
        StructureEncounterSavedData data = StructureEncounterSavedData.load(new CompoundTag(), null);
        data.markResolved("site-a");
        data.markResolved("site-a");

        CompoundTag saved = data.save(new CompoundTag(), null);
        assertEquals(1, saved.getList("resolved_sites", Tag.TAG_STRING).size());
    }

    @Test
    void emptySavedDataLoadsWithoutErrors() {
        StructureEncounterSavedData data = StructureEncounterSavedData.load(new CompoundTag(), null);
        assertFalse(data.hasResolved("site-a"));
    }
}
