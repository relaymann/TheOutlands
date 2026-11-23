package com.relaymann.theoutlands.item;

import com.relaymann.theoutlands.NightmareMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;

/** A durable shield whose binding only functions in NIGHTMARE worlds. */
public final class NightmareWardItem extends ShieldItem {
    public NightmareWardItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!NightmareMode.isEnabled(level)) return InteractionResultHolder.fail(stack);
        return super.use(level, player, hand);
    }
}
