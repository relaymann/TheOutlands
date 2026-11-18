package com.relaymann.theoutlands.item;

import com.relaymann.theoutlands.TheOutlands;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class OutlandsItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, TheOutlands.MOD_ID);

    public static final DeferredHolder<Item, Item> DEATH_JAR = ITEMS.register("death_jar",
            () -> new ClueItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON),
                    "item.theoutlands.death_jar.lore"));
    public static final DeferredHolder<Item, Item> DEATH_MANIFEST = ITEMS.register("death_manifest",
            () -> new ClueItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    "item.theoutlands.death_manifest.lore"));
    public static final DeferredHolder<Item, Item> ALMIGHTY_FRAGMENT = ITEMS.register("almighty_fragment",
            () -> new ClueItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC),
                    "item.theoutlands.almighty_fragment.lore"));

    private OutlandsItems() {}
}
