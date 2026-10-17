package com.darkspace.item;

import com.darkspace.client.ShipBlueprintScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public final class ShipComputerItem extends Item {
    public ShipComputerItem(Settings settings) { super(settings); }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
            MinecraftClient.getInstance().setScreen(new ShipBlueprintScreen());
        }
        return ActionResult.SUCCESS;
    }
}
