package com.relaymann.theoutlands.event;

import com.mojang.brigadier.Command;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.horror.AlmightyTerritory;
import com.relaymann.theoutlands.horror.ApostleProgression;
import com.relaymann.theoutlands.horror.HorrorProgressionManager;
import com.relaymann.theoutlands.OutlandsConfig;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = TheOutlands.MOD_ID)
public final class HorrorDebugCommand {
    private HorrorDebugCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("theoutlands")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("debug").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    var snapshot = HorrorProgressionManager.getSnapshot(player.getUUID());
                    var decision = HorrorProgressionManager.getLastDecision(player.getUUID());
                    if (snapshot.isEmpty() || decision.isEmpty()) {
                        context.getSource().sendFailure(Component.literal(
                                "No horror evaluation yet; wait up to one second."));
                        return 0;
                    }
                    var s = snapshot.orElseThrow();
                    var d = decision.orElseThrow();
                    double borderDistance = AlmightyTerritory.distanceInsideBorder(
                            player.serverLevel(), player.getX(), player.getZ());
                    boolean almightyLand = AlmightyTerritory.contains(
                            player.serverLevel(), player.getX(), player.getZ());
                    var apostles = ApostleProgression.evaluate(s.distanceFromSpawn(), almightyLand,
                            OutlandsConfig.FEAR_FIRST_APPEARANCE_DISTANCE.get(),
                            OutlandsConfig.DEATH_FIRST_APPEARANCE_DISTANCE.get(),
                            OutlandsConfig.FEAR_FULL_TERRITORY_DISTANCE.get());
                    context.getSource().sendSuccess(() -> Component.literal(String.format(
                            "The Outlands | spawn distance %.1f | tier %d | pressure %.2f | %s%s | border distance %.1f | Null eligible %s | Fear intro %s | Fear territory %s | Death intro %s | Death territory %s | Almighty territory %s",
                            s.distanceFromSpawn(), s.escalationTier(), d.pressure(), d.reason(),
                            d.eventId() == null ? "" : " (" + d.eventId() + ")",
                            borderDistance, apostles.nullMayAppear() ? "YES" : "no",
                            apostles.fearIntroductionUnlocked() ? "YES" : "no",
                            apostles.fearFullTerritoryUnlocked() ? "YES" : "no",
                            apostles.deathIntroductionUnlocked() ? "YES" : "no",
                            apostles.deathTerritoryActive() ? "YES" : "no",
                            apostles.almightyTerritoryActive() ? "YES" : "no")), false);
                    return Command.SINGLE_SUCCESS;
                })));
    }
}
