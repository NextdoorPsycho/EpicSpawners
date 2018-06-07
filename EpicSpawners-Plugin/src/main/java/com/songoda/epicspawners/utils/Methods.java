package com.songoda.epicspawners.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Created by songoda on 2/24/2017.
 */
@SuppressWarnings("deprecation")
public class Methods {

    private static final Set<Material> STAINED_GLASS = EnumSet.of(
            Material.BLACK_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE,
            Material.BROWN_STAINED_GLASS_PANE, Material.CYAN_STAINED_GLASS_PANE,
            Material.GRAY_STAINED_GLASS, Material.GREEN_STAINED_GLASS_PANE,
            Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.LIGHT_GRAY_STAINED_GLASS_PANE,
            Material.LIME_STAINED_GLASS_PANE, Material.MAGENTA_STAINED_GLASS_PANE,
            Material.ORANGE_STAINED_GLASS_PANE, Material.PINK_STAINED_GLASS_PANE,
            Material.PURPLE_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE,
            Material.WHITE_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE
    );

    public static void takeItem(Player player, int amount) {
        if (player.getGameMode() == GameMode.CREATIVE) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;

        int result = item.getAmount() - amount;
        item.setAmount(result);
        player.getInventory().setItemInMainHand(result > 0 ? item : null);
    }

    public static String getBoostCost(int time, int amount) {
        StringBuilder cost = new StringBuilder("&6&l");
        String[] parts = EpicSpawnersPlugin.getInstance().getConfig().getString("Spawner Boosting.Item Charged For A Boost").split(":");

        String type = parts[0];
        String multi = parts[1];

        int co = boostCost(multi, time, amount);

        if (!type.equals("ECO") && !type.equals("XP")) {
            cost.append(co).append(" &7").append(WordUtils.capitalizeFully(type));
            if (co != 1) {
                cost.append('s');
            }
        } else if (type.equals("ECO")) {
            cost.append('$').append(TextComponent.formatEconomy(co));
        } else if (type.equals("XP")) {
            cost.append(co).append(" &7Levels");
        }

        return cost.toString();
    }

    public static int boostCost(String multi, int time, int amt) {
        return (int) Math.ceil(NumberUtils.toDouble(multi, 1) * time * amt);
    }

    public static String compileName(String type, int multi, boolean full) {
        try {
            EpicSpawnersPlugin plugin = EpicSpawnersPlugin.getInstance();

            String nameFormat = plugin.getConfig().getString("Main.Spawner Name Format");
            String displayName = plugin.getSpawnerManager().getSpawnerData(type).getDisplayName();

            nameFormat = nameFormat.replace("{TYPE}", displayName);

            if ((multi > 1 || plugin.getConfig().getBoolean("Main.Display Level In Spawner Title If Level 1") || plugin.getConfig().getBoolean("Main.Named Spawners Tiers")) && multi >= 0) {
                if (plugin.getConfig().getBoolean("Main.Named Spawners Tiers") && plugin.getConfig().getStringList("Main.Tier Names").size() >= multi) {
                    nameFormat = nameFormat.replace("{AMT}", plugin.getConfig().getStringList("Main.Tier Names").get(multi - 1));
                } else {
                    nameFormat = nameFormat.replace("{AMT}", Integer.toString(multi));
                }
                nameFormat = nameFormat.replace("[", "").replace("]", "");
            } else {
                nameFormat = nameFormat.replaceAll("\\[.*?]", "");
            }

            String info = "";
            if (full) {
                info += TextComponent.convertToInvisibleString(type.toUpperCase().replaceAll(" ", "_") + ":" + multi + ":");
            }

            return info + TextComponent.formatText(nameFormat).trim();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public static ItemStack getGlass() {
        try {
            FileConfiguration config = EpicSpawnersPlugin.getInstance().getConfig();
            return Arconix.pl().getApi().getGUI().getGlass(config.getBoolean("Interfaces.Replace Glass Type 1 With Rainbow Glass"), config.getInt("Interfaces.Glass Type 1"));
        } catch (Exception e) {
            Debugger.runReport(e);
        }

        return null;
    }

    public static ItemStack getBackgroundGlass(boolean type) {
        try {
            EpicSpawnersPlugin plugin = EpicSpawnersPlugin.getInstance();
            return Arconix.pl().getApi().getGUI().getGlass(false, plugin.getConfig().getInt("Interfaces.Glass Type " + (type ? 2 : 3)));
        } catch (Exception e) {
            Debugger.runReport(e);
        }

        return null;
    }

    public static String getTypeFromString(String type) {
        return (type != null) ? ChatColor.stripColor(WordUtils.capitalizeFully(type.replace("_", " "))) : null;
    }

    public static String restoreType(String type) {
        return (type != null) ? type.replace(" ", "_").toUpperCase() : null;
    }

    public static boolean isAir(Material type) {
        return type == Material.AIR || Tag.WOODEN_PRESSURE_PLATES.isTagged(type)
            || type == Material.LIGHT_WEIGHTED_PRESSURE_PLATE || type == Material.HEAVY_WEIGHTED_PRESSURE_PLATE;
    }

    public static boolean isStainedGlassPane(Material type) { // No vanilla Tag for this -,-
    	return STAINED_GLASS.contains(type);
    }

    public static int countEntitiesAroundLocation(Location location) {
        try {
            int amt = 0;

            String[] arr = EpicSpawnersPlugin.getInstance().getConfig().getString("Main.Radius To Search Around Spawners").split("x");
            Collection<Entity> nearbyEntite = location.getWorld().getNearbyEntities(location.clone().add(0.5, 0.5, 0.5), Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
            if (nearbyEntite.size() < 1) return amt;

            for (Entity ee : nearbyEntite) {
                if (!(ee instanceof LivingEntity) || ee instanceof Player || ee.getType().name().toLowerCase().contains("armor")) {
                    continue;
                }
                if (EpicSpawnersPlugin.getInstance().getServer().getPluginManager().getPlugin("StackMob") != null
                        && ee.getMetadata(uk.antiperson.stackmob.tools.extras.GlobalValues.METATAG).size() != 0) {
                    amt = amt + ee.getMetadata(uk.antiperson.stackmob.tools.extras.GlobalValues.METATAG).get(0).asInt();
                } else {
                    amt++;
                }
            }
            return amt;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return 0;
    }

}