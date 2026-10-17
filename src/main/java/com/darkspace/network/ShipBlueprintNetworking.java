package com.darkspace.network;

import com.darkspace.DarkSpace;
import com.darkspace.item.ShipBlueprintItem;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ShipBlueprintNetworking {
    private ShipBlueprintNetworking() {}
    private static final Map<UUID, BlockPos[]> CORNERS = new ConcurrentHashMap<>();

    public record ActionPayload(String action) implements CustomPayload {
        public static final Id<ActionPayload> ID = new Id<>(Identifier.of(DarkSpace.MOD_ID, "ship_blueprint_action"));
        public static final PacketCodec<RegistryByteBuf, ActionPayload> CODEC =
                PacketCodec.tuple(PacketCodecs.STRING, ActionPayload::action, ActionPayload::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public static void initialize() {
        PayloadTypeRegistry.playC2S().register(ActionPayload.ID, ActionPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ActionPayload.ID, (payload, context) ->
                handle(context.player(), payload.action()));
    }

    private static void handle(ServerPlayerEntity player, String action) {
        if (!action.equals("corner1") && !action.equals("corner2") && !action.equals("create")) return;
        if (action.equals("corner1") || action.equals("corner2")) {
            HitResult hit = player.raycast(6.0, 1.0F, false);
            if (hit.getType() != HitResult.Type.BLOCK || !(hit instanceof BlockHitResult blockHit)) {
                player.sendMessage(Text.literal("Look directly at a block within 6 blocks first."), true);
                return;
            }
            BlockPos[] corners = CORNERS.computeIfAbsent(player.getUuid(), id -> new BlockPos[2]);
            corners[action.equals("corner1") ? 0 : 1] = blockHit.getBlockPos().toImmutable();
            player.sendMessage(Text.literal((action.equals("corner1") ? "Corner 1" : "Corner 2") + " saved: " + blockHit.getBlockPos().toShortString()), true);
            return;
        }
        BlockPos[] corners = CORNERS.get(player.getUuid());
        if (corners == null || corners[0] == null || corners[1] == null) {
            player.sendMessage(Text.literal("Select both corners before creating a blueprint."), true);
            return;
        }
        BlockPos a = corners[0], b = corners[1];
        BlockPos min = new BlockPos(Math.min(a.getX(),b.getX()),Math.min(a.getY(),b.getY()),Math.min(a.getZ(),b.getZ()));
        BlockPos max = new BlockPos(Math.max(a.getX(),b.getX()),Math.max(a.getY(),b.getY()),Math.max(a.getZ(),b.getZ()));
        if (max.getX()-min.getX()+1 > 100 || max.getY()-min.getY()+1 > 100 || max.getZ()-min.getZ()+1 > 100) {
            player.sendMessage(Text.literal("Blueprint too large! Each dimension must be 100 blocks or less."), true);
            return;
        }
        try {
            var blueprint = ShipBlueprintItem.createBlueprint("My Ship", min, max, player.getWorld());
            if (!player.getInventory().insertStack(blueprint)) player.dropItem(blueprint, false);
            player.sendMessage(Text.literal("Blueprint created! It is now in your inventory."), true);
            CORNERS.remove(player.getUuid());
        } catch (RuntimeException ex) {
            DarkSpace.LOGGER.error("Could not create ship blueprint for {}", player.getName().getString(), ex);
            player.sendMessage(Text.literal("Blueprint creation failed. Try a smaller selection."), true);
        }
    }
}
