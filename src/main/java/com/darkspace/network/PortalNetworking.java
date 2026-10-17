package com.darkspace.network;

import com.darkspace.DarkSpace;
import com.darkspace.item.PortalItems;
import com.darkspace.portal.PlanetDestination;
import com.darkspace.portal.PortalTravelService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public final class PortalNetworking {
    private PortalNetworking() {}

    public record DestinationsPayload(String destinations) implements CustomPayload {
        public static final Id<DestinationsPayload> ID = new Id<>(Identifier.of(DarkSpace.MOD_ID, "portal_destinations"));
        public static final PacketCodec<RegistryByteBuf, DestinationsPayload> CODEC =
                PacketCodec.tuple(PacketCodecs.STRING, DestinationsPayload::destinations, DestinationsPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record TravelRequestPayload(String destination) implements CustomPayload {
        public static final Id<TravelRequestPayload> ID = new Id<>(Identifier.of(DarkSpace.MOD_ID, "portal_travel"));
        public static final PacketCodec<RegistryByteBuf, TravelRequestPayload> CODEC =
                PacketCodec.tuple(PacketCodecs.STRING, TravelRequestPayload::destination, TravelRequestPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record BlueprintSavePayload(boolean offHand, String blueprint) implements CustomPayload {
        public static final Id<BlueprintSavePayload> ID = new Id<>(Identifier.of(DarkSpace.MOD_ID, "blueprint_save"));
        public static final PacketCodec<RegistryByteBuf, BlueprintSavePayload> CODEC =
                PacketCodec.tuple(PacketCodecs.BOOLEAN, BlueprintSavePayload::offHand,
                        PacketCodecs.STRING, BlueprintSavePayload::blueprint, BlueprintSavePayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(DestinationsPayload.ID, DestinationsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TravelRequestPayload.ID, TravelRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BlueprintSavePayload.ID, BlueprintSavePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TravelRequestPayload.ID, (payload, context) ->
                PortalTravelService.travel(context.player(), payload.destination()));

        ServerPlayNetworking.registerGlobalReceiver(BlueprintSavePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (payload.blueprint() == null || payload.blueprint().length() > 30000) return;
            var stack = payload.offHand() ? player.getOffHandStack() : player.getMainHandStack();
            if (!stack.isOf(PortalItems.SHIP_COMPUTER)) return;

            NbtCompound data = new NbtCompound();
            data.putString("blueprint", payload.blueprint());
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
            player.getInventory().markDirty();
            player.currentScreenHandler.sendContentUpdates();
        });
    }

    public static void sendDestinations(ServerPlayerEntity player, List<PlanetDestination> destinations) {
        String encoded = destinations.stream().map(PlanetDestination::id).reduce((a, b) -> a + "," + b).orElse("");
        ServerPlayNetworking.send(player, new DestinationsPayload(encoded));
    }
}
