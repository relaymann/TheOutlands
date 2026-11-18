package com.relaymann.theoutlands.horror;

import java.util.ArrayList;
import java.util.List;

/** Pure policies for bounded encounter history and unique ARG discoveries. */
public final class HorrorMemoryPolicy {
    private HorrorMemoryPolicy() {
    }

    public static boolean isValidId(String id) {
        return id != null && !id.isBlank() && id.length() <= 128;
    }

    public static List<String> appendEncounter(List<String> recent, String eventId, int maximum) {
        if (!isValidId(eventId) || maximum < 1) return List.copyOf(recent);
        List<String> updated = new ArrayList<>(recent);
        updated.add(eventId);
        while (updated.size() > maximum) updated.remove(0);
        return List.copyOf(updated);
    }

    public static List<String> addUnique(List<String> discoveries, String discoveryId) {
        if (!isValidId(discoveryId) || discoveries.contains(discoveryId)) return List.copyOf(discoveries);
        List<String> updated = new ArrayList<>(discoveries);
        updated.add(discoveryId);
        return List.copyOf(updated);
    }
}
