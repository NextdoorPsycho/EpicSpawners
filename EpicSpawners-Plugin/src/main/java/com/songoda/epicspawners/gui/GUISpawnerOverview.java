package com.songoda.epicspawners.gui;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.api.methods.formatting.TimeComponent;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.CostType;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.spawners.object.ESpawner;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GUISpawnerOverview extends AbstractGUI {

    private final ESpawner spawner;
    private final Player player;
    private final EpicSpawnersPlugin instance;

    private int infoPage = 1;

    public GUISpawnerOverview(EpicSpawnersPlugin instance, ESpawner spawner, Player player) {
        super(27, TextComponent.formatTitle(Methods.compileName(spawner.getIdentifyingName(), spawner.getSpawnerDataCount(), false)));
        this.spawner = spawner;
        this.player = player;
        this.instance = instance;
    }

    @Override
    protected void initInventoryItems(Inventory inventory) {
        SpawnerData spawnerData = spawner.getFirstStack().getSpawnerData();

        int showAmt = spawner.getSpawnerDataCount();
        if (showAmt > 64 || showAmt == 0)
            showAmt = 1;

        int stackAmount = spawner.getSpawnerStacks().size();

        ItemStack item = new ItemStack(Material.SKULL_ITEM, showAmt, (byte) 3);
        if (stackAmount != 1) {
            item = EpicSpawnersPlugin.getInstance().getHeads().addTexture(item, instance.getSpawnerManager().getSpawnerData("omni"));
        } else {
            try {
                item = EpicSpawnersPlugin.getInstance().getHeads().addTexture(item, spawnerData);
            } catch (Exception e) {
                item = new ItemStack(Material.MOB_SPAWNER, showAmt);
            }
        }

        if (stackAmount == 1 && spawner.getFirstStack().getSpawnerData().getDisplayItem() != null) {
            item.setType(spawner.getFirstStack().getSpawnerData().getDisplayItem());
        }

        ItemMeta itemmeta = item.getItemMeta();
        itemmeta.setDisplayName(instance.getLocale().getMessage("interface.spawner.statstitle"));
        ArrayList<String> lore = new ArrayList<>();

        if (stackAmount != 1) {
            StringBuilder only = new StringBuilder("&6" + Methods.compileName(spawner.getFirstStack().getSpawnerData().getIdentifyingName(), spawner.getFirstStack().getStackSize(), false));

            int num = 1;
            for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                if (num != 1)
                    only.append("&8, &6").append(Methods.compileName(stack.getSpawnerData().getIdentifyingName(), stack.getStackSize(), false));
                num++;
            }

            lore.add(TextComponent.formatText(only.toString()));
        }

        List<Material> blocks = spawner.getFirstStack().getSpawnerData().getSpawnBlocksList();

        StringBuilder only = new StringBuilder(blocks.get(0).name());

        int num = 1;
        for (Material block : blocks) {
            if (num != 1)
                only.append("&8, &6").append(Methods.getTypeFromString(block.name()));
            num++;
        }

        lore.add(instance.getLocale().getMessage("interface.spawner.onlyspawnson", only.toString()));

        lore.add(instance.getLocale().getMessage("interface.spawner.stats", spawner.getSpawnCount()));
        if (player.hasPermission("epicspawners.convert") && spawner.getSpawnerStacks().size() == 1) {
            lore.add("");
            lore.add(instance.getLocale().getMessage("interface.spawner.convert"));
        }
        if (player.hasPermission("epicspawners.canboost")) {
            if (spawner.getBoost() == 0) {
                if (!player.hasPermission("epicspawners.convert") || spawner.getSpawnerStacks().size() != 1) {
                    lore.add("");
                }
                lore.add(instance.getLocale().getMessage("interface.spawner.boost"));
            }
        }
        if (spawner.getBoost() != 0) {

            // ToDo: Make it display all boosts.
            String[] parts = instance.getLocale().getMessage("interface.spawner.boostedstats", Integer.toString(spawner.getBoost()), spawnerData.getIdentifyingName(), TimeComponent.makeReadable(spawner.getBoostEnd().toEpochMilli() - System.currentTimeMillis())).split("\\|");
            lore.add("");
            for (String line : parts)
                lore.add(TextComponent.formatText(line));
        }
        itemmeta.setLore(lore);
        item.setItemMeta(itemmeta);

        int xpCost = spawner.getUpgradeCost(CostType.EXPERIENCE);

        int ecoCost = spawner.getUpgradeCost(CostType.ECONOMY);

        boolean maxed = false;
        if (spawner.getSpawnerDataCount() == EpicSpawnersPlugin.getInstance().getConfig().getInt("Main.Spawner Max Upgrade"))
            maxed = true;

        ItemStack itemXP = new ItemStack(Material.valueOf(instance.getConfig().getString("Interfaces.XP Icon")), 1);
        ItemMeta itemmetaXP = itemXP.getItemMeta();
        itemmetaXP.setDisplayName(instance.getLocale().getMessage("interface.spawner.upgradewithxp"));
        ArrayList<String> loreXP = new ArrayList<>();
        if (!maxed)
            loreXP.add(instance.getLocale().getMessage("interface.spawner.upgradewithxplore", Integer.toString(xpCost)));
        else
            loreXP.add(instance.getLocale().getMessage("event.upgrade.maxed"));
        itemmetaXP.setLore(loreXP);
        itemXP.setItemMeta(itemmetaXP);

        ItemStack itemECO = new ItemStack(Material.valueOf(instance.getConfig().getString("Interfaces.Economy Icon")), 1);
        ItemMeta itemmetaECO = itemECO.getItemMeta();
        itemmetaECO.setDisplayName(instance.getLocale().getMessage("interface.spawner.upgradewitheconomy"));
        ArrayList<String> loreECO = new ArrayList<>();
        if (!maxed)
            loreECO.add(instance.getLocale().getMessage("interface.spawner.upgradewitheconomylore", TextComponent.formatEconomy(ecoCost)));
        else
            loreECO.add(instance.getLocale().getMessage("event.upgrade.maxed"));
        itemmetaECO.setLore(loreECO);
        itemECO.setItemMeta(itemmetaECO);

        int nu = 0;
        while (nu != 27) {
            inventory.setItem(nu, Methods.getGlass());
            nu++;
        }
        inventory.setItem(13, item);

        inventory.setItem(0, Methods.getBackgroundGlass(true));
        inventory.setItem(1, Methods.getBackgroundGlass(true));
        inventory.setItem(2, Methods.getBackgroundGlass(false));
        inventory.setItem(6, Methods.getBackgroundGlass(false));
        inventory.setItem(7, Methods.getBackgroundGlass(true));
        inventory.setItem(8, Methods.getBackgroundGlass(true));
        inventory.setItem(9, Methods.getBackgroundGlass(true));
        inventory.setItem(10, Methods.getBackgroundGlass(false));
        inventory.setItem(16, Methods.getBackgroundGlass(false));
        inventory.setItem(17, Methods.getBackgroundGlass(true));
        inventory.setItem(18, Methods.getBackgroundGlass(true));
        inventory.setItem(19, Methods.getBackgroundGlass(true));
        inventory.setItem(20, Methods.getBackgroundGlass(false));
        inventory.setItem(24, Methods.getBackgroundGlass(false));
        inventory.setItem(25, Methods.getBackgroundGlass(true));
        inventory.setItem(26, Methods.getBackgroundGlass(true));

        if (EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Display Help Button In Spawner Overview"))
            addInfo(inventory);

        if (spawner.getSpawnerStacks().size() == 1) {
            if (spawner.getFirstStack().getSpawnerData().isUpgradeable()) {
                if (EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Upgrade With XP"))
                    inventory.setItem(11, itemXP);
                if (EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Upgrade With Economy"))
                    inventory.setItem(15, itemECO);
            }
        }
    }

    @Override
    protected void initClickableObjects() {
        this.registerClickableObject(8, (player, inventory, cursor, slot, type) -> {
            infoPage++;
            addInfo(inventory);
        });

        this.registerClickableObject(13, (player, inventory, cursor, slot, type) -> {
            if (type.isRightClick()) {
                if (spawner.getBoost() == 0) {
                    spawner.playerBoost(player);
                }
            } else if (type.isLeftClick()) {
                if (spawner.getSpawnerStacks().size() == 1) {
                    spawner.convertOverview(player, 1);
                }
            }
        });

        this.registerClickableObject(11, (player, inventory, cursor, slot, type) -> {
            if (instance.getConfig().getBoolean("Main.Upgrade With XP")
                    && !inventory.getItem(slot).getItemMeta().getDisplayName().equals(ChatColor.COLOR_CHAR + "l")) {
                spawner.upgrade(player, CostType.EXPERIENCE);
            }
            spawner.overview(player, 0);
        });

        this.registerClickableObject(15, (player, inventory, cursor, slot, type) -> {
            if (instance.getConfig().getBoolean("Main.Upgrade With Economy")
                    && !inventory.getItem(slot).getItemMeta().getDisplayName().equals(ChatColor.COLOR_CHAR + "l")) {
                spawner.upgrade(player, CostType.ECONOMY);
            }
            spawner.overview(player, 0);
        });
    }

    private void addInfo(Inventory inventory) {
        ItemStack itemO = new ItemStack(Material.PAPER, 1);
        ItemMeta itemmetaO = itemO.getItemMeta();
        itemmetaO.setDisplayName(instance.getLocale().getMessage("interface.spawner.howtotitle"));
        ArrayList<String> loreO = new ArrayList<>();
        String text = EpicSpawnersPlugin.getInstance().getLocale().getMessage("interface.spawner.howtoinfo");

        int start = (14 * infoPage) - 14;
        int li = 1; // 12
        int added = 0;
        boolean max = false;

        String[] parts = text.split("\\|");
        for (String line : parts) {
            line = compileHow(player, line);
            if (line.equals(".") || line.equals("")) {

            } else {
                Pattern regex = Pattern.compile("(.{1,28}(?:\\s|$))|(.{0,28})", Pattern.DOTALL);
                Matcher m = regex.matcher(line);
                while (m.find()) {
                    if (li > start) {
                        if (li < start + 15) {
                            loreO.add(TextComponent.formatText("&7" + m.group()));
                            added++;
                        } else {
                            max = true;
                        }
                    }
                    li++;
                }
            }
        }
        if (added == 0) {
            infoPage = 1;
            addInfo(inventory);
            return;
        }
        if (max) {
            loreO.add(instance.getLocale().getMessage("interface.spawner.howtonext"));
        } else {
            loreO.add(instance.getLocale().getMessage("interface.spawner.howtoback"));
        }
        itemmetaO.setLore(loreO);
        itemO.setItemMeta(itemmetaO);
        inventory.setItem(8, itemO);
    }


    private String compileHow(Player p, String text) {
        try {
            Matcher m = Pattern.compile("\\{(.*?)}").matcher(text);
            while (m.find()) {
                Matcher mi = Pattern.compile("\\[(.*?)]").matcher(text);
                int nu = 0;
                int a = 0;
                String type = "";
                while (mi.find()) {
                    if (nu == 0) {
                        type = mi.group().replace("[", "").replace("]", "");
                        text = text.replace(mi.group(), "");
                    } else {
                        switch (type) {
                            case "LEVELUP":
                                if (nu == 1) {
                                    if (!p.hasPermission("epicspawners.combine." + spawner.getIdentifyingName()) && !p.hasPermission("epicspawners.combine." + spawner.getIdentifyingName())) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                        a++;
                                    }
                                } else if (nu == 2) {
                                    if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Upgrade With XP")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                        a++;
                                    }
                                } else if (nu == 3) {
                                    if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Upgrade With Economy")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                        a++;
                                    }
                                }
                                break;
                            case "WATER":
                                if (nu == 1) {
                                    if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("settings.Spawners-repel-liquid")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                            case "INVSTACK":
                                if (nu == 1) {
                                    if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Allow Stacking Spawners In Survival Inventories")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                            case "REDSTONE":
                                if (nu == 1) {
                                    if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Redstone Power Deactivates Spawners")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                            case "OMNI":
                                if (nu == 1) {
                                    if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.OmniSpawners Enabled")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                            case "DROP":
                                if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Spawner Drops.Allow Killing Mobs To Drop Spawners") || !p.hasPermission("epicspawners.Killcounter")) {
                                    text = "";
                                } else {
                                    text = text.replace("<TYPE>", spawner.getIdentifyingName().toLowerCase());
                                    if (EpicSpawnersPlugin.getInstance().spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(spawner.getIdentifyingName()) + ".CustomGoal") != 0)
                                        text = text.replace("<AMT>", Integer.toString(EpicSpawnersPlugin.getInstance().spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(spawner.getIdentifyingName()) + ".CustomGoal")));
                                    else
                                        text = text.replace("<AMT>", Integer.toString(EpicSpawnersPlugin.getInstance().getConfig().getInt("Spawner Drops.Kills Needed for Drop")));
                                }
                                if (nu == 1) {
                                    if (EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Spawner Drops.Count Unnatural Kills Towards Spawner Drop")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                        }
                    }
                    nu++;
                }

            }
            text = text.replace("[", "").replace("]", "").replace("{", "").replace("}", "");
            return text;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    private String a(int a, String text) {
        try {
            if (a != 0) {
                text = ", " + text;
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return text;
    }
}
