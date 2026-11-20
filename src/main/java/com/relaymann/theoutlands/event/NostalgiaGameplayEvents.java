package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/**
 * Restores the pre-shears sheep interaction while retaining modern shears.
 * A successful player attack plucks a living sheep once; vanilla grass-eating
 * behavior remains responsible for regrowing its fleece.
 */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class NostalgiaGameplayEvents {
    private NostalgiaGameplayEvents() {}

    @SubscribeEvent
    public static void onPlayerAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || !(event.getTarget() instanceof Sheep sheep)
                || !(player.level() instanceof ServerLevel level)
                || !sheep.isAlive() || sheep.isSheared()
                || player.isCreative() || player.isSpectator()) {
            return;
        }

        // Use the sheep's actual fleece colour, including naturally spawned
        // brown, black, gray, light-gray, and pink variants.
        ResourceLocation woolId = ResourceLocation.withDefaultNamespace(
                sheep.getColor().getName() + "_wool");
        var woolItem = BuiltInRegistries.ITEM.get(woolId);
        if (woolItem == null || woolItem == net.minecraft.world.item.Items.AIR) return;

        sheep.setSheared(true);
        int count = 1 + level.getRandom().nextInt(3);
        ItemEntity drop = new ItemEntity(level, sheep.getX(), sheep.getY() + 0.55D,
                sheep.getZ(), new ItemStack(woolItem, count));
        drop.setDefaultPickUpDelay();
        drop.setDeltaMovement((level.getRandom().nextDouble() - 0.5D) * 0.16D,
                0.18D, (level.getRandom().nextDouble() - 0.5D) * 0.16D);
        level.addFreshEntity(drop);
    }
}
