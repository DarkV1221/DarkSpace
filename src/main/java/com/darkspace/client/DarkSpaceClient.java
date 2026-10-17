package com.darkspace.client;

import com.darkspace.difficulty.DifficultyConfig;
import com.darkspace.network.PortalNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class DarkSpaceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DifficultyConfig.load();

        ClientPlayNetworking.registerGlobalReceiver(PortalNetworking.DestinationsPayload.ID, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(
                        new PortalDestinationScreen(payload.destinations()))));
    }
}
