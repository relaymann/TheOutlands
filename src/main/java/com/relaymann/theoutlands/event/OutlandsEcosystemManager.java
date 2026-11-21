package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.entity.*;
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

/** Server-authoritative ecology: factions, territory and rare emergent encounters. */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class OutlandsEcosystemManager {
    private OutlandsEcosystemManager() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!OutlandsConfig.ENTITY_ECOSYSTEM_ENABLED.get()
                || event.getServer().getTickCount() % 20 != 0) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            Set<Long> processedPairs = new HashSet<>();
            for (ServerPlayer player : level.players()) {
                if (!player.isAlive() || player.isSpectator() || player.isCreative()) continue;
                var nearby = level.getEntitiesOfClass(Mob.class, new AABB(player.blockPosition()).inflate(64.0D),
                        mob -> mob.isAlive() && isOutlandsCreature(mob));
                for (int i = 0; i < nearby.size(); i++) {
                    Mob first = nearby.get(i);
                    for (int j = i + 1; j < nearby.size(); j++) {
                        Mob second = nearby.get(j);
                        if (first.distanceToSqr(second) <= 24.0D * 24.0D) {
                            int low = Math.min(first.getId(), second.getId());
                            int high = Math.max(first.getId(), second.getId());
                            long pairKey = ((long) low << 32) | (high & 0xffffffffL);
                            if (processedPairs.add(pairKey)) {
                                ServerPlayer witness = player;
                                Mob defended = first instanceof AgitatedOneEntity ? first
                                        : second instanceof AgitatedOneEntity ? second : null;
                                if (defended != null) {
                                    double nearest = Double.MAX_VALUE;
                                    for (ServerPlayer candidate : level.players()) {
                                        if (!candidate.isAlive() || candidate.isSpectator() || candidate.isCreative()) continue;
                                        double distance = candidate.distanceToSqr(defended);
                                        if (distance < nearest) { nearest = distance; witness = candidate; }
                                    }
                                }
                                interact(first, second, witness);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isOutlandsCreature(Mob mob) {
        return mob instanceof CaveJarEntity || mob instanceof StalkerEntity || mob instanceof ObserverEntity
                || mob instanceof HunterEntity || mob instanceof BurrowerEntity || mob instanceof MimicEntity
                || mob instanceof StillbornStatueEntity || mob instanceof AgitatedOneEntity
                || mob instanceof NullEntity || mob instanceof NullApostleEntity || mob instanceof FearEntity
                || mob instanceof DeathEntity || mob instanceof AlmightyEntity
                || mob instanceof LurkerEntity || mob instanceof HowlerEntity || mob instanceof DredgeEntity;
    }

    private static void interact(Mob a, Mob b, ServerPlayer witness) {
        if (a instanceof NullEntity && b instanceof NullApostleEntity) escort(a, b);
        if (b instanceof NullEntity && a instanceof NullApostleEntity) escort(b, a);

        if (a instanceof FearEntity) hesitateNearFear(b, a);
        if (b instanceof FearEntity) hesitateNearFear(a, b);

        // Death retains its assigned-player hunt. Almighty exclusion remains governed
        // by AlmightyTerritoryEvents; neither joins ordinary creature rivalries.
        if (a instanceof DeathEntity || b instanceof DeathEntity
                || a instanceof AlmightyEntity || b instanceof AlmightyEntity) return;

        // Hunter and Stalker cooperate only after one has actually acquired a player.
        if (a instanceof HunterEntity && b instanceof StalkerEntity) shareTarget(a, b);
        if (b instanceof HunterEntity && a instanceof StalkerEntity) shareTarget(b, a);

        // Howler calls create a temporary hunting alliance: nearby trackers converge
        // only on a player already acquired by the Howler, never on arbitrary mobs.
        if (a instanceof HowlerEntity && (b instanceof HunterEntity || b instanceof StalkerEntity)) shareTarget(a, b);
        if (b instanceof HowlerEntity && (a instanceof HunterEntity || a instanceof StalkerEntity)) shareTarget(b, a);

        // Lurkers are opportunists: they shadow a Howler's territory, but retreat
        // from the Dredge's low, aggressive hunting route.
        if (a instanceof HowlerEntity && b instanceof LurkerEntity) shadowHowler((LurkerEntity)b, a);
        if (b instanceof HowlerEntity && a instanceof LurkerEntity) shadowHowler((LurkerEntity)a, b);
        if (a instanceof DredgeEntity && b instanceof LurkerEntity) yieldTerritory(a, b);
        if (b instanceof DredgeEntity && a instanceof LurkerEntity) yieldTerritory(b, a);

        // Dredges and Lurkers recognize the old cave sentinels as dominant.
        if (a instanceof CaveJarEntity && (b instanceof DredgeEntity || b instanceof LurkerEntity)) yieldTerritory(a, b);
        if (b instanceof CaveJarEntity && (a instanceof DredgeEntity || a instanceof LurkerEntity)) yieldTerritory(b, a);
        if (a instanceof StillbornStatueEntity && (b instanceof DredgeEntity || b instanceof LurkerEntity)) yieldTerritory(a, b);
        if (b instanceof StillbornStatueEntity && (a instanceof DredgeEntity || a instanceof LurkerEntity)) yieldTerritory(b, a);

        // Cave.jar holds its ground; Burrowers yield instead of mob-vs-mob brawling.
        if (a instanceof CaveJarEntity && b instanceof BurrowerEntity) yieldTerritory(a, b);
        if (b instanceof CaveJarEntity && a instanceof BurrowerEntity) yieldTerritory(b, a);

        // Cave.jar dominates narrow cave routes. Hunters and unrevealed Mimics
        // avoid contesting its immediate hunting ground rather than starting
        // artificial mob-vs-mob combat.
        if (a instanceof CaveJarEntity && b instanceof HunterEntity) yieldTerritory(a, b);
        if (b instanceof CaveJarEntity && a instanceof HunterEntity) yieldTerritory(b, a);
        if (a instanceof CaveJarEntity && b instanceof MimicEntity mimic && !mimic.isRevealed()) yieldTerritory(a, b);
        if (b instanceof CaveJarEntity && a instanceof MimicEntity mimic && !mimic.isRevealed()) yieldTerritory(b, a);

        // Stillborn territory is avoided by subterranean travelers; the statue
        // remains a stationary threat instead of chasing unrelated creatures.
        if (a instanceof StillbornStatueEntity && b instanceof BurrowerEntity) yieldTerritory(a, b);
        if (b instanceof StillbornStatueEntity && a instanceof BurrowerEntity) yieldTerritory(b, a);

        // Two sentinels avoid crowding the same narrow approach.
        if (a instanceof StillbornStatueEntity && b instanceof ObserverEntity) giveSpace(a, b);
        if (b instanceof StillbornStatueEntity && a instanceof ObserverEntity) giveSpace(b, a);

        // Very rarely, an Observer shadows an active predator from a distance.
        if (a instanceof ObserverEntity && isPredator(b)) studyHunt((ObserverEntity) a, b);
        if (b instanceof ObserverEntity && isPredator(a)) studyHunt((ObserverEntity) b, a);

        // Agitated One defends its immediate territory against intruders when a
        // legitimate player is present, but never attacks other Outlands entities.
        if (a instanceof AgitatedOneEntity && isPredator(b)) defendPlayerSpace((AgitatedOneEntity)a, witness);
        if (b instanceof AgitatedOneEntity && isPredator(a)) defendPlayerSpace((AgitatedOneEntity)b, witness);

        // A rare encounter: Hunters occasionally investigate a dormant Mimic.
        if (a instanceof HunterEntity && b instanceof MimicEntity) investigateMimic(a, b);
        if (b instanceof HunterEntity && a instanceof MimicEntity) investigateMimic(b, a);
    }

    private static void escort(Mob nullEntity, Mob apostle) {
        if (apostle.getTarget() != null || apostle.distanceToSqr(nullEntity) < 64.0D || apostle.tickCount % 40 != 0) return;
        apostle.getNavigation().moveTo(nullEntity, 0.9D);
    }

    private static void hesitateNearFear(Mob creature, Mob fear) {
        if (creature.getTarget() != null || creature.tickCount % 80 != 0 || creature.distanceToSqr(fear) > 144.0D) return;
        creature.getNavigation().stop();
    }

    private static void shareTarget(Mob hunter, Mob stalker) {
        if (hunter.getTarget() instanceof Player target && target.isAlive() && !target.isCreative() && !target.isSpectator()
                && (stalker.getTarget() == null || stalker.getTarget() == target)) stalker.setTarget(target);
        else if (stalker.getTarget() instanceof Player target && target.isAlive() && !target.isCreative()
                && !target.isSpectator() && hunter.getTarget() == null) hunter.setTarget(target);
    }

    private static void yieldTerritory(Mob owner, Mob intruder) {
        if (intruder.getTarget() != null || intruder.tickCount % 40 != 0) return;
        Vec3 away = intruder.position().subtract(owner.position());
        if (away.lengthSqr() < 0.001D) away = new Vec3(1, 0, 0);
        Vec3 destination = intruder.position().add(away.normalize().scale(12.0D));
        intruder.getNavigation().moveTo(destination.x, destination.y, destination.z, 1.05D);
    }

    private static void giveSpace(Mob sentinel, Mob observer) {
        if (observer.getTarget() != null || observer.tickCount % 60 != 0) return;
        Vec3 away = observer.position().subtract(sentinel.position());
        if (away.lengthSqr() < 0.001D) away = new Vec3(0, 0, 1);
        Vec3 destination = observer.position().add(away.normalize().scale(8.0D));
        observer.getNavigation().moveTo(destination.x, destination.y, destination.z, 0.8D);
    }

    private static boolean isPredator(Mob mob) {
        return mob instanceof CaveJarEntity || mob instanceof StalkerEntity || mob instanceof HunterEntity
                || mob instanceof BurrowerEntity || mob instanceof MimicEntity || mob instanceof StillbornStatueEntity
                || mob instanceof LurkerEntity || mob instanceof DredgeEntity || mob instanceof HowlerEntity;
    }

    private static void defendPlayerSpace(AgitatedOneEntity entity, ServerPlayer witness) {
        if (witness.isAlive() && !witness.isCreative() && !witness.isSpectator()
                && entity.distanceToSqr(witness) <= 18.0D * 18.0D && entity.getTarget() == null) entity.setTarget(witness);
    }

    private static void studyHunt(ObserverEntity observer, Mob predator) {
        if (observer.getTarget() != null || predator.getTarget() == null || observer.tickCount % 200 != 0
                || observer.getRandom().nextInt(6) != 0 || observer.distanceToSqr(predator) > 18.0D * 18.0D) return;
        Vec3 direction = predator.position().subtract(observer.position());
        if (direction.lengthSqr() < 0.001D) direction = new Vec3(0, 0, 1);
        Vec3 shadowPoint = predator.position().subtract(direction.normalize().scale(6.0D));
        observer.getNavigation().moveTo(shadowPoint.x, shadowPoint.y, shadowPoint.z, 0.72D);
    }


    private static void shadowHowler(LurkerEntity lurker, Mob howler) {
        if (lurker.getTarget() != null || lurker.tickCount % 80 != 0
                || lurker.distanceToSqr(howler) < 25.0D || lurker.distanceToSqr(howler) > 24.0D * 24.0D) return;
        Vec3 offset = lurker.position().subtract(howler.position());
        if (offset.lengthSqr() < 0.001D) offset = new Vec3(1, 0, 0);
        Vec3 point = howler.position().add(offset.normalize().scale(9.0D));
        lurker.getNavigation().moveTo(point.x, point.y, point.z, 0.76D);
    }

    private static void investigateMimic(Mob hunter, Mob mimic) {
        if (hunter.getTarget() != null || mimic.getTarget() != null || hunter.tickCount % 240 != 0
                || hunter.getRandom().nextInt(8) != 0 || hunter.distanceToSqr(mimic) >= 14.0D * 14.0D) return;
        hunter.getNavigation().moveTo(mimic, 0.72D);
    }
}
