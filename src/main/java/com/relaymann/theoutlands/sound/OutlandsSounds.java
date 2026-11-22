package com.relaymann.theoutlands.sound;
import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
/** Original non-verbal audio for entities and structure-specific atmospheres. */
public final class OutlandsSounds {
 public static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOutlands.MOD_ID);
 public static final DeferredHolder<SoundEvent,SoundEvent> NULL_PRESENCE=register("null_presence");
 public static final DeferredHolder<SoundEvent,SoundEvent> NULL_APOSTLE_WHISPER=register("null_apostle_whisper");
 public static final DeferredHolder<SoundEvent,SoundEvent> NULL_APOSTLE_DEPARTURE=register("null_apostle_departure");
 public static final DeferredHolder<SoundEvent,SoundEvent> HUNTER_STALK=register("hunter_stalk");
 public static final DeferredHolder<SoundEvent,SoundEvent> CAVEJAR_STALK=register("cavejar_stalk");
 public static final DeferredHolder<SoundEvent,SoundEvent> STALKER_BREATH=register("stalker_breath");
 public static final DeferredHolder<SoundEvent,SoundEvent> STALKER_JOINT=register("stalker_joint");
 public static final DeferredHolder<SoundEvent,SoundEvent> STALKER_LUNGE=register("stalker_lunge");
 public static final DeferredHolder<SoundEvent,SoundEvent> NIGHTMARE_SCREECH=register("nightmare_screech");
 public static final DeferredHolder<SoundEvent,SoundEvent> OBSERVER_CLICK=register("observer_click");
 public static final DeferredHolder<SoundEvent,SoundEvent> BURROWER_RISE=register("burrower_rise");
 public static final DeferredHolder<SoundEvent,SoundEvent> MIMIC_REVEAL=register("mimic_reveal");
 public static final DeferredHolder<SoundEvent,SoundEvent> STILLBORN_STONE=register("stillborn_stone");
 public static final DeferredHolder<SoundEvent,SoundEvent> STILLBORN_STRIKE=register("stillborn_strike");
 public static final DeferredHolder<SoundEvent,SoundEvent> AGITATED_WARNING=register("agitated_warning");
 public static final DeferredHolder<SoundEvent,SoundEvent> AGITATED_CHARGE=register("agitated_charge");
 public static final DeferredHolder<SoundEvent,SoundEvent> PILGRIM_BREATH=register("pilgrim_breath"), PILGRIM_HURT=register("pilgrim_hurt"), CEILING_CRAWL=register("ceiling_crawl"), CEILING_HIT=register("ceiling_hit"), COLOSSUS_STEP=register("colossus_step"), COLOSSUS_HURT=register("colossus_hurt"), PILGRIM_STEP=register("pilgrim_step"), CEILING_DROP=register("ceiling_drop"), COLOSSUS_WARNING=register("colossus_warning"), COLOSSUS_STOMP=register("colossus_stomp"), COLOSSUS_RUMBLE=register("colossus_rumble");
 private static DeferredHolder<SoundEvent,SoundEvent> register(String name){ResourceLocation id=ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,name);return SOUNDS.register(name,()->SoundEvent.createVariableRangeEvent(id));}

 public static final DeferredHolder<SoundEvent,SoundEvent> LURKER_BREATH=register("lurker_breath");
 public static final DeferredHolder<SoundEvent,SoundEvent> LURKER_HIT=register("lurker_hit");
 public static final DeferredHolder<SoundEvent,SoundEvent> HOWLER_BREATH=register("howler_breath");
 public static final DeferredHolder<SoundEvent,SoundEvent> HOWLER_CALL=register("howler_call");
 public static final DeferredHolder<SoundEvent,SoundEvent> HOWLER_HIT=register("howler_hit");
 public static final DeferredHolder<SoundEvent,SoundEvent> DREDGE_CRAWL=register("dredge_crawl");
 public static final DeferredHolder<SoundEvent,SoundEvent> DREDGE_LUNGE=register("dredge_lunge");
 public static final DeferredHolder<SoundEvent,SoundEvent> DREDGE_HIT=register("dredge_hit");
 public static final DeferredHolder<SoundEvent,SoundEvent> FEAR_PRESENCE=register("fear_presence");
 public static final DeferredHolder<SoundEvent,SoundEvent> DEATH_PRESENCE=register("death_presence");
 public static final DeferredHolder<SoundEvent,SoundEvent> ALMIGHTY_PRESENCE=register("almighty_presence");
 public static final DeferredHolder<SoundEvent,SoundEvent> STRUCTURE_CABIN=register("structure_cabin");
 public static final DeferredHolder<SoundEvent,SoundEvent> STRUCTURE_TOWER=register("structure_watchtower");
 public static final DeferredHolder<SoundEvent,SoundEvent> STRUCTURE_BUNKER=register("structure_bunker");
 public static final DeferredHolder<SoundEvent,SoundEvent> STRUCTURE_SHRINE=register("structure_shrine");
 public static final DeferredHolder<SoundEvent,SoundEvent> STRUCTURE_OBSERVATORY=register("structure_observatory");
 public static final DeferredHolder<SoundEvent,SoundEvent> STRUCTURE_ARCHIVE=register("structure_death_archive");
 public static final DeferredHolder<SoundEvent,SoundEvent> STRUCTURE_RESEARCH=register("structure_research_facility");
 public static final DeferredHolder<SoundEvent,SoundEvent> STRUCTURE_CATHEDRAL=register("structure_apostle_cathedral");
 public static final DeferredHolder<SoundEvent,SoundEvent> STRUCTURE_SETTLEMENT=register("structure_forsaken_settlement");
 public static final DeferredHolder<SoundEvent,SoundEvent> STRUCTURE_OUTPOST=register("structure_last_outpost");
 public static final DeferredHolder<SoundEvent,SoundEvent> STRUCTURE_STATION=register("structure_forgotten_station");
 public static final DeferredHolder<SoundEvent,SoundEvent> STRUCTURE_NULL_CHAMBER=register("structure_null_chamber");
 public static final DeferredHolder<SoundEvent,SoundEvent> STRUCTURE_CRYPT=register("structure_hollow_crypt");
 public static final DeferredHolder<SoundEvent,SoundEvent> STRUCTURE_MONUMENT=register("structure_boundary_monument");
 public static final DeferredHolder<SoundEvent,SoundEvent> NIGHTMARE_WATCHER=register("nightmare_watcher");
 public static final DeferredHolder<SoundEvent,SoundEvent> NIGHTMARE_HOUND=register("nightmare_hound");
 public static final DeferredHolder<SoundEvent,SoundEvent> NIGHTMARE_WARDENED=register("nightmare_wardened");
 public static final DeferredHolder<SoundEvent,SoundEvent> NIGHTMARE_FALSE_SURVIVOR=register("nightmare_false_survivor");
 public static final DeferredHolder<SoundEvent,SoundEvent> NIGHTMARE_UNBOUND=register("nightmare_unbound");
 private OutlandsSounds(){}
}
