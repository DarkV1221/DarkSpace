package com.darkspace.item;

import com.darkspace.DarkSpace;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class PortalItems {
    public static final Item PLANETARY_PORTAL_GUN = register("planetary_portal_gun", new PlanetaryPortalGunItem(new Item.Settings().maxCount(1)));
    public static final Item PORTAL_CELL = register("portal_cell", new Item(new Item.Settings()));
    public static final Item REFINED_PORTAL_CELL = register("refined_portal_cell", new Item(new Item.Settings()));
    public static final Item QUANTUM_PORTAL_CELL = register("quantum_portal_cell", new Item(new Item.Settings()));

    private PortalItems() {}

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(DarkSpace.MOD_ID, name), item);
    }

    public static void initialize() {
        DarkSpace.LOGGER.info("Registering DarkSpace portal items.");
    }

    public static int fuelTier(Item item) {
        if (item == QUANTUM_PORTAL_CELL) return 3;
        if (item == REFINED_PORTAL_CELL) return 2;
        if (item == PORTAL_CELL) return 1;
        return 0;
    }
}