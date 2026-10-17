package com.darkspace.item;

import com.darkspace.network.PortalNetworking;
import com.darkspace.portal.PortalTravelService;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;

public final class PlanetaryPortalGunItem extends Item {
    public PlanetaryPortalGunItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
            PortalNetworking.sendDestinations(serverPlayer, PortalTravelService.discoveredDestinations(serverPlayer));
        }
        return ActionResult.SUCCESS;
    }
}