package com.darkspace.portal;

import net.minecraft.util.Identifier;

import java.util.List;

public record PlanetDestination(String id, String name, int fuelTier) {
    public static final List<PlanetDestination> ALL = List.of(
            new PlanetDestination("minecraft:overworld", "Earth", 1),
            new PlanetDestination("darkspace:luna", "Luna", 1),
            new PlanetDestination("darkspace:ares", "Ares", 1),
            new PlanetDestination("darkspace:nox", "Nox", 2),
            new PlanetDestination("darkspace:shattered_expanse", "Shattered Expanse", 2),
            new PlanetDestination("darkspace:outer_reaches", "Outer Reaches", 3)
    );

    public Identifier identifier() {
        return Identifier.tryParse(id);
    }

    public static PlanetDestination find(String id) {
        return ALL.stream().filter(destination -> destination.id().equals(id)).findFirst().orElse(null);
    }
}