package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.horror.AlmightyTerritory;
import com.relaymann.theoutlands.item.OutlandsItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Enforces the Almighty's exclusive endgame territory.
 *
 * <p>Players may enter because they are the story's intended visitors. The Almighty
 * is identified by a dedicated entity tag so its future implementation can opt in
 * without coupling this rule to an entity class that does not exist yet. Every other
 * entity is rejected on entry and removed if it crosses into the region later.</p>
 */
@EventBusSubscriber(modid = TheOutlands.MOD_ID)
public final class AlmightyTerritoryEvents {
    public static final String ALMIGHTY_ENTITY_TAG = "theoutlands_almighty";

    private AlmightyTerritoryEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !level.dimension().equals(Level.OVERWORLD)) return;
        Entity entity = event.getEntity();
        if (isExempt(entity)) return;
        if (AlmightyTerritory.contains(level, entity.getX(), entity.getZ())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)
                || !level.dimension().equals(Level.OVERWORLD)
                || isExempt(entity)) return;
        if (AlmightyTerritory.contains(level, entity.getX(), entity.getZ())) {
            entity.discard();
        }
    }

    private static boolean isExempt(Entity entity) {
        if (entity instanceof Player || entity.getTags().contains(ALMIGHTY_ENTITY_TAG)) return true;
        if (entity instanceof ItemEntity item) {
            return item.getItem().is(OutlandsItems.DEATH_JAR.get())
                    || item.getItem().is(OutlandsItems.DEATH_MANIFEST.get())
                    || item.getItem().is(OutlandsItems.ALMIGHTY_FRAGMENT.get())
                    || item.getItem().is(OutlandsItems.THE_LAST_PAGE.get());
        }
        return false;
    }
}
