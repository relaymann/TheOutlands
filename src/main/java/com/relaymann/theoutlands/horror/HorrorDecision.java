package com.relaymann.theoutlands.horror;

public record HorrorDecision(Reason reason, String eventId, double pressure) {
    public enum Reason {
        OUTSIDE_HORROR_RANGE,
        BREATHING_ROOM,
        BUILDING_PRESSURE,
        NO_ELIGIBLE_EVENT,
        EVENT_SELECTED
    }

    public boolean hasSelectedEvent() {
        return reason == Reason.EVENT_SELECTED && eventId != null;
    }

    public static HorrorDecision withoutEvent(Reason reason, double pressure) {
        return new HorrorDecision(reason, null, pressure);
    }
}
