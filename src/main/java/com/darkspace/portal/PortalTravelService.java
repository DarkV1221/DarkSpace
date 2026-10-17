package com.darkspace.portal;

import com.darkspace.item.PortalItems;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class PortalTravelService {
    private static final String DISCOVERY_PREFIX = "darkspace_discovered_";

    private PortalTravelService() {}

    public static void recordVisit(ServerPlayerEntity player) {
        String currentId = player.getWorld().getRegistryKey().getValue().toString();
        PlanetDestination destination = PlanetDestination.find(currentId);
        if (destination != null) {
            String tag = discoveryTag(destination);
            if (!player.getCommandTags().contains(tag)) {
                player.addCommandTag(tag);
                player.sendMessage(Text.literal("Planet discovered: " + destination.name() + "!"), true);
            }
        }
    }

    public static List<PlanetDestination> discoveredDestinations(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return List.of();

        List<PlanetDestination> found = new ArrayList<>();
        for (PlanetDestination destination : PlanetDestination.ALL) {
            Identifier id = destination.identifier();
            if (id == null) continue;
            RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, id);
            if (server.getWorld(key) != null && player.getCommandTags().contains(discoveryTag(destination))) {
                found.add(destination);
            }
        }
        return found;
    }

    public static void travel(ServerPlayerEntity player, String destinationId) {
        PlanetDestination destination = PlanetDestination.find(destinationId);
        if (destination == null) {
            player.sendMessage(Text.literal("Unknown planetary destination."), true);
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) return;

        if (!player.getCommandTags().contains(discoveryTag(destination))) {
            player.sendMessage(Text.literal("You must physically visit this planet before it unlocks."), true);
            return;
        }

        Identifier identifier = destination.identifier();
        if (identifier == null) return;
        RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, identifier);
        ServerWorld targetWorld = server.getWorld(key);
        if (targetWorld == null) {
            player.sendMessage(Text.literal(destination.name() + " is not available yet. Its dimension has not been added to this world."), true);
            return;
        }
        if (player.getWorld() == targetWorld) {
            player.sendMessage(Text.literal("You're already at " + destination.name() + "."), true);
            return;
        }

        ItemStack fuel = findFuel(player, destination.fuelTier());
        if (fuel == null) {
            player.sendMessage(Text.literal("Not enough fuel. " + fuelName(destination.fuelTier()) + " or better is required."), true);
            return;
        }

        BlockPos top = targetWorld.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, targetWorld.getSpawnPos());
        boolean teleported = player.teleport(
                targetWorld,
                top.getX() + 0.5,
                top.getY() + 1.0,
                top.getZ() + 0.5,
                Set.of(),
                player.getYaw(),
                player.getPitch(),
                true
        );
        if (!teleported) {
            player.sendMessage(Text.literal("Portal jump failed. No fuel was consumed."), true);
            return;
        }

        fuel.decrement(1);
        player.sendMessage(Text.literal("Portal jump complete: " + destination.name() + "."), true);
        recordVisit(player);
    }

    private static ItemStack findFuel(ServerPlayerEntity player, int requiredTier) {
        for (ItemStack stack : player.getInventory().main) {
            if (!stack.isEmpty() && PortalItems.fuelTier(stack.getItem()) >= requiredTier) {
                return stack;
            }
        }
        return null;
    }

    private static String fuelName(int tier) {
        return switch (tier) {
            case 1 -> "Portal Cell";
            case 2 -> "Refined Portal Cell";
            default -> "Quantum Portal Cell";
        };
    }

    private static String discoveryTag(PlanetDestination destination) {
        return DISCOVERY_PREFIX + destination.id().replace(':', '_').replace('/', '_');
    }
}
