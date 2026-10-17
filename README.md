# DarkSpace

A Minecraft space exploration mod targeting **Minecraft Java Edition 1.21.8**, Fabric, and Java 21.

## First milestone: difficulty UI foundation

- Defines Peaceful, Normal, Hard, and Nightmare difficulty levels.
- Includes a confirmation-based difficulty selection screen.
- Persists the selected choice to `config/darkspace-difficulty.json` on the local client.

## Prototype limitations

This is an initial scaffold, not a finished playable feature. The menu is not yet connected to the world-creation or pause menus. The selection is currently a client config, not saved per world or synchronized in multiplayer. Oxygen, planetary hazards, alien aggression, and resource scarcity are not yet affected. These are the next implementation steps.

## Requirements

- Minecraft Java Edition 1.21.8
- Fabric Loader and Fabric API
- Java 21
