package com.relaymann.theoutlands.event;

import com.relaymann.theoutlands.OutlandsConfig;
import com.relaymann.theoutlands.horror.HorrorDecision;
import com.relaymann.theoutlands.horror.HorrorMemory;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import java.util.Set;

/** Executes all registered director events without altering player-built blocks. */
public final class HorrorEventExecutor {
    private static final Set<String> SUPPORTED_EVENTS = com.relaymann.theoutlands.horror.HorrorEventCatalog.ids();
