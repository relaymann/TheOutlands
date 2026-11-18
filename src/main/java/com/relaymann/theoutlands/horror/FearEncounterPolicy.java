package com.relaymann.theoutlands.horror;
public final class FearEncounterPolicy {
 private FearEncounterPolicy() {}
 public static boolean shouldIntroduce(double distance,double threshold,boolean introduced,boolean almighty) {
  return Double.isFinite(distance)&&Double.isFinite(threshold)&&threshold>=0.0D&&distance>=threshold&&!introduced&&!almighty;
 }
}
