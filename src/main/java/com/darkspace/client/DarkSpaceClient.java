package com.darkspace.client;

import net.fabricmc.api.ClientModInitializer;
import com.darkspace.difficulty.DifficultyConfig;

public final class DarkSpaceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DifficultyConfig.load();
    }
}
