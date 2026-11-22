package com.relaymann.theoutlands.client;

import com.relaymann.theoutlands.NightmareMode;
import com.relaymann.theoutlands.OutlandsGameRules;
import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.Map;
import java.util.WeakHashMap;

/** Adds a persistent NIGHTMARE selector to the vanilla Create World screen. */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class NightmareModeSelection {
    private static final Map<WorldCreationUiState, Selection> SELECTIONS = new WeakHashMap<>();

    private NightmareModeSelection() {}

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof CreateWorldScreen screen)) return;
        WorldCreationUiState state = screen.getUiState();
        Selection selection = SELECTIONS.computeIfAbsent(state, ignored -> new Selection());

        if (!selection.listenerInstalled) {
            selection.listenerInstalled = true;
            state.addListener(changed -> enforceSelection(changed, selection));
        }

        Button button = Button.builder(label(selection.nightmare),
                clicked -> toggle(state, selection))
                .bounds(Math.max(8, screen.width - 142), 8, 134, 20)
                .build();
        selection.button = button;
        event.addListener(button);
        updateButton(selection);
    }

    private static void toggle(WorldCreationUiState state, Selection selection) {
        if (!selection.nightmare) {
            selection.previousMode = state.getGameMode();
            selection.previousDifficulty = state.getDifficulty();
            selection.previousCommands = state.isAllowCommands();
            selection.nightmare = true;
            applyNightmare(state, selection);
        } else {
            selection.nightmare = false;
            state.getGameRules().getRule(OutlandsGameRules.NIGHTMARE_MODE).set(false, null);
            state.setGameMode(selection.previousMode == null
                    ? WorldCreationUiState.SelectedGameMode.SURVIVAL : selection.previousMode);
            if (selection.previousDifficulty != null) state.setDifficulty(selection.previousDifficulty);
            state.setAllowCommands(selection.previousCommands);
        }
        updateButton(selection);
    }

    private static void enforceSelection(WorldCreationUiState state, Selection selection) {
        if (selection.nightmare && !selection.locking) applyNightmare(state, selection);
        updateButton(selection);
    }

    private static void applyNightmare(WorldCreationUiState state, Selection selection) {
        selection.locking = true;
        state.setGameMode(WorldCreationUiState.SelectedGameMode.HARDCORE);
        state.setDifficulty(Difficulty.HARD);
        state.setAllowCommands(false);
        state.getGameRules().getRule(OutlandsGameRules.NIGHTMARE_MODE).set(true, null);
        selection.locking = false;
    }

    private static Component label(boolean enabled) {
        return Component.literal(enabled ? "NIGHTMARE: ON" : "NIGHTMARE: OFF");
    }

    private static void updateButton(Selection selection) {
        if (selection.button != null) selection.button.setMessage(label(selection.nightmare));
    }

    private static final class Selection {
        private boolean nightmare;
        private boolean locking;
        private boolean listenerInstalled;
        private boolean previousCommands;
        private WorldCreationUiState.SelectedGameMode previousMode;
        private Difficulty previousDifficulty;
        private Button button;
    }
}
