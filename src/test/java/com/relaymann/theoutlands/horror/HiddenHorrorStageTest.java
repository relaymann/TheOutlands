package com.relaymann.theoutlands.horror;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class HiddenHorrorStageTest{@Test void stagesAdvance(){assertEquals(HiddenHorrorStage.UNEASE,HiddenHorrorStage.fromProgress(0,0,0,0));assertEquals(HiddenHorrorStage.DOUBT,HiddenHorrorStage.fromProgress(15,0,0,0));assertEquals(HiddenHorrorStage.PARANOIA,HiddenHorrorStage.fromProgress(33,0,0,0));assertEquals(HiddenHorrorStage.TERROR,HiddenHorrorStage.fromProgress(51,0,0,0));assertEquals(HiddenHorrorStage.DESPAIR,HiddenHorrorStage.fromProgress(72,0,0,0));}}
