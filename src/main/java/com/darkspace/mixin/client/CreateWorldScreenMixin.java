package com.darkspace.mixin.client;

import com.darkspace.difficulty.DifficultyScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
    protected CreateWorldScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void darkspace$addDifficultyButton(CallbackInfo ci) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("DarkSpace Difficulty"), button ->
            this.client.setScreen(new DifficultyScreen(this)))
            .dimensions(this.width - 150, this.height - 34, 140, 20)
            .build());
    }
}
