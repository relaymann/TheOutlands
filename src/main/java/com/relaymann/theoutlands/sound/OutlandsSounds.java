package com.relaymann.theoutlands.sound;
import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
/** Non-verbal audio for Null and the beings connected to his blessing. */
public final class OutlandsSounds {
 public static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOutlands.MOD_ID);
 public static final DeferredHolder<SoundEvent,SoundEvent> NULL_PRESENCE=register("null_presence");
 public static final DeferredHolder<SoundEvent,SoundEvent> NULL_APOSTLE_WHISPER=register("null_apostle_whisper");
 public static final DeferredHolder<SoundEvent,SoundEvent> NULL_APOSTLE_DEPARTURE=register("null_apostle_departure");
 private static DeferredHolder<SoundEvent,SoundEvent> register(String name){ResourceLocation id=ResourceLocation.fromNamespaceAndPath(TheOutlands.MOD_ID,name);return SOUNDS.register(name,()->SoundEvent.createVariableRangeEvent(id));}
 private OutlandsSounds(){}
}
