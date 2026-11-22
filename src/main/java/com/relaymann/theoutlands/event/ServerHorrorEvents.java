package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.NightmareMode;
import com.relaymann.theoutlands.horror.HorrorMemory;
import com.relaymann.theoutlands.worldgen.StructureEncounterManager;
import com.relaymann.theoutlands.horror.HorrorProgressionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class ServerHorrorEvents {
    private ServerHorrorEvents() {}

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) NightmareMode.refresh(level);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        FootstepEchoManager.tick(player);
        if (player.tickCount % 20 != 0
                || !player.serverLevel().dimension().equals(Level.OVERWORLD)) return;
        HorrorProgressionManager.update(player);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (event.getServer().getTickCount() % 10 == 0) VoicePerceptionManager.tick(level);
            NullWatcherManager.tick(level);
            NullApostleManager.tick(level);
            FearEncounterManager.tick(level);
            AggressiveJumpscareManager.tick(level);
            ClientAfterimageManager.tick(level);
            if (event.getServer().getTickCount() % 20 == 0) {
                EnvironmentalAnomalyManager.tick(level);
                EntityTraceManager.tick(level);
                StructureAmbienceManager.tick(level);
                StructureEncounterManager.tick(level);
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ClientAfterimageManager.clear(player);
            AggressiveJumpscareManager.clear(player);
            FootstepEchoManager.clear(player);
            EnvironmentalAnomalyManager.clear(player);
            StructureAmbienceManager.clear(player);
            VoicePerceptionManager.clear(player.getUUID());
            EntityTraceManager.clear(player);
        }
        HorrorProgressionManager.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HorrorMemory.recordBuildLocation(player, event.getPos());
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HorrorMemory.recordDeathLocation(player, player.blockPosition());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer original
                && event.getEntity() instanceof ServerPlayer replacement) {
            HorrorMemory.copy(original, replacement);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        HorrorProgressionManager.clear();
        FearEncounterManager.clear();
        AggressiveJumpscareManager.clear();
        NullApostleManager.clear();
        DeathHuntManager.clear();
        ClientAfterimageManager.clear();
        FootstepEchoManager.clear();
        EnvironmentalAnomalyManager.clear();
        StructureAmbienceManager.clear();
        OutlandsIntelligenceManager.clear();
        PredatoryStalkingManager.clear(event.getServer().getAllLevels());
        EntityTraceManager.clear();
        VoicePerceptionManager.clear();
        NightmareMode.clear();
    }
}
