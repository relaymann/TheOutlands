package com.relaymann.theoutlands.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class SurvivalToolItem extends Item {
    public SurvivalToolItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && level instanceof ServerLevel server) {
            player.getCooldowns().addCooldown(this, 20 * 45);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            server.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.2D,
                    player.getZ(), 22, 0.22D, 0.35D, 0.22D, 0.025D);
            server.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0D,
                    player.getZ(), 12, 0.3D, 0.25D, 0.3D, 0.012D);
            server.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE,
                    SoundSource.PLAYERS, 0.55F, 0.82F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
