package com.darkspace.client;

import com.darkspace.item.PortalItems;
import com.darkspace.network.PortalNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ShipBlueprintScreen extends Screen {
    private static final int GRID_SIZE = 10;
    private static final int CELL_SIZE = 12;
    private static final int GRID_LEFT = 14;
    private static final int GRID_TOP = 68;

    private final Hand hand;
    private final Map<String, String> placedBlocks = new LinkedHashMap<>();
    private final List<Item> palette = new ArrayList<>();
    private int selectedPaletteIndex;
    private int sizeX = 10;
    private int sizeY = 10;
    private int sizeZ = 10;
    private int layerY;
    private int pageX;
    private int pageZ;
    private String status = "Choose a block, then click a cell.";

    public ShipBlueprintScreen(Hand hand) {
        super(Text.literal("DarkSpace Ship Blueprint"));
        this.hand = hand;
        loadSavedBlueprint();
    }

    private void loadSavedBlueprint() {
        var player = MinecraftClient.getInstance().player;
        if (player == null) return;
        ItemStack stack = player.getStackInHand(hand);
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return;
        String encoded = customData.copyNbt().getString("blueprint").orElse("");
        if (encoded.isBlank()) return;

        String[] sections = encoded.split(";", 2);
        if (sections.length != 2) return;
        String[] dimensions = sections[0].split(",");
        try {
            if (dimensions.length == 3) {
                sizeX = clamp(Integer.parseInt(dimensions[0]), 1, 100);
                sizeY = clamp(Integer.parseInt(dimensions[1]), 1, 100);
                sizeZ = clamp(Integer.parseInt(dimensions[2]), 1, 100);
            }
        } catch (NumberFormatException ignored) {
            sizeX = sizeY = sizeZ = 10;
        }

        for (String entry : sections[1].split("\\|")) {
            String[] pair = entry.split("=", 2);
            if (pair.length == 2 && pair[0].matches("\\d+,\\d+,\\d+")
                    && Identifier.tryParse(pair[1]) != null && placedBlocks.size() < 10000) {
                placedBlocks.put(pair[0], pair[1]);
            }
        }
        status = "Loaded blueprint (" + placedBlocks.size() + " placed blocks).";
    }

    @Override
    protected void init() {
        refreshPalette();
        clearChildren();

        int controlX = GRID_LEFT + GRID_SIZE * CELL_SIZE + 14;
        int controlWidth = Math.max(112, Math.min(170, width - controlX - 12));

        for (int z = 0; z < GRID_SIZE; z++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                final int cellX = x;
                final int cellZ = z;
                int px = GRID_LEFT + x * CELL_SIZE;
                int py = GRID_TOP + z * CELL_SIZE;
                addDrawableChild(ButtonWidget.builder(Text.literal(cellLabel(cellX, cellZ)), button -> {
                    int actualX = pageX * GRID_SIZE + cellX;
                    int actualZ = pageZ * GRID_SIZE + cellZ;
                    if (actualX >= sizeX || actualZ >= sizeZ) return;
                    String cellKey = key(actualX, layerY, actualZ);
                    if (placedBlocks.containsKey(cellKey)) {
                        placedBlocks.remove(cellKey);
                    } else if (!palette.isEmpty()) {
                        placedBlocks.put(cellKey, Registries.ITEM.getId(palette.get(selectedPaletteIndex)).toString());
                    }
                    status = "Blocks placed: " + placedBlocks.size();
                    init();
                }).dimensions(px, py, CELL_SIZE - 1, CELL_SIZE - 1).build());
            }
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Block: " + shorten(selectedBlockName(), 18)), button -> {
            if (!palette.isEmpty()) selectedPaletteIndex = (selectedPaletteIndex + 1) % palette.size();
            status = "Selected: " + selectedBlockName();
            init();
        }).dimensions(controlX, GRID_TOP, controlWidth, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("X " + sizeX + "  −"), button -> {
            sizeX = Math.max(1, sizeX - 10);
            pageX = Math.min(pageX, Math.max(0, (sizeX - 1) / GRID_SIZE));
            init();
        }).dimensions(controlX, GRID_TOP + 22, controlWidth / 2 - 2, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> {
            sizeX = Math.min(100, sizeX + 10);
            init();
        }).dimensions(controlX + controlWidth / 2 + 2, GRID_TOP + 22, controlWidth / 2 - 2, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Y " + sizeY + "  −"), button -> {
            sizeY = Math.max(1, sizeY - 10);
            layerY = Math.min(layerY, sizeY - 1);
            init();
        }).dimensions(controlX, GRID_TOP + 43, controlWidth / 2 - 2, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> {
            sizeY = Math.min(100, sizeY + 10);
            init();
        }).dimensions(controlX + controlWidth / 2 + 2, GRID_TOP + 43, controlWidth / 2 - 2, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Z " + sizeZ + "  −"), button -> {
            sizeZ = Math.max(1, sizeZ - 10);
            pageZ = Math.min(pageZ, Math.max(0, (sizeZ - 1) / GRID_SIZE));
            init();
        }).dimensions(controlX, GRID_TOP + 64, controlWidth / 2 - 2, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> {
            sizeZ = Math.min(100, sizeZ + 10);
            init();
        }).dimensions(controlX + controlWidth / 2 + 2, GRID_TOP + 64, controlWidth / 2 - 2, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Layer Y: " + layerY + "  ▲"), button -> {
            layerY = Math.min(sizeY - 1, layerY + 1);
            init();
        }).dimensions(controlX, GRID_TOP + 85, controlWidth, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Layer Y: " + layerY + "  ▼"), button -> {
            layerY = Math.max(0, layerY - 1);
            init();
        }).dimensions(controlX, GRID_TOP + 106, controlWidth, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("X page " + (pageX + 1)), button -> {
            pageX = (pageX + 1) % Math.max(1, (sizeX + GRID_SIZE - 1) / GRID_SIZE);
            init();
        }).dimensions(controlX, GRID_TOP + 127, controlWidth / 2 - 2, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Z page " + (pageZ + 1)), button -> {
            pageZ = (pageZ + 1) % Math.max(1, (sizeZ + GRID_SIZE - 1) / GRID_SIZE);
            init();
        }).dimensions(controlX + controlWidth / 2 + 2, GRID_TOP + 127, controlWidth / 2 - 2, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Save Blueprint"), button -> saveBlueprint())
                .dimensions(width / 2 - 94, height - 26, 112, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> close())
                .dimensions(width / 2 + 22, height - 26, 62, 18).build());
    }

    private void refreshPalette() {
        palette.clear();
        if (client == null || client.player == null) return;
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();
            if (item instanceof BlockItem || item == PortalItems.SHIP_COMPUTER
                    || item == PortalItems.NAVIGATION_COMPUTER || item == PortalItems.HYPERDRIVE) {
                if (!palette.contains(item)) palette.add(item);
            }
        }
        if (!palette.isEmpty()) selectedPaletteIndex = Math.min(selectedPaletteIndex, palette.size() - 1);
    }

    private String selectedBlockName() {
        if (palette.isEmpty()) return "No blocks in inventory";
        return palette.get(selectedPaletteIndex).getName().getString();
    }

    private String cellLabel(int x, int z) {
        String id = placedBlocks.get(key(pageX * GRID_SIZE + x, layerY, pageZ * GRID_SIZE + z));
        if (id == null) return "·";
        Identifier identifier = Identifier.tryParse(id);
        if (identifier == null) return "?";
        String path = identifier.getPath();
        return path.substring(0, Math.min(2, path.length()));
    }

    private static String key(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    private void saveBlueprint() {
        if (!placedBlocks.containsValue("darkspace:ship_computer")
                || !placedBlocks.containsValue("darkspace:navigation_computer")
                || !placedBlocks.containsValue("darkspace:hyperdrive")) {
            status = "Required: Ship Computer, Navigation Computer, and Hyperdrive.";
            return;
        }

        StringBuilder encoded = new StringBuilder()
                .append(sizeX).append(',').append(sizeY).append(',').append(sizeZ).append(';');
        for (Map.Entry<String, String> entry : placedBlocks.entrySet()) {
            encoded.append(entry.getKey()).append('=').append(entry.getValue()).append('|');
            if (encoded.length() > 30000) {
                status = "Blueprint too large to save in one packet. Remove some blocks.";
                return;
            }
        }
        ClientPlayNetworking.send(new PortalNetworking.BlueprintSavePayload(hand == Hand.OFF_HAND, encoded.toString()));
        status = "Blueprint sent to the Ship Computer.";
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int panelRight = Math.min(width - 10, GRID_LEFT + GRID_SIZE * CELL_SIZE + 205);
        context.fill(8, 8, panelRight, height - 8, 0xE6111020);
        context.fill(8, 8, panelRight, 10, 0xFF8A6DCE);
        context.drawTextWithShadow(textRenderer, title, GRID_LEFT, 22, 0xFFE1D4FF);
        context.drawTextWithShadow(textRenderer,
                Text.literal("Size " + sizeX + " × " + sizeY + " × " + sizeZ + " | Layer " + layerY),
                GRID_LEFT, 39, 0xFFBFB4D8);
        context.drawTextWithShadow(textRenderer, Text.literal("Place blocks from your inventory."), GRID_LEFT, 53, 0xFFD0C7E4);
        context.drawTextWithShadow(textRenderer, Text.literal(shorten(status, 56)), GRID_LEFT, height - 42, 0xFFFFD580);
        super.render(context, mouseX, mouseY, delta);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String shorten(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value == null ? "" : value;
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }
}
