package com.songoda.epicspawners.utils;

@Deprecated // No longer needed due to 1.13+ support. May be needed in future. - Choco
public enum ServerVersion {

    UNKNOWN("unknown_server_version"),
    V1_13("org.bukkit.craftbukkit.v1_13");


    private final String packagePrefix;

    private ServerVersion(String packagePrefix) {
        this.packagePrefix = packagePrefix;
    }

    public static ServerVersion fromPackageName(String packageName) {
        for (ServerVersion version : values())
            if (packageName.startsWith(version.packagePrefix)) return version;
        return ServerVersion.UNKNOWN;
    }

}