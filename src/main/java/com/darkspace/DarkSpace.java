package com.darkspace;

import com.darkspace.item.PortalItems;
import com.darkspace.network.PortalNetworking;\nimport com.darkspace.network.ShipBlueprintNetworking;
import com.darkspace.portal.PortalTravelService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DarkSpace implements ModInitializer {
    public static final String MOD_ID = "darkspace";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        PortalItems.initialize();
        PortalNetworking.initialize();\n        ShipBlueprintNetworking.initialize();

        // Discovery tags are saved with player data, so they persist between sessions.
        ServerTickEvents.END_WORLD_TICK.register(world ->
                world.getPlayers().forEach(PortalTravelService::recordVisit));

        LOGGER.info("DarkSpace is initializing.");
    }
}
