package com.songoda.epicspawners.api.boost;

import java.time.Instant;

public interface SpawnerBoost {

    BoostType getType();

    int getAmount();

    boolean isTemporary();

    boolean isExpired();

    Instant getEndTime();

    Object getData();

}