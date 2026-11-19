package com.relaymann.theoutlands.horror;
/** Hidden pacing bands; never exposed through chat or HUD. */
public enum HiddenHorrorStage { UNEASE, DOUBT, PARANOIA, TERROR, DESPAIR;
 public static HiddenHorrorStage fromProgress(int encounters,int tier,int lookBacks,int pauses){int score=Math.max(0,encounters/3)+Math.max(0,tier/2)+Math.max(0,lookBacks/4)+Math.max(0,pauses/8);if(score>=24)return DESPAIR;if(score>=17)return TERROR;if(score>=11)return PARANOIA;if(score>=5)return DOUBT;return UNEASE;}
 public double weightScale(){return switch(this){case UNEASE->0.72D;case DOUBT->0.86D;case PARANOIA->1.0D;case TERROR->1.08D;case DESPAIR->1.12D;};}
}