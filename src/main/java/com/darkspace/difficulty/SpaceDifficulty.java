package com.darkspace.difficulty;

public enum SpaceDifficulty {
    PEACEFUL("Peaceful", "No hostile alien mobs; forgiving survival hazards."),
    NORMAL("Normal", "Balanced oxygen, hazards, enemies, and resources."),
    HARD("Hard", "Faster oxygen use, harsher hazards, and tougher enemies."),
    NIGHTMARE("Nightmare", "Extreme hazards, aggressive enemies, and scarce supplies.");

    private final String label;
    private final String description;

    SpaceDifficulty(String label, String description) {
        this.label = label;
        this.description = description;
    }
    public String label() { return label; }
    public String description() { return description; }
}
