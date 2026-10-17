package com.darkspace.difficulty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DifficultyConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("darkspace-difficulty.json");
    private static SpaceDifficulty difficulty = SpaceDifficulty.NORMAL;

    private DifficultyConfig() {}

    public static SpaceDifficulty getDifficulty() {
        return difficulty;
    }

    public static void setDifficulty(SpaceDifficulty value) {
        if (value == null) return;
        difficulty = value;
        save();
    }

    public static void load() {
        difficulty = SpaceDifficulty.NORMAL;
        try {
            if (!Files.exists(FILE)) {
                save();
                return;
            }
            ConfigData data = GSON.fromJson(Files.readString(FILE), ConfigData.class);
            if (data != null && data.difficulty != null) {
                difficulty = SpaceDifficulty.valueOf(data.difficulty);
            }
        } catch (IOException | RuntimeException exception) {
            // A missing, malformed, or outdated config must never crash the game/menu.
            difficulty = SpaceDifficulty.NORMAL;
            save();
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(new ConfigData(difficulty.name())));
        } catch (IOException | RuntimeException exception) {
            // Keep the in-memory setting usable even if the config folder is read-only.
        }
    }

    private record ConfigData(String difficulty) {}
}
