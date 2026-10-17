package com.darkspace.client;

import com.darkspace.difficulty.DifficultyConfig;
import com.darkspace.item.PortalItems;
import com.darkspace.network.PortalNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;

public final class DarkSpaceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DifficultyConfig.load();

        ClientPlayNetworking.registerGlobalReceiver(PortalNetworking.DestinationsPayload.ID, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(
                        new PortalDestinationScreen(payload.destinations()))));

        // Keep client GUI classes out of common/server item classes.
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient || !player.getStackInHand(hand).isOf(PortalItems.SHIP_COMPUTER)) {
                return ActionResult.PASS;
            }
            MinecraftClient.getInstance().setScreen(new ShipBlueprintScreen(hand));
            return ActionResult.SUCCESS;
        });
    }
}
