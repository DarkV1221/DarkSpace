package com.darkspace.client;

import com.darkspace.difficulty.DifficultyConfig;\nimport com.darkspace.item.PortalItems;\nimport net.fabricmc.fabric.api.event.player.UseItemCallback;\nimport net.minecraft.util.TypedActionResult;\nimport net.minecraft.client.MinecraftClient;
import com.darkspace.network.PortalNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class DarkSpaceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DifficultyConfig.load();\n\n        UseItemCallback.EVENT.register((player, world, hand) -> {\n            if (player.getStackInHand(hand).isOf(PortalItems.SHIP_COMPUTER)) {\n                MinecraftClient.getInstance().setScreen(new ShipBlueprintScreen());\n                return TypedActionResult.success(player.getStackInHand(hand));\n            }\n            return TypedActionResult.pass(player.getStackInHand(hand));\n        });

        ClientPlayNetworking.registerGlobalReceiver(PortalNetworking.DestinationsPayload.ID, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(
                        new PortalDestinationScreen(payload.destinations()))));
    }
}
