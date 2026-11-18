package com.relaymann.theoutlands.horror;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FearEncounterPolicyTest {
 @Test void unlocksAtTheConfiguredThreshold(){assertFalse(FearEncounterPolicy.shouldIntroduce(14_999,15_000,false,false));assertTrue(FearEncounterPolicy.shouldIntroduce(15_000,15_000,false,false));}
 @Test void onlyIntroducesOnceAndOutsideAlmightyTerritory(){assertFalse(FearEncounterPolicy.shouldIntroduce(20_000,15_000,true,false));assertFalse(FearEncounterPolicy.shouldIntroduce(20_000,15_000,false,true));}
 @Test void invalidInputsFailClosed(){assertFalse(FearEncounterPolicy.shouldIntroduce(Double.NaN,15_000,false,false));assertFalse(FearEncounterPolicy.shouldIntroduce(20_000,Double.POSITIVE_INFINITY,false,false));assertFalse(FearEncounterPolicy.shouldIntroduce(20_000,-1,false,false));}
}
