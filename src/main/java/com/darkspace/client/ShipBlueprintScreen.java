package com.darkspace.client;

import com.darkspace.network.ShipBlueprintNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ShipBlueprintScreen extends Screen {
    public ShipBlueprintScreen() { super(Text.literal("Ship Blueprint Computer")); }

    @Override
    protected void init() {
        int left = this.width / 2 - 100;
        int top = this.height / 2 - 55;
        addDrawableChild(ButtonWidget.builder(Text.literal("Set Corner 1"), b -> send("corner1"))
                .dimensions(left, top, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Set Corner 2"), b -> send("corner2"))
                .dimensions(left, top + 25, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Create Blueprint"), b -> send("create"))
                .dimensions(left, top + 55, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(left, top + 85, 200, 20).build());
    }

    private void send(String action) {
        if (ClientPlayNetworking.canSend(ShipBlueprintNetworking.ActionPayload.ID)) {
            ClientPlayNetworking.send(new ShipBlueprintNetworking.ActionPayload(action));
        }
        if ("create".equals(action)) close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 85, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Look at a block, then select each corner."), width / 2, height / 2 - 70, 0xD8E8F4);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Max size: 100 x 100 x 100 blocks."), width / 2, height / 2 + 57, 0xD8E8F4);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}
