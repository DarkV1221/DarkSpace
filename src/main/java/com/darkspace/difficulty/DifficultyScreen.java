package com.darkspace.difficulty;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class DifficultyScreen extends Screen {
    private static final int PANEL = 0xE817102B;
    private static final int EDGE = 0xFF8D55D9;
    private static final int GLOW = 0xFFB9F7FF;

    private final Screen parent;
    private SpaceDifficulty selected;

    public DifficultyScreen(Screen parent) {
        super(Text.literal("DarkSpace Difficulty"));
        this.parent = parent;
        this.selected = DifficultyConfig.getDifficulty();
    }

    @Override
    protected void init() {
        int center = width / 2;
        int top = Math.max(54, height / 2 - 68);

        for (SpaceDifficulty option : SpaceDifficulty.values()) {
            this.addDrawableChild(ButtonWidget.builder(
                    optionLabel(option), button -> {
                        selected = option;
                        // Rebuild the buttons only after a valid selection; no screen stack changes.
                        clearAndInit();
                    })
                    .dimensions(center - 100, top, 200, 20)
                    .build());
            top += 25;
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Apply"), button -> {
            MinecraftClient.getInstance().setScreen(new ConfirmScreen(confirmed -> {
                if (confirmed) {
                    DifficultyConfig.setDifficulty(selected);
                }
                MinecraftClient.getInstance().setScreen(parent);
            }, Text.literal("Apply " + selected.label() + " difficulty?"),
                    Text.literal("Your choice is saved locally. Some space-survival effects are still planned.")));
        }).dimensions(center - 100, top + 5, 98, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button ->
                MinecraftClient.getInstance().setScreen(parent))
                .dimensions(center + 2, top + 5, 98, 20).build());
    }

    private Text optionLabel(SpaceDifficulty option) {
        return Text.literal((option == selected ? "» " : "") + option.label());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int left = Math.max(8, width / 2 - 164);
        int right = Math.min(width - 8, width / 2 + 164);
        context.fill(left - 2, 10, right + 2, Math.min(height - 8, 194), EDGE);
        context.fill(left, 12, right, Math.min(height - 10, 192), PANEL);
        context.fill(left + 8, 16, right - 8, 17, GLOW);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 22, GLOW);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(selected.description()),
                width / 2, 39, 0xFFD8C8FF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}
