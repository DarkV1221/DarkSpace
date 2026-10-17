package com.darkspace.client;

import com.darkspace.item.PortalItems;
import com.darkspace.network.PortalNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
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
    private static final int CELL_SIZE = 20;
    private static final int GRID_LEFT = 28;
    private static final int GRID_TOP = 92;

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
    }

    @Override
    protected void init() {
        refreshPalette();
        clearChildren();

        int right = GRID_LEFT + GRID_SIZE * CELL_SIZE + 20;
        int controlX = right + 8;
        int controlWidth = Math.max(112, Math.min(160, width - controlX - 12));

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
                    String key = key(actualX, layerY, actualZ);
                    if (placedBlocks.containsKey(key)) {
                        placedBlocks.remove(key);
                    } else if (!palette.isEmpty()) {
                        placedBlocks.put(key, Registries.ITEM.getId(palette.get(selectedPaletteIndex)).toString());
                    }
                    status = "Blocks placed: " + placedBlocks.size();
                    rebuildGridLabels();
                }).dimensions(px, py, CELL_SIZE - 1, CELL_SIZE - 1).build());
            }
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Block: " + selectedBlockName()), button -> {
            if (!palette.isEmpty()) selectedPaletteIndex = (selectedPaletteIndex + 1) % palette.size();
            status = "Selected: " + selectedBlockName();
            init();
        }).dimensions(controlX, GRID_TOP, controlWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Width X: " + sizeX + "  −"), button -> {
            sizeX = Math.max(1, sizeX - 10);
            pageX = Math.min(pageX, Math.max(0, (sizeX - 1) / GRID_SIZE));
            init();
        }).dimensions(controlX, GRID_TOP + 26, controlWidth / 2 - 2, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> {
            sizeX = Math.min(100, sizeX + 10);
            init();
        }).dimensions(controlX + controlWidth / 2 + 2, GRID_TOP + 26, controlWidth / 2 - 2, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Height Y: " + sizeY + "  −"), button -> {
            sizeY = Math.max(1, sizeY - 10);
            layerY = Math.min(layerY, sizeY - 1);
            init();
        }).dimensions(controlX, GRID_TOP + 50, controlWidth / 2 - 2, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> {
            sizeY = Math.min(100, sizeY + 10);
            init();
        }).dimensions(controlX + controlWidth / 2 + 2, GRID_TOP + 50, controlWidth / 2 - 2, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Length Z: " + sizeZ + "  −"), button -> {
            sizeZ = Math.max(1, sizeZ - 10);
            pageZ = Math.min(pageZ, Math.max(0, (sizeZ - 1) / GRID_SIZE));
            init();
        }).dimensions(controlX, GRID_TOP + 74, controlWidth / 2 - 2, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> {
            sizeZ = Math.min(100, sizeZ + 10);
            init();
        }).dimensions(controlX + controlWidth / 2 + 2, GRID_TOP + 74, controlWidth / 2 - 2, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Layer Y: " + layerY + "  ▲"), button -> {
            layerY = Math.min(sizeY - 1, layerY + 1);
            init();
        }).dimensions(controlX, GRID_TOP + 98, controlWidth, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Layer Y: " + layerY + "  ▼"), button -> {
            layerY = Math.max(0, layerY - 1);
            init();
        }).dimensions(controlX, GRID_TOP + 122, controlWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("X Page " + (pageX + 1)), button -> {
            pageX = (pageX + 1) % Math.max(1, (sizeX + GRID_SIZE - 1) / GRID_SIZE);
            init();
        }).dimensions(controlX, GRID_TOP + 146, controlWidth, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Z Page " + (pageZ + 1)), button -> {
            pageZ = (pageZ + 1) % Math.max(1, (sizeZ + GRID_SIZE - 1) / GRID_SIZE);
            init();
        }).dimensions(controlX, GRID_TOP + 170, controlWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Save Blueprint"), button -> saveBlueprint())
                .dimensions(width / 2 - 105, height - 30, 125, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> close())
                .dimensions(width / 2 + 25, height - 30, 80, 20).build());
    }

    private void refreshPalette() {
        palette.clear();
        if (client == null || client.player == null) return;
        for (ItemStack stack : client.player.getInventory().getMainStacks()) {
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
        return path.length() <= 3 ? path : path.substring(0, 3);
    }

    private void rebuildGridLabels() {
        // Reinitializing refreshes the text on each cell while preserving the blueprint map.
        init();
    }

    private static String key(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    private void saveBlueprint() {
        boolean hasShipComputer = placedBlocks.containsValue("darkspace:ship_computer");
        boolean hasNavigationComputer = placedBlocks.containsValue("darkspace:navigation_computer");
        boolean hasHyperdrive = placedBlocks.containsValue("darkspace:hyperdrive");

        if (!hasShipComputer || !hasNavigationComputer || !hasHyperdrive) {
            status = "Required: Ship Computer, Navigation Computer, and Hyperdrive.";
            return;
        }

        StringBuilder encoded = new StringBuilder()
                .append(sizeX).append(',').append(sizeY).append(',').append(sizeZ).append(';');
        for (Map.Entry<String, String> entry : placedBlocks.entrySet()) {
            if (encoded.length() > 30000) {
                status = "Blueprint is too large to save in one packet. Remove some blocks.";
                return;
            }
            encoded.append(entry.getKey()).append('=').append(entry.getValue()).append('|');
        }
        ClientPlayNetworking.send(new PortalNetworking.BlueprintSavePayload(hand == Hand.OFF_HAND, encoded.toString()));
        status = "Blueprint saved to the Ship Computer!";
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int panelRight = Math.min(width - 10, GRID_LEFT + GRID_SIZE * CELL_SIZE + 205);
        context.fill(10, 10, panelRight, height - 10, 0xE6111020);
        context.fill(10, 10, panelRight, 12, 0xFF8A6DCE);
        context.drawTextWithShadow(textRenderer, title, GRID_LEFT, 24, 0xFFE1D4FF);
        context.drawTextWithShadow(textRenderer,
                Text.literal("Blueprint: " + sizeX + " × " + sizeY + " × " + sizeZ + "  |  Y layer " + layerY),
                GRID_LEFT, 43, 0xFFBFB4D8);
        context.drawTextWithShadow(textRenderer,
                Text.literal("Click cells to place/remove the selected inventory block."),
                GRID_LEFT, 59, 0xFFD0C7E4);
        context.drawTextWithShadow(textRenderer, Text.literal(status), GRID_LEFT, height - 48, 0xFFFFD580);
        super.render(context, mouseX, mouseY, delta);
    }
}
