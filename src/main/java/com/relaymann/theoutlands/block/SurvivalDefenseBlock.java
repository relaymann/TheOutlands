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
import com.relaymann.theoutlands.entity.CreatureHearingEvents;

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
        if (monster.tickCount % 20 != 0) return;
        if (kind == Kind.SPIKES) {
            monster.hurt(level.damageSources().cactus(), 3.0F);
            monster.setDeltaMovement(monster.getDeltaMovement().scale(0.65D));
            level.playSound(null, pos, SoundEvents.ANVIL_HIT, SoundSource.BLOCKS, 0.28F, 0.72F);
        } else {
            level.playSound(null, pos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.8F, 0.72F);
            if (level instanceof net.minecraft.server.level.ServerLevel server) {
                CreatureHearingEvents.alarm(server, pos);
            }
            if (level instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5D, pos.getY() + 0.8D,
                        pos.getZ() + 0.5D, 5, 0.25D, 0.2D, 0.25D, 0.02D);
            }
        }
    }
}
