package com.relaymann.theoutlands;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Main entry point for The Outlands.
 *
 * <p>Keep this class focused on bootstrap and registration. Gameplay systems live in
 * their own packages so the mod can grow without becoming monolithic.</p>
 */
@Mod(TheOutlands.MOD_ID)
public final class TheOutlands {
    public static final String MOD_ID = "theoutlands";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheOutlands(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("The Outlands is loading. The world is still ordinary.");
    }
}
