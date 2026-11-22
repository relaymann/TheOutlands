package com.relaymann.theoutlands;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Common server-authoritative tuning for horror pacing and story milestones. */
public final class OutlandsConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue HORROR_INTENSITY;
    public static final ModConfigSpec.DoubleValue EVENT_FREQUENCY;
    public static final ModConfigSpec.DoubleValue FIRST_ANOMALY_DISTANCE;
    public static final ModConfigSpec.DoubleValue FEAR_FIRST_APPEARANCE_DISTANCE;
    public static final ModConfigSpec.DoubleValue DEATH_FIRST_APPEARANCE_DISTANCE;
    public static final ModConfigSpec.DoubleValue FEAR_FULL_TERRITORY_DISTANCE;
    public static final ModConfigSpec.DoubleValue ALMIGHTY_TERRITORY_DEPTH;
    public static final ModConfigSpec.BooleanValue EVENTS_ENABLED;
    public static final ModConfigSpec.BooleanValue NULL_SIGHTINGS_ENABLED;
    public static final ModConfigSpec.BooleanValue HORROR_AUDIO_ENABLED;
    public static final ModConfigSpec.BooleanValue FEAR_AMBIENT_ATTENUATION;
    public static final ModConfigSpec.BooleanValue AMBIENT_ANOMALIES_ENABLED;
    public static final ModConfigSpec.BooleanValue ENTITY_INTELLIGENCE_ENABLED;
    public static final ModConfigSpec.BooleanValue VOICE_HORROR_ENABLED;
    public static final ModConfigSpec.DoubleValue VOICE_DETECTION_RANGE;
    public static final ModConfigSpec.BooleanValue ENTITY_ECOSYSTEM_ENABLED;
    public static final ModConfigSpec.BooleanValue ENTITY_SPAWN_BALANCE_ENABLED;
    public static final ModConfigSpec.BooleanValue OUTLANDS_STRUCTURES_ENABLED;
    public static final ModConfigSpec.DoubleValue STRUCTURE_MIN_DISTANCE;
    public static final ModConfigSpec.DoubleValue STRUCTURE_CHANCE_MULTIPLIER;
    public static final ModConfigSpec.IntValue STRUCTURE_SPACING_CHUNKS;
    public static final ModConfigSpec.DoubleValue AMBIENT_ANOMALY_MIN_DISTANCE;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("pacing");
        HORROR_INTENSITY = BUILDER
                .comment("Overall pressure buildup multiplier. 1.0 is the intended default.")
                .defineInRange("horrorIntensity", 1.0D, 0.1D, 2.5D);
        EVENT_FREQUENCY = BUILDER
                .comment("Scales weighted event selection. 1.0 is the intended default.")
                .defineInRange("eventFrequency", 1.0D, 0.1D, 3.0D);
        EVENTS_ENABLED = BUILDER
                .comment("Enables or disables random Horror Director encounters.")
                .define("eventsEnabled", true);
        BUILDER.pop();

        BUILDER.push("progression");
        FIRST_ANOMALY_DISTANCE = BUILDER
                .comment("Horizontal distance from original shared Overworld spawn where horror begins.")
                .defineInRange("firstAnomalyDistance", 1_000.0D, 1.0D, 30_000_000.0D);
        FEAR_FIRST_APPEARANCE_DISTANCE = BUILDER
                .comment("Distance milestone for Fear's first story appearance.")
                .defineInRange("fearFirstAppearanceDistance", 15_000.0D, 1_000.0D, 30_000_000.0D);
        DEATH_FIRST_APPEARANCE_DISTANCE = BUILDER
                .comment("Distance milestone for Death's first story appearance.")
                .defineInRange("deathFirstAppearanceDistance", 50_000.0D, 1_000.0D, 30_000_000.0D);
        FEAR_FULL_TERRITORY_DISTANCE = BUILDER
                .comment("Distance milestone for Fear's full territorial presence.")
                .defineInRange("fearFullTerritoryDistance", 100_000.0D, 1_000.0D, 30_000_000.0D);
        ALMIGHTY_TERRITORY_DEPTH = BUILDER
                .comment("Depth in blocks measured inward from the actual configured world border.")
                .defineInRange("almightyTerritoryDepth", 5_000.0D, 1.0D, 30_000_000.0D);
        AMBIENT_ANOMALY_MIN_DISTANCE = BUILDER
                .comment("Minimum horizontal distance from world spawn before rare ambient anomalies can occur.")
                .defineInRange("ambientAnomalyMinDistance", 1_000.0D, 0.0D, 30_000_000.0D);
        BUILDER.pop();

        BUILDER.push("presentation");
        NULL_SIGHTINGS_ENABLED = BUILDER
                .comment("Allows Null's temporary watcher sightings.")
                .define("nullSightingsEnabled", true);
        HORROR_AUDIO_ENABLED = BUILDER
                .comment("Enables horror encounter sound effects.")
                .define("horrorAudioEnabled", true);
        FEAR_AMBIENT_ATTENUATION = BUILDER
                .comment("Locally attenuates ambient and weather audio in Fear's distant forest territory.")
                .define("fearAmbientAttenuation", true);
        AMBIENT_ANOMALIES_ENABLED = BUILDER
                .comment("Enables rare, player-private environmental sound anomalies. No terrain is changed.")
                .define("ambientAnomaliesEnabled", true);
        BUILDER.pop();

        BUILDER.push("entitySystems");
        ENTITY_INTELLIGENCE_ENABLED = BUILDER
                .comment("Enables existing creatures' investigation, memory, pursuit adaptation, and home-range behavior.")
                .define("entityIntelligenceEnabled", true);
        ENTITY_ECOSYSTEM_ENABLED = BUILDER
                .comment("Enables existing creatures' territorial relationships and rare inter-entity encounters.")
                .define("entityEcosystemEnabled", true);
        ENTITY_SPAWN_BALANCE_ENABLED = BUILDER
                .comment("Enables distance-based natural spawning and nearby Outlands threat limits. Disable only for unrestricted testing.")
                .define("entitySpawnBalanceEnabled", true);
        OUTLANDS_STRUCTURES_ENABLED = BUILDER
                .comment("Enables rare additive Outlands structures. Vanilla terrain and structures remain enabled.")
                .define("outlandsStructuresEnabled", true);
        STRUCTURE_MIN_DISTANCE = BUILDER
                .comment("Minimum horizontal distance from the original shared Overworld spawn for Outlands structures.")
                .defineInRange("structureMinDistance", 1_000.0D, 0.0D, 30_000_000.0D);
        STRUCTURE_CHANCE_MULTIPLIER = BUILDER
                .comment("Multiplies Outlands structure frequency. 1.0 is the intended default.")
                .defineInRange("structureChanceMultiplier", 1.0D, 0.0D, 10.0D);
        STRUCTURE_SPACING_CHUNKS = BUILDER
                .comment("Minimum chunk spacing between Outlands structure candidate sites. Vanilla structures are unaffected.")
                .defineInRange("structureSpacingChunks", 4, 1, 16);
        VOICE_HORROR_ENABLED = BUILDER
                .comment("Enables optional Simple Voice Chat hearing and voice-reactive entity behavior when Simple Voice Chat is installed.")
                .define("voiceHorrorEnabled", true);
        VOICE_DETECTION_RANGE = BUILDER
                .comment("Base hearing range for Outlands entities when using optional voice chat integration.")
                .defineInRange("voiceDetectionRange", 52.0D, 8.0D, 96.0D);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private OutlandsConfig() {
    }
}
