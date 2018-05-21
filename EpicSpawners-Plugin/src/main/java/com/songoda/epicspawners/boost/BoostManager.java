package com.songoda.epicspawners.boost;

import java.util.HashSet;
import java.util.Set;

public class BoostManager {

    private final Set<ESpawnerBoost> boostedSpawners = new HashSet<>();

    public void addBoostToSpawner(ESpawnerBoost data) {
        this.boostedSpawners.add(data);
    }

    public void removeBoostFromSpawner(ESpawnerBoost data) {
        this.boostedSpawners.remove(data);
    }

    public Set<ESpawnerBoost> getBoosts() {
        return boostedSpawners;
    }
}
