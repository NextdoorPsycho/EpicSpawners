package com.songoda.epicspawners.utils;

import com.songoda.epicspawners.EpicSpawnersPlugin;

/**
 * Created by songoda on 3/21/2017.
 */
public class Debugger {

    public static void runReport(Exception e) {
        if (!isDebug()) return;

        System.out.println("==============================================================");
        System.out.println("The following is an error encountered in EpicSpawners.");
        System.out.println("--------------------------------------------------------------");
        e.printStackTrace();
        System.out.println("==============================================================");
    }

    public static boolean isDebug() {
        EpicSpawnersPlugin plugin = EpicSpawnersPlugin.getInstance();
        return plugin.getConfig().getBoolean("System.Debugger Enabled");
    }

}
