package com.songoda.epicspawners.boost;

import java.time.Instant;
import java.util.Objects;

import com.songoda.epicspawners.api.boost.BoostType;
import com.songoda.epicspawners.api.boost.SpawnerBoost;

public class ESpawnerBoost implements SpawnerBoost {

    private final BoostType boostType;
    private final int amount;
    private final Instant endTime;
    private final Object data;

    public ESpawnerBoost(BoostType type, int amount, Instant endTime, Object data) {
        this.boostType = type;
        this.amount = amount;
        this.endTime = endTime;
        this.data = data;
    }

    @Override
    public BoostType getType() {
        return boostType;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public boolean isTemporary() {
        return !endTime.equals(Instant.MAX);
    }

    @Override
    public boolean isExpired() {
        return Instant.now().isAfter(endTime);
    }

    @Override
    public Instant getEndTime() {
        return endTime;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public int hashCode() {
        int result = 31 * amount;

        result = 31 * result + (boostType == null ? 0 : boostType.hashCode());
        result = 31 * result + (data == null ? 0 : data.hashCode());
        result = 31 * result + (endTime == null ? 0 : endTime.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ESpawnerBoost)) return false;

        ESpawnerBoost other = (ESpawnerBoost) obj;
        return amount == other.amount && boostType == other.boostType
            && Objects.equals(endTime, other.endTime) && Objects.equals(data, other.data);
    }

}
