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
    public static SpaceDifficulty getDifficulty() { return difficulty; }
    public static void setDifficulty(SpaceDifficulty value) { difficulty = value; save(); }
    public static void load() {
        if (!Files.exists(FILE)) { save(); return; }
        try {
            ConfigData data = GSON.fromJson(Files.readString(FILE), ConfigData.class);
            if (data != null && data.difficulty != null) difficulty = SpaceDifficulty.valueOf(data.difficulty);
        } catch (IOException | IllegalArgumentException exception) {
            difficulty = SpaceDifficulty.NORMAL;
        }
    }
    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(new ConfigData(difficulty.name())));
        } catch (IOException ignored) { }
    }
    private record ConfigData(String difficulty) {}
}
