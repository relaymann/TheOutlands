package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.HowlerEntity;
import com.relaymann.theoutlands.entity.LurkerEntity;
import com.relaymann.theoutlands.sound.OutlandsSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Adds low-frequency, entity-specific decisions on top of each creature's own
 * goals. It never assigns arbitrary targets, teleports mobs, or changes blocks.
 */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class EntityBehaviorDirector {
    private static final Set<UUID> PROCESSED = new HashSet<>();

    private EntityBehaviorDirector() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!OutlandsConfig.ENTITY_INTELLIGENCE_ENABLED.get()
                || event.getServer().getTickCount() % 10 != 0) return;
        PROCESSED.clear();
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ServerPlayer observer : level.players()) {
                if (!eligible(observer)) continue;
                for (Mob mob : level.getEntitiesOfClass(Mob.class,
                        new AABB(observer.blockPosition()).inflate(40.0D),
                        candidate -> candidate.isAlive()
                                && (candidate instanceof LurkerEntity || candidate instanceof HowlerEntity))) {
                    if (!PROCESSED.add(mob.getUUID())) continue;
                    if (mob instanceof LurkerEntity lurker) stalkFromCover(lurker);
                    if (mob instanceof HowlerEntity howler) coordinateCall(howler);
                }
            }
        }
    }

    private static void stalkFromCover(LurkerEntity lurker) {
        if (!(lurker.getTarget() instanceof Player target) || !eligible(target)
                || !lurker.hasLineOfSight(target) || lurker.tickCount % 60 != 0
                || !lurker.getNavigation().isDone()) return;
        double distance = lurker.distanceToSqr(target);
        if (distance < 64.0D || distance > 625.0D) return;

        // Move laterally and slightly behind the player's facing direction.
        // The Lurker keeps a real path and can be spotted during the maneuver.
        double yaw = Math.toRadians(target.getYRot());
        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);
        double side = lurker.getRandom().nextBoolean() ? 1.0D : -1.0D;
        double x = target.getX() + forwardX * 4.0D - forwardZ * side * 7.0D;
        double z = target.getZ() + forwardZ * 4.0D + forwardX * side * 7.0D;
        lurker.getNavigation().moveTo(x, target.getY(), z, 0.88D);
    }

    private static void coordinateCall(HowlerEntity howler) {
        if (!(howler.getTarget() instanceof Player target) || !eligible(target)
                || howler.hasLineOfSight(target) || howler.tickCount % 200 != 0
                || howler.distanceToSqr(target) > 1_296.0D
                || !OutlandsConfig.HORROR_AUDIO_ENABLED.get()) return;
        // This is an audible territorial call, not a forced spawn. The existing
        // ecosystem rules decide whether nearby Hunters or Stalkers respond.
        howler.playSound(OutlandsSounds.HOWLER_CALL.get(), 0.72F,
                0.88F + howler.getRandom().nextFloat() * 0.18F);
    }

    private static boolean eligible(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }
}
