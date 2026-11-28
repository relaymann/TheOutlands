package com.relaymann.theoutlands.item;

import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import java.util.List;

public final class InvestigationToolItem extends Item {
    public enum Mode { MOTION, EMF, CAMERA, THERMAL, RADIO, ACOUSTIC, ALARM }
    private final Mode mode;
    public InvestigationToolItem(Mode mode, Properties properties) { super(properties); this.mode = mode; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && level instanceof ServerLevel server) {
            int cooldown = switch (mode) {
                case MOTION, EMF -> 50; case CAMERA -> 35; case THERMAL -> 70;
                case RADIO, ACOUSTIC -> 90; case ALARM -> 120;
            };
            player.getCooldowns().addCooldown(this, cooldown);
            scan(server, player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void scan(ServerLevel level, Player player) {
        double range = switch (mode) {
            case MOTION -> 22.0D; case EMF -> 14.0D; case CAMERA -> 26.0D;
            case THERMAL -> 20.0D; case RADIO -> 30.0D; case ACOUSTIC -> 34.0D; case ALARM -> 12.0D;
        };
        List<Mob> nearby = level.getEntitiesOfClass(Mob.class, new AABB(player.blockPosition()).inflate(range),
                mob -> mob.isAlive() && mob.getType().builtInRegistryHolder().unwrapKey()
                        .map(key -> key.location().getNamespace().equals(TheOutlands.MOD_ID)).orElse(false));
        if (mode == Mode.MOTION) nearby = nearby.stream()
                .filter(mob -> mob.getDeltaMovement().lengthSqr() > 0.0015D).toList();
        float interference = 0.10F;
        if (level.isRainingAt(player.blockPosition())) interference += 0.10F;
        if (player.getY() < 0.0D) interference += 0.12F;
        if (player.getY() < -32.0D) interference += 0.08F;
        if (player.getRandom().nextFloat() < interference) {
            BlockPos falseSignal = player.blockPosition().offset(player.getRandom().nextInt(13)-6,
                    player.getRandom().nextInt(5)-2, player.getRandom().nextInt(13)-6);
            emit(level, falseSignal, ParticleTypes.ELECTRIC_SPARK, 2);
            return;
        }
        if (nearby.isEmpty()) {
            if (player.getRandom().nextInt(7) == 0) {
                BlockPos faint = player.blockPosition().offset(player.getRandom().nextInt(17)-8,
                        player.getRandom().nextInt(5)-2, player.getRandom().nextInt(17)-8);
                emit(level, faint, particle(), 1);
            }
            return;
        }
        List<Mob> targets = new java.util.ArrayList<>(nearby);
        int limit = Math.min(3, targets.size());
        for (int i = 0; i < limit; i++) {
            int selected = i + player.getRandom().nextInt(targets.size() - i);
            Mob mob = targets.get(selected);
            targets.set(selected, targets.get(i));
            targets.set(i, mob);
            emit(level, mob.blockPosition(), particle(), mode == Mode.CAMERA ? 5 : 2);
        }
        if (mode == Mode.RADIO || mode == Mode.ACOUSTIC || mode == Mode.ALARM)
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS, 0.12F, 0.55F + player.getRandom().nextFloat() * 0.35F);
    }

    private ParticleOptions particle() {
        return switch (mode) {
            case MOTION -> ParticleTypes.ELECTRIC_SPARK; case EMF -> ParticleTypes.SCULK_SOUL;
            case CAMERA -> ParticleTypes.FLASH; case THERMAL -> ParticleTypes.FLAME;
            case RADIO -> ParticleTypes.NOTE; case ACOUSTIC -> ParticleTypes.SMOKE;
            case ALARM -> ParticleTypes.CRIT;
        };
    }
    private static void emit(ServerLevel level, BlockPos pos, ParticleOptions particle, int count) {
        level.sendParticles(particle, pos.getX()+0.5D, pos.getY()+0.8D, pos.getZ()+0.5D,
                count, 0.22D, 0.35D, 0.22D, 0.015D);
    }
}
