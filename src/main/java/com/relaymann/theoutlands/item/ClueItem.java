package com.relaymann.theoutlands.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

public final class ClueItem extends Item {
    private final String clueKey;

    public ClueItem(Properties properties, String clueKey) {
        super(properties);
        this.clueKey = clueKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(clueKey));
    }
}
