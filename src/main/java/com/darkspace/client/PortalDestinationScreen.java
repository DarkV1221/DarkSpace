package com.darkspace.client;

import com.darkspace.network.PortalNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;

public final class PortalDestinationScreen extends Screen {
    private final List<String> destinationIds;

    public PortalDestinationScreen(String encodedDestinations) {
        super(Text.literal("Planetary Portal Gun"));
        if (encodedDestinations == null || encodedDestinations.isBlank()) {
            this.destinationIds = List.of();
        } else {
            this.destinationIds = Arrays.stream(encodedDestinations.split(","))
                    .filter(id -> !id.isBlank())
                    .toList();
        }
    }

    @Override
    protected void init() {
        int buttonWidth = 250;
        int left = (this.width - buttonWidth) / 2;
        int y = 58;

        if (destinationIds.isEmpty()) {
            addDrawableChild(ButtonWidget.builder(Text.literal("No discovered planets yet"), button -> {})
                    .dimensions(left, y, buttonWidth, 20).build());
            y += 26;
        } else {
            for (String id : destinationIds) {
                String label = displayName(id) + "  •  " + fuelName(id);
                addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> {
                    ClientPlayNetworking.send(new PortalNetworking.TravelRequestPayload(id));
                    if (client != null) client.setScreen(null);
                }).dimensions(left, y, buttonWidth, 20).build());
                y += 24;
                if (y > this.height - 45) break;
            }
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> close())
                .dimensions(left, this.height - 32, buttonWidth, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xE6D8FF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Each jump consumes one fuel cell."), width / 2, 37, 0x9CDDE3);
        super.render(context, mouseX, mouseY, delta);
    }

    private static String displayName(String id) {
        return switch (id) {
            case "minecraft:overworld" -> "Earth";
            case "darkspace:luna" -> "Luna";
            case "darkspace:ares" -> "Ares";
            case "darkspace:nox" -> "Nox";
            case "darkspace:shattered_expanse" -> "Shattered Expanse";
            case "darkspace:outer_reaches" -> "Outer Reaches";
            default -> id;
        };
    }

    private static String fuelName(String id) {
        return switch (id) {
            case "darkspace:nox", "darkspace:shattered_expanse" -> "Refined Cell";
            case "darkspace:outer_reaches" -> "Quantum Cell";
            default -> "Portal Cell";
        };
    }
}
