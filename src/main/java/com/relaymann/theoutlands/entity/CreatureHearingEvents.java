package com.relaymann.theoutlands.entity;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.block.OutlandsBlocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Converts ordinary player actions into short-lived stimuli for creature hearing. */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class CreatureHearingEvents {
    private CreatureHearingEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level) || !player.isAlive()
                || player.isSpectator() || player.isCreative() || player.isPassenger()) return;
        if (player.tickCount % 4 != 0 || !player.onGround()
                || player.getDeltaMovement().horizontalDistanceSqr() < 0.0016D) return;
        if (player.isCrouching()) return;
        boolean sprinting = player.isSprinting();
        CreatureNoise.emit(level, player.position(), player,
                sprinting ? CreatureNoise.Kind.SPRINT : CreatureNoise.Kind.FOOTSTEP,
                sprinting ? 0.88F : 0.48F);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer().level() instanceof ServerLevel level)) return;
        CreatureNoise.emit(level, event.getPos().getCenter(), event.getPlayer(),
                CreatureNoise.Kind.BLOCK_BREAK, 0.92F);
    }

    @SubscribeEvent
    public static void onDoorUse(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level) || !player.isAlive()) return;
        if (level.getBlockState(event.getPos()).is(BlockTags.DOORS)) {
            CreatureNoise.emit(level, event.getPos().getCenter(), player, CreatureNoise.Kind.DOOR, 0.78F);
        }
    }

    public static void alarm(ServerLevel level, net.minecraft.core.BlockPos pos) {
        CreatureNoise.emit(level, pos.getCenter(), null, CreatureNoise.Kind.ALARM, 1.0F);
    }
}
