package com.relaymann.theoutlands.horror;

import java.util.Set;

public final class HorrorEventCatalog {
    private static final Set<String> IDS = Set.of(
            "ambient_displacement", "false_footsteps", "distant_figure", "unexplained_message",
            "wrong_sounds", "observer", "environmental_distortion", "missing_terrain",
            "fake_structure", "the_return", "repeated_location", "silent_world",
            "entity_imitation", "false_death", "distant_scream", "false_player",
            "direct_intervention", "fear_whisper", "fear_presence", "fear_call",
            "familiar_echo", "environmental_hush", "almighty_influence", "world_inconsistency",
            "false_structure", "paranoia_echo", "null_apostle_sign", "wall_knock", "behind_steps", "breathing_presence",
            "door_afterimage", "false_wakeup", "mimic_activity", "horizon_giant", "black_room_echo", "impossible_corridor", "distant_door", "memory_intrusion", "silence_break", "false_base", "horizon_wrongness", "black_room_threshold", "fear_landscape");

    private HorrorEventCatalog() {}
    public static Set<String> ids() { return IDS; }
}
