package com.relaymann.theoutlands.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.relaymann.theoutlands.event.OutlandsIntelligenceManager;

/** Simple server-authoritative trap/alarm block with per-entity tick throttling. */
public final class SurvivalDefenseBlock extends Block {
    public enum Kind { SPIKES, ALARM }
    private final Kind kind;

    public SurvivalDefenseBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (level.isClientSide || !(entity instanceof Monster monster) || !monster.isAlive()) return;
        if (!(level instanceof net.minecraft.server.level.ServerLevel server)) return;
        String cooldownKey = "theoutlands_" + kind.name().toLowerCase(java.util.Locale.ROOT)
                + "_trap_" + Long.toUnsignedString(pos.asLong());
        var persistent = monster.getPersistentData();
        boolean hasPrevious = persistent.contains(cooldownKey);
        long previous = persistent.getLong(cooldownKey);
        long now = server.getGameTime();
        if (!SurvivalDefensePolicy.isReady(hasPrevious, now, previous, 20L)) return;
        persistent.putLong(cooldownKey, now);
        if (kind == Kind.SPIKES) {
            monster.hurt(level.damageSources().cactus(), 3.0F);
            monster.setDeltaMovement(monster.getDeltaMovement().scale(0.65D));
            level.playSound(null, pos, SoundEvents.ANVIL_HIT, SoundSource.BLOCKS, 0.28F, 0.72F);
        } else {
            level.playSound(null, pos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.8F, 0.72F);
            if (level instanceof net.minecraft.server.level.ServerLevel server) {
                OutlandsIntelligenceManager.recordAlarm(server, pos);
                server.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5D, pos.getY() + 0.8D,
                        pos.getZ() + 0.5D, 5, 0.25D, 0.2D, 0.25D, 0.02D);
            }
        }
    }
}
