package com.songoda.epicspawners.api.boost;

import org.apache.commons.lang.ArrayUtils;

public enum BoostType {

    LOCATION("org.bukkit.Location"),

    PLAYER("org.bukkit.entity.Player"),

    FACTION("me.markeh.factionsframework.entities.Faction"),

    TOWN("com.palmergames.bukkit.towny.object.Town"),

    ISLAND("com.wasteofplastic.askyblock.Island", "us.talabrek.ultimateskyblock.api.IslandInfo");


    private final String[] supportedDataTypes;

    private BoostType(String... supportedDataTypes) {
        this.supportedDataTypes = supportedDataTypes;
    }

    public boolean isSupportedDataType(Class<?> clazz) {
        return clazz != null && ArrayUtils.contains(supportedDataTypes, clazz.getName());
    }

    public boolean isSupportedDataType(Object object) {
        return object != null && ArrayUtils.contains(supportedDataTypes, object.getClass().getName());
    }

}