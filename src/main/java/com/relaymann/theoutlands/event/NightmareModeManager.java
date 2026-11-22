package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.NightmareMode;
import com.relaymann.theoutlands.OutlandsGameRules;
import com.relaymann.theoutlands.TheOutlands;
import com.relaymann.theoutlands.entity.OutlandsEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NIGHTMARE-only encounter director. Events are diegetic: no chat narration,
 * forced movement, permanent terrain edits, or chunk-forcing spawn attempts.
 */
@EventBusSubscriber(modid = TheOutlands.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class NightmareModeManager {
    private static final Map<UUID, Long> NEXT_EVENT = new ConcurrentHashMap<>();
    private static final Map<ServerLevel, Long> BLOOD_MOON_UNTIL = new ConcurrentHashMap<>();
    private static final List<net.minecraft.world.entity.EntityType<? extends Mob>> THREATS = List.of(
            OutlandsEntities.STALKER.get(), OutlandsEntities.HUNTER.get(),
            OutlandsEntities.LURKER.get(), OutlandsEntities.DREDGE.get(),
            OutlandsEntities.MIMIC.get(), OutlandsEntities.OBSERVER.get(),
            OutlandsEntities.HOWLER.get(), OutlandsEntities.PILGRIM.get(),
            OutlandsEntities.BURROWER.get(), OutlandsEntities.CAVE_JAR.get(),
            OutlandsEntities.CEILING_DWELLER.get());

    private NightmareModeManager() {}

    public static boolean isBloodMoonActive(ServerLevel level) {
        return BLOOD_MOON_UNTIL.getOrDefault(level, 0L) > level.getGameTime();
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) NightmareMode.refresh(level);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 20 != 0) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            NightmareMode.refresh(level);
            if (!level.dimension().equals(Level.OVERWORLD) || !NightmareMode.isEnabled(level)) continue;
            for (ServerPlayer player : level.players()) {
                if (!eligible(player)) continue;
                long now = level.getGameTime();
                Long next = NEXT_EVENT.get(player.getUUID());
                if (next == null) {
                    NEXT_EVENT.put(player.getUUID(), now + 1_200L + player.getRandom().nextInt(1_801));
                    continue;
                }
                if (now < next) continue;
                triggerEvent(player);
                NEXT_EVENT.put(player.getUUID(), now + 2_400L + player.getRandom().nextInt(4_801));
            }
        }
    }

    private static boolean eligible(ServerPlayer player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator()
                && !player.isSleeping() && !player.isPassenger() && !player.isInWaterOrBubble();
    }

    private static void triggerEvent(ServerPlayer player) {
        switch (player.getRandom().nextInt(4)) {
            case 0 -> bloodMoon(player);
            case 1 -> silence(player);
            case 2 -> unusualActivity(player);
            default -> containmentFailure(player);
        }
    }

    /** A two-minute storm window also raises the natural encounter budget. */
    private static void bloodMoon(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.setWeatherParameters(0, 2_400, true, true);
        BLOOD_MOON_UNTIL.put(level, level.getGameTime() + 2_400L);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 3.0D,
                player.getZ(), 28, 2.2D, 1.2D, 2.2D, 0.015D);
        play(player, SoundEvents.WARDEN_ROAR, SoundSource.AMBIENT, 0.28F, 0.42F);
        spawnThreats(player, 1, true);
    }

    /** Briefly cuts ambient audio, then leaves one distant sign behind. */
    private static void silence(ServerPlayer player) {
        player.connection.send(new ClientboundStopSoundPacket(Optional.empty(), Optional.of(SoundSource.AMBIENT)));
        play(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.07F, 0.35F);
        player.serverLevel().sendParticles(ParticleTypes.ASH, player.getX(), player.getY() + 1.0D,
                player.getZ(), 12, 0.7D, 0.8D, 0.7D, 0.002D);
    }

    /** A small hunting party appears at the edge of already-loaded terrain. */
    private static void unusualActivity(ServerPlayer player) {
        play(player, SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.24F, 0.55F);
        spawnThreats(player, 2, true);
    }

    /** A much more dangerous group encounter, exclusive to NIGHTMARE. */
    private static void containmentFailure(ServerPlayer player) {
        play(player, SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 0.34F, 0.62F);
        player.serverLevel().sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0D,
                player.getZ(), 18, 1.1D, 0.7D, 1.1D, 0.01D);
        spawnThreats(player, 4, true);
    }

    private static void spawnThreats(ServerPlayer player, int count, boolean empowered) {
        ServerLevel level = player.serverLevel();
        for (int i = 0; i < count; i++) {
            double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
            double distance = 22.0D + player.getRandom().nextDouble() * 15.0D;
            int x = net.minecraft.util.Mth.floor(player.getX() + Math.cos(angle) * distance);
            int z = net.minecraft.util.Mth.floor(player.getZ() + Math.sin(angle) * distance);
            BlockPos column = new BlockPos(x, net.minecraft.util.Mth.floor(player.getY()), z);
            if (!level.hasChunkAt(column)) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos spawn = new BlockPos(x, y, z);
            if (!level.hasChunkAt(spawn)) continue;

            var type = THREATS.get(player.getRandom().nextInt(THREATS.size()));
            Mob mob = type.create(level);
            if (mob == null) continue;
            mob.moveTo(x + 0.5D, y, z + 0.5D, player.getRandom().nextFloat() * 360.0F, 0.0F);
            if (!level.noCollision(mob, mob.getBoundingBox())) {
                mob.discard();
                continue;
            }
            if (empowered) empower(mob);
            mob.addTag("theoutlands_nightmare_spawn");
            mob.setPersistenceRequired();
            mob.setTarget(player);
            if (!level.addFreshEntity(mob)) mob.discard();
        }
    }

    /** NIGHTMARE event-spawned creatures are faster and tougher, not merely more numerous. */
    private static void empower(Mob mob) {
        AttributeInstance speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(Math.min(0.65D, speed.getBaseValue() * 1.18D));
        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(health.getBaseValue() * 1.15D);
            mob.setHealth(mob.getMaxHealth());
        }
        AttributeInstance damage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) damage.setBaseValue(damage.getBaseValue() * 1.15D);
    }

    private static void play(ServerPlayer player, SoundEvent sound, SoundSource source,
                             float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), source,
                player.getX(), player.getY(), player.getZ(), volume, pitch,
                player.getRandom().nextLong()));
    }

    @SubscribeEvent
    public static void onGameModeChange(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !NightmareMode.isEnabled(player.serverLevel())) return;
        if (player.isAlive() && event.getNewGameMode() != net.minecraft.world.level.GameType.SURVIVAL) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        NEXT_EVENT.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        NEXT_EVENT.clear();
        BLOOD_MOON_UNTIL.clear();
        NightmareMode.clear();
    }
}
