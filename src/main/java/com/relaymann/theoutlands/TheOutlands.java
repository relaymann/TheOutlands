package com.relaymann.theoutlands;

import com.mojang.logging.LogUtils;
import com.relaymann.theoutlands.entity.OutlandsEntities;
import com.relaymann.theoutlands.block.OutlandsBlocks;
import com.relaymann.theoutlands.item.OutlandsItems;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(TheOutlands.MOD_ID)
public final class TheOutlands {
    public static final String MOD_ID = "theoutlands";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheOutlands(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, OutlandsConfig.SPEC);
        OutlandsGameRules.register();
        OutlandsEntities.ENTITY_TYPES.register(modEventBus);
        OutlandsBlocks.BLOCKS.register(modEventBus);
        OutlandsItems.ITEMS.register(modEventBus);
        OutlandsSounds.SOUNDS.register(modEventBus);
        com.relaymann.theoutlands.worldgen.OutlandsWorldgen.STRUCTURE_TYPES.register(modEventBus);
        com.relaymann.theoutlands.worldgen.OutlandsWorldgen.STRUCTURE_PIECES.register(modEventBus);
        LOGGER.info("The Outlands is loading. The world is still ordinary.");
    }
}
