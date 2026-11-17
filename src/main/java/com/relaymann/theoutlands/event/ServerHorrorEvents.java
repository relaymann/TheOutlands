package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.horror.HorrorProgressionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Server-side hooks for the initial progression framework.
 *
 * <p>Evaluation is throttled to once per second per player. Do not put entity scans,
 * chunk scans, structure placement, or expensive event selection in this tick handler.</p>
 */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class ServerHorrorEvents {
    private static final int EVALUATION_INTERVAL_TICKS = 20;

    private ServerHorrorEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.tickCount % EVALUATION_INTERVAL_TICKS != 0) {
            return;
        }

        if (!player.serverLevel().dimension().equals(Level.OVERWORLD)) {
            return;
        }

        HorrorProgressionManager.update(player);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        HorrorProgressionManager.remove(event.getEntity().getUUID());
    }
}
