package com.darkspace.item;

import com.darkspace.DarkSpace;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class PortalItems {
    public static final Item PLANETARY_PORTAL_GUN = register("planetary_portal_gun", PlanetaryPortalGunItem::new);
    public static final Item PORTAL_CELL = register("portal_cell", Item::new);
    public static final Item REFINED_PORTAL_CELL = register("refined_portal_cell", Item::new);
    public static final Item QUANTUM_PORTAL_CELL = register("quantum_portal_cell", Item::new);
    public static final Item RADIANT_PILLAR_SHIP = register("radiant_pillar_ship", RadiantPillarShipItem::new);

    public static final ItemGroup PORTAL_ITEM_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of(DarkSpace.MOD_ID, "portal_items"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(PLANETARY_PORTAL_GUN))
                    .displayName(Text.translatable("itemGroup.darkspace.portal_items"))
                    .entries((context, entries) -> {
                        entries.add(PLANETARY_PORTAL_GUN);
                        entries.add(PORTAL_CELL);
                        entries.add(REFINED_PORTAL_CELL);
                        entries.add(QUANTUM_PORTAL_CELL);
                        entries.add(RADIANT_PILLAR_SHIP);
                    })
                    .build()
    );

    private PortalItems() {}

    private static Item register(String name, java.util.function.Function<Item.Settings, Item> factory) {
        Identifier id = Identifier.of(DarkSpace.MOD_ID, name);
        Item.Settings settings = new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, id));
        return Registry.register(Registries.ITEM, id, factory.apply(settings));
    }

    public static void initialize() {
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(entries -> {
                    entries.add(PLANETARY_PORTAL_GUN);
                    entries.add(RADIANT_PILLAR_SHIP);
                });
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register(entries -> {
                    entries.add(PORTAL_CELL);
                    entries.add(REFINED_PORTAL_CELL);
                    entries.add(QUANTUM_PORTAL_CELL);
                });
        DarkSpace.LOGGER.info("Registered DarkSpace portal items and creative tab.");
    }

    public static int fuelTier(Item item) {
        if (item == QUANTUM_PORTAL_CELL) return 3;
        if (item == REFINED_PORTAL_CELL) return 2;
        if (item == PORTAL_CELL) return 1;
        return 0;
    }
}
