package com.darkspace;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DarkSpace implements ModInitializer {
    public static final String MOD_ID = "darkspace";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("DarkSpace is initializing.");
    }
}
