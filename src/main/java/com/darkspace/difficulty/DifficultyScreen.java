package com.darkspace.difficulty;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class DifficultyScreen extends Screen {
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
        int top = height / 2 - 60;
        for (SpaceDifficulty option : SpaceDifficulty.values()) {
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal((option == selected ? "» " : "") + option.label()), button -> {
                    selected = option;
                    clearAndInit();
                }).dimensions(center - 100, top, 200, 20).build());
            top += 25;
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Apply"), button ->
            client.setScreen(new ConfirmScreen(confirmed -> {
                if (confirmed) {
                    DifficultyConfig.setDifficulty(selected);
                }
                client.setScreen(parent);
            },
                Text.literal("Apply " + selected.label() + " difficulty?"),
                Text.literal("This saves the choice. Space-survival effects are not implemented yet.")
            ))
        ).dimensions(center - 100, top + 4, 98, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button ->
            client.setScreen(parent)
        ).dimensions(center + 2, top + 4, 98, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 22, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(selected.description()),
            width / 2, 38, 0xD8C8FF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
