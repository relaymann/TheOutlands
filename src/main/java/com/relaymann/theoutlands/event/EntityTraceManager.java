package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Short-lived physical evidence cues near active predators. Particles are
 * deliberately sparse and never alter blocks, doors, player builds or terrain.
 */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class EntityTraceManager {
    private static final Map<UUID, Long> NEXT_TRACE = new HashMap<>();
    private EntityTraceManager() {}

    public static void tick(ServerLevel level) {
        if (level.getGameTime() % 40L != 0L) return;
        for (ServerPlayer player : level.players()) {
            if (!player.isAlive() || player.isCreative() || player.isSpectator()
                    || player.isSleeping() || player.isPassenger()) continue;
            var predators = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(34.0D),
                    mob -> mob.isAlive() && mob.getType().builtInRegistryHolder().unwrapKey()
                            .map(key -> key.location().getNamespace().equals(TheOutlands.MOD_ID)).orElse(false));
            if (predators.isEmpty()) continue;
            long now = level.getGameTime();
            if (now < NEXT_TRACE.getOrDefault(player.getUUID(), 0L)
                    || player.getRandom().nextInt(18) != 0) continue;

            Mob source = predators.get(player.getRandom().nextInt(predators.size()));
            BlockPos evidence = source.blockPosition();
            // A rare scrape against a nearby door gives the impression of claw contact.
            boolean emitted = false;
            for (int i = 0; i < 12; i++) {
                BlockPos candidate = player.blockPosition().offset(
                        player.getRandom().nextInt(17) - 8,
                        player.getRandom().nextInt(5) - 2,
                        player.getRandom().nextInt(17) - 8);
                var state = level.getBlockState(candidate);
                if (!(state.getBlock() instanceof DoorBlock) && !(state.getBlock() instanceof TrapDoorBlock)) continue;
                if (net.minecraft.world.phys.Vec3.atCenterOf(candidate).distanceToSqr(source.position()) > 14.0D * 14.0D) continue;
                level.sendParticles(ParticleTypes.CRIT, candidate.getX() + 0.5D,
                        candidate.getY() + 0.9D, candidate.getZ() + 0.5D,
                        3, 0.16D, 0.12D, 0.16D, 0.015D);
                emitted = true;
                break;
            }
            // Otherwise, a tiny burst of block dust suggests disturbed debris near its route.
            if (!emitted) {
                BlockPos ground = evidence.below();
                var state = level.getBlockState(ground);
                if (!state.isAir() && state.getRenderShape() != net.minecraft.world.level.block.RenderShape.INVISIBLE) {
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                            evidence.getX() + 0.5D, evidence.getY() + 0.08D, evidence.getZ() + 0.5D,
                            4, 0.22D, 0.025D, 0.22D, 0.025D);
                }
            }
            NEXT_TRACE.put(player.getUUID(), now + 1_200L + player.getRandom().nextInt(1_201));
        }
    }

    public static void clear(ServerPlayer player) { NEXT_TRACE.remove(player.getUUID()); }

    public static void clear() { NEXT_TRACE.clear(); }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) { clear(); }
}
