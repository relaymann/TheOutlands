package com.relaymann.theoutlands.horror;

/**
 * The director's result for one evaluation. A selected event is a proposal only;
 * it is not proof that a world effect has already been executed.
 */
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
