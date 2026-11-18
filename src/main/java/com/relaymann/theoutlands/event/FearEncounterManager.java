package com.relaymann.theoutlands.event;
import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.entity.FearEntity;
import com.relaymann.theoutlands.entity.OutlandsEntities;
import com.relaymann.theoutlands.horror.AlmightyTerritory;
import com.relaymann.theoutlands.horror.DistanceProgression;
import com.relaymann.theoutlands.horror.FearEncounterPolicy;
import com.relaymann.theoutlands.horror.HorrorMemory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
/** Owns Fear's canonical first meeting and the short-lived visible manifestation. */
public final class FearEncounterManager {
 private static final String INTRODUCTION_DISCOVERY="fear_first_introduction";
 private static final long LIFETIME=160L;
 private static final Map<UUID,Active> ACTIVE=new HashMap<>();
 private FearEncounterManager(){}
 public static void evaluate(ServerPlayer player) {
  if(!player.serverLevel().dimension().equals(Level.OVERWORLD))return;
  double distance=DistanceProgression.horizontalDistanceFromWorldSpawn(player);
  boolean almighty=AlmightyTerritory.contains(player.serverLevel(),player.getX(),player.getZ());
  if(FearEncounterPolicy.shouldIntroduce(distance,OutlandsConfig.FEAR_FIRST_APPEARANCE_DISTANCE.get(),
    HorrorMemory.hasDiscovered(player,INTRODUCTION_DISCOVERY),almighty))introduce(player);
 }
 private static void introduce(ServerPlayer player) {
  ServerLevel level=player.serverLevel();float yaw=player.getYRot();double a=Math.toRadians(yaw);
  double x=player.getX()-Math.sin(a)*3.5D,z=player.getZ()+Math.cos(a)*3.5D,y=player.getY()+1.6D;
  if(!level.hasChunkAt(BlockPos.containing(x,y,z)))return;
  FearEntity fear=OutlandsEntities.FEAR.get().create(level);if(fear==null)return;
  fear.moveTo(x,y,z,yaw+180.0F,0.0F);fear.setInvulnerable(true);fear.setSilent(true);fear.setNoAi(true);fear.setNoGravity(true);
  fear.addTag("theoutlands_fear_manifestation");if(!level.addFreshEntity(fear))return;
  HorrorMemory.recordDiscovery(player,INTRODUCTION_DISCOVERY);
  Component line=Component.literal("You are the first person to reach me. You are worthy of my fear.");
  for(ServerPlayer audience:level.players())if(audience.distanceToSqr(player)<=4096.0D){
   HorrorMemory.recordDiscovery(audience,INTRODUCTION_DISCOVERY);
   audience.sendSystemMessage(line);
  }
  ACTIVE.put(fear.getUUID(),new Active(level,fear.getUUID(),level.getGameTime()+LIFETIME));
 }
 public static void tick(ServerLevel level) {
  Iterator<Map.Entry<UUID,Active>> it=ACTIVE.entrySet().iterator();
  while(it.hasNext()){Active a=it.next().getValue();if(a.level!=level)continue;Entity e=level.getEntity(a.id);
   if(e==null||level.getGameTime()>=a.expires){if(e!=null)e.discard();it.remove();}}
 }
 public static void clear(){for(Active a:ACTIVE.values()){Entity e=a.level.getEntity(a.id);if(e!=null)e.discard();}ACTIVE.clear();}
 private record Active(ServerLevel level,UUID id,long expires){}
}
