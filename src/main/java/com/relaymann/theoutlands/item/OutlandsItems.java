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

    public static final DeferredHolder<Item, Item> THE_LAST_PAGE = ITEMS.register("the_last_page",
            () -> new ClueItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC),
                    "item.theoutlands.the_last_page.lore"));


    public static final DeferredHolder<Item, Item> MOTION_DETECTOR = ITEMS.register("motion_detector",
            () -> new InvestigationToolItem(InvestigationToolItem.Mode.MOTION, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> EMF_RECEIVER = ITEMS.register("emf_receiver",
            () -> new InvestigationToolItem(InvestigationToolItem.Mode.EMF, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> FIELD_CAMERA = ITEMS.register("field_camera",
            () -> new InvestigationToolItem(InvestigationToolItem.Mode.CAMERA, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> THERMAL_VIEWER = ITEMS.register("thermal_viewer",
            () -> new InvestigationToolItem(InvestigationToolItem.Mode.THERMAL, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> SHORTWAVE_RADIO = ITEMS.register("shortwave_radio",
            () -> new InvestigationToolItem(InvestigationToolItem.Mode.RADIO, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> ACOUSTIC_RECORDER = ITEMS.register("acoustic_recorder",
            () -> new InvestigationToolItem(InvestigationToolItem.Mode.ACOUSTIC, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> TRIPWIRE_ALARM = ITEMS.register("tripwire_alarm",
            () -> new InvestigationToolItem(InvestigationToolItem.Mode.ALARM, new Item.Properties().stacksTo(1)));

    private OutlandsItems() {}
}
