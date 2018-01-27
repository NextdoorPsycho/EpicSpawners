package com.songoda.epicspawners.Spawners;

import com.songoda.arconix.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Lang;
import com.songoda.epicspawners.Utils.Debugger;
import com.songoda.epicspawners.Utils.Methods;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by songoda on 2/24/2017.
 */
public class Spawner {

    Location location = null;
    public String locationStr = null;
    public CreatureSpawner spawner = null;
    public String spawnedType = null;
    public String spawnedTypeU = null;

    private EpicSpawners plugin = EpicSpawners.pl();

    public Spawner() {
    }

    public Spawner(Block b) {
        if (b != null) {
            if (b.getType() == Material.MOB_SPAWNER) {
                String loc = Arconix.pl().serialize().serializeLocation(b);
                defineBlockInformation(loc);
            }
        }
    }

    public Spawner(Location location) {
        if (location.getBlock() != null) {
            if (location.getBlock().getType() == Material.MOB_SPAWNER) {
                String loc = Arconix.pl().serialize().serializeLocation(location.getBlock());
                defineBlockInformation(loc);
            }
        }
    }

    private void defineBlockInformation(String loc) {
        try {
            locationStr = loc;
            location = Arconix.pl().serialize().unserializeLocation(loc);

            spawner = (CreatureSpawner) location.getBlock().getState();

            spawnedType = Methods.getType(spawner.getSpawnedType());
            spawnedTypeU = spawner.getSpawnedType().name();

            if (plugin.dataFile.getConfig().contains("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(location) + ".type")) {
                if (plugin.dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(location) + ".type").equals("OMNI")) {
                    spawnedType = "Omni";
                    spawnedTypeU = "OMNI";
                } else {
                    spawnedType = Methods.getTypeFromString(plugin.dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(location) + ".type"));
                    spawnedTypeU = plugin.dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(location) + ".type");
                }
            }

        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public boolean isSpawningOnFire() {
        return plugin.spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(Methods.getTypeFromString(spawnedType)) + ".Spawn-On-Fire");
    }

    public int getMulti() {
        try {
            return plugin.dataFile.getConfig().getInt("data.spawner." + locationStr);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return 0;
    }

    public int getSpawns() {
        try {
            return plugin.dataFile.getConfig().getInt("data.spawnerstats." + locationStr + ".spawns");
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return 0;
    }

    public void updateDelay() {
        try {
            if (plugin.getConfig().getBoolean("settings.Alter-Delay")) {
                int multi = getMulti();

                if (getMulti() == 0)
                    multi = 1;

                if (plugin.dataFile.getConfig().contains("data.spawnerstats." + locationStr + ".type")) {
                    if (plugin.dataFile.getConfig().getString("data.spawnerstats." + locationStr + ".type").equals("OMNI")) {
                        List<SpawnerItem> list = plugin.getApi().convertFromList(plugin.dataFile.getConfig().getStringList("data.spawnerstats." + locationStr + ".entities"));
                        for (SpawnerItem ent : list) {
                            if (multi > ent.getMulti())
                                multi = ent.getMulti();
                        }
                    }
                }

                String equation = plugin.getConfig().getString("settings.Spawner-Rate-Equation");
                if (getSpawner() != null) {

                    int delay;
                    if (!plugin.cache.containsKey(equation)) {
                        ScriptEngineManager mgr = new ScriptEngineManager();
                        ScriptEngine engine = mgr.getEngineByName("JavaScript");
                        Random rand = new Random();
                        equation = equation.replace("{DEFAULT}", Integer.toString(rand.nextInt(800) + 200));
                        equation = equation.replace("{MULTI}", Integer.toString(multi));
                        delay = (int) Math.round(Double.parseDouble(engine.eval(equation).toString()));
                        plugin.cache.put(equation, delay);
                    } else {
                        delay = plugin.cache.get(equation);
                    }

                    getSpawner().setDelay(delay);
                    update();
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public boolean processCombine(Player p, ItemStack item, ItemStack item2) {
        if (plugin.getConfig().getBoolean("settings.OmniSpawners")) {
            if (plugin.getApi().getIType(item).equals("OMNI") && !plugin.getApi().isOmniBlock(location) || plugin.getApi().getIType(item2).equals("OMNI") && !plugin.getApi().isOmniBlock(location)) {
                if (plugin.getApi().getIType(item).equals("OMNI") && plugin.getApi().getIType(item2).equals("OMNI")) {
                    return false;
                } else {
                    if (plugin.getApi().getType(item).equals("OMNI")) {
                        if (plugin.getApi().addOmniSpawner(new SpawnerItem(plugin.getApi().getType(item2), plugin.getApi().getIMulti(item2)), item) != null) {
                            item2.setItemMeta(plugin.getApi().addOmniSpawner(new SpawnerItem(plugin.getApi().getType(item2), plugin.getApi().getIMulti(item2)), item).getItemMeta());
                            return true;
                        }
                    } else {
                        if (plugin.getApi().addOmniSpawner(new SpawnerItem(plugin.getApi().getType(item), plugin.getApi().getIMulti(item)), item2) != null) {
                            item2.setItemMeta(plugin.getApi().addOmniSpawner(new SpawnerItem(plugin.getApi().getType(item), plugin.getApi().getIMulti(item)), item2).getItemMeta());
                            return true;
                        }
                    }
                    return false;
                }
            }
        }

        int bmulti;
        String btype;
        if (item2 == null) {
            if (plugin.dataFile.getConfig().contains("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(getSpawner().getBlock()) + ".type"))
                btype = plugin.dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(getSpawner().getBlock()) + ".type");
            else
                btype = getSpawner().getSpawnedType().name();
            bmulti = getMulti();
        } else {
            btype = plugin.getApi().getType(item2);
            bmulti = plugin.getApi().getIMulti(item2);
        }

        if (plugin.getApi().isOmniBlock(location)) {
            if (plugin.getConfig().getBoolean("settings.OmniSpawners")) {
                if (plugin.getApi().getIType(item).equals("OMNI")) {
                    p.sendMessage(Arconix.pl().format().formatText(Lang.CANNOT_MERGE_TWO_OMNI.getConfigValue()));
                    return false;
                }
                List<SpawnerItem> spawners = plugin.getApi().convertFromList(plugin.dataFile.getConfig().getStringList("data.spawnerstats." + locationStr + ".entities"));

                if (plugin.getApi().addOmniSpawner(new SpawnerItem(plugin.getApi().getType(item), plugin.getApi().getIMulti(item)), plugin.getApi().newOmniSpawner(spawners)) == null) {
                    p.sendMessage(Arconix.pl().format().formatText(Lang.OMNI_FULL.getConfigValue()));
                } else {
                    plugin.getApi().saveCustomSpawner(plugin.getApi().addOmniSpawner(new SpawnerItem(plugin.getApi().getType(item), plugin.getApi().getIMulti(item)), plugin.getApi().newOmniSpawner(spawners)), location.getBlock());
                    p.sendMessage(Arconix.pl().format().formatText(Lang.ADD_OMNI.getConfigValue()));
                    if (item2 == null) {
                        Methods.takeItem(p, 1);
                    }
                }
            }
        } else {
            int imulti = plugin.getApi().getIMulti(item);
            int newMulti = bmulti + imulti;

            String itype = plugin.getApi().getType(item);

            if (!btype.equals(itype)) {

                if (plugin.getConfig().getBoolean("settings.OmniSpawners")) {
                    if (item2 != null) {
                        item2.setItemMeta(plugin.getApi().newOmniSpawner(new SpawnerItem(btype, bmulti), new SpawnerItem(itype, imulti)).getItemMeta()); //here
                        return true;
                    } else {
                        plugin.getApi().saveCustomSpawner(plugin.getApi().newOmniSpawner(new SpawnerItem(btype, bmulti), new SpawnerItem(itype, imulti)), location.getBlock());
                        plugin.holo.processChange(location.getBlock());
                        Methods.takeItem(p, 1);
                        return false;
                    }
                } else {
                    p.sendMessage(Lang.TYPE_MISMATCH.getConfigValue());
                }
            } else {
                if (newMulti <= plugin.getConfig().getInt("settings.Spawner-max")) {
                    if (p.getGameMode() != GameMode.CREATIVE) {
                        Methods.takeItem(p, 1);
                    }
                    upgradeFinal(p, bmulti, newMulti, item, item2);
                    return true;
                } else {
                    if (bmulti == plugin.getConfig().getInt("settings.Spawner-max")) {
                        p.sendMessage(Lang.MAXED.getConfigValue());
                    } else {
                        if (p.getGameMode() != GameMode.CREATIVE) {
                            Methods.takeItem(p, 1);
                        }
                        int newamt = imulti - (plugin.getConfig().getInt("settings.Spawner-max") - bmulti);
                        ItemStack spawnerItem = new ItemStack(Material.MOB_SPAWNER);
                        ItemMeta itemMeta = spawnerItem.getItemMeta();
                        String name = Methods.compileName(itype, newamt, true);
                        itemMeta.setDisplayName(name);
                        spawnerItem.setItemMeta(itemMeta);
                        p.getInventory().addItem(spawnerItem);
                        upgradeFinal(p, bmulti, plugin.getConfig().getInt("settings.Spawner-max"), item, item2);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void upgrade(Player p, String type) {
        try {
            int multi = getMulti();
            if (multi == 0) {
                multi = 1;
            }
            String typ = spawnedType;


            int cost = getCost(type, typ, multi);

            boolean maxed = false;

            if (multi == plugin.getConfig().getInt("settings.Spawner-max")) {
                maxed = true;
            }
            if (maxed) {
                p.sendMessage(Lang.MAXED.getConfigValue());
            } else {
                if (type == "ECO") {
                    if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
                        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                        net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
                        if (econ.has(p, cost)) {
                            econ.withdrawPlayer(p, cost);
                            upgradeFinal(p, multi, multi + 1,null, null);
                        } else {
                            p.sendMessage(Lang.CANTAFFORD.getConfigValue());
                        }
                    } else {
                        p.sendMessage("Vault is not installed.");
                    }
                } else if (type == "XP") {
                    if (p.getLevel() >= cost || p.getGameMode() == GameMode.CREATIVE) {
                        if (p.getGameMode() != GameMode.CREATIVE) {
                            p.setLevel(p.getLevel() - cost);
                        }
                        upgradeFinal(p, multi, multi + 1, null, null);
                    } else {
                        p.sendMessage(Lang.CANTAFFORD.getConfigValue());
                    }
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public boolean canBreak() {
        if (plugin.getConfig().getBoolean("settings.Only-drop-placed")) {
            if (plugin.dataFile.getConfig().contains("data.spawner." + locationStr)) {
                return true;
            }
            return false;
        }
        return true;
    }

    public boolean canCharge() {
        if (plugin.getConfig().getBoolean("settings.Only-charge-natural")) {
            if (plugin.dataFile.getConfig().contains("data.spawner." + locationStr)) {
                return false;
            }
        }
        return true;
    }

    public void upgradeFinal(Player p, int oldMulti, int multi, ItemStack item, ItemStack item2) {
        try {
            p.sendMessage(Lang.ON_UPGRADE.getConfigValue(Integer.toString(multi)));
            if (item2 == null) {
                plugin.dataFile.getConfig().set("data.spawner." + locationStr, multi);
                Location loc = location.clone();
                loc.setX(loc.getX() + .5);
                loc.setY(loc.getY() + .5);
                loc.setZ(loc.getZ() + .5);
                if (!plugin.v1_8 && !plugin.v1_7) {
                    p.getWorld().spawnParticle(org.bukkit.Particle.valueOf(plugin.getConfig().getString("settings.Upgrade-particle-type")), loc, 200, .5, .5, .5);
                } else {
                    p.getWorld().spigot().playEffect(loc, org.bukkit.Effect.valueOf(plugin.getConfig().getString("settings.Upgrade-particle-type")), 1, 0, (float) 1, (float) 1, (float) 1, 1, 200, 10);
                }
                SpawnerChangeEvent event = new SpawnerChangeEvent(location, p, multi, oldMulti);
                plugin.getServer().getPluginManager().callEvent(event);
                plugin.holo.processChange(location.getBlock());
            } else {
                ItemMeta meta = item.getItemMeta().clone();
                meta.setDisplayName(Methods.compileName(plugin.getApi().getType(item.clone()), multi, true));
                item2.setItemMeta(meta);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public Date getBoostEnd() {
        if (plugin.dataFile.getConfig().contains("data.boosts")) {
            ConfigurationSection cs = plugin.dataFile.getConfig().getConfigurationSection("data.boosts");
            for (String key : cs.getKeys(true)) {
                if (plugin.dataFile.getConfig().contains("data.boosts." + key + ".boosted")) {
                    long endd = plugin.dataFile.getConfig().getLong("data.boosts." + key + ".end");
                    Date end;
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(endd);
                    end = calendar.getTime();
                    return end;
                }
            }
        }
        return null;
    }

    public String getOmniState() {
        if (plugin.dataFile.getConfig().contains("data.spawnerstats." + locationStr + ".OmniState")) {
            return plugin.dataFile.getConfig().getString("data.spawnerstats." + locationStr + ".OmniState");
        }
        return null;
    }

    public void setOmniState(String state) {
        plugin.dataFile.getConfig().set("data.spawnerstats." + locationStr + ".OmniState", state);
    }

    public int getBoost() {
        if (plugin.dataFile.getConfig().contains("data.spawnerstats." + locationStr + ".player")) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(plugin.dataFile.getConfig().getString("data.spawnerstats." + locationStr + ".player")));
            if (plugin.dataFile.getConfig().contains("data.boosts")) {
                ConfigurationSection cs = plugin.dataFile.getConfig().getConfigurationSection("data.boosts");
                for (String key : cs.getKeys(false)) {
                    if (plugin.dataFile.getConfig().contains("data.boosts." + key + ".boosted")) {

                        int boost = plugin.dataFile.getConfig().getInt("data.boosts." + key + ".boosted");

                        Date today = new Date();
                        Date end = getBoostEnd();
                        if (today.after(end)) {
                            plugin.dataFile.getConfig().set("data.boosts." + key, null);
                        } else {

                            boolean go = false;
                            if (plugin.dataFile.getConfig().contains("data.boosts." + key + ".player")) {
                                OfflinePlayer p2 = Bukkit.getOfflinePlayer(UUID.fromString(plugin.dataFile.getConfig().getString("data.boosts." + key + ".player")));
                                if (p2.equals(p)) {
                                    go = true;
                                }
                            } else if (plugin.dataFile.getConfig().contains("data.boosts." + key + ".faction")) {
                                String id = plugin.dataFile.getConfig().getString("data.boosts." + key + ".faction");
                                if (plugin.hooks.isInFaction(id, location)) {
                                    go = true;
                                }
                            } else if (plugin.dataFile.getConfig().contains("data.boosts." + key + ".town")) {
                                String id = plugin.dataFile.getConfig().getString("data.boosts." + key + ".town");
                                if (plugin.hooks.isInTown(id, location)) {
                                    go = true;
                                }
                            } else if (plugin.dataFile.getConfig().contains("data.boosts." + key + ".island")) {
                                String id = plugin.dataFile.getConfig().getString("data.boosts." + key + ".island");
                                if (plugin.hooks.isInIsland(id, location)) {
                                    go = true;
                                }
                            } else if (plugin.dataFile.getConfig().contains("data.boosts." + key + ".location")) {
                                String loc = plugin.dataFile.getConfig().getString("data.boosts." + key + ".location");
                                if (loc.equals(locationStr)) {
                                    go = true;
                                }
                            }
                            if (go) {
                                return boost;
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }

    public void downgradeFinal(Player p, int multi, int oldMulti, String type) {
        try {
            plugin.holo.processChange(location.getBlock());
            if (multi >= 1) {
                plugin.dataFile.getConfig().set("data.spawner." + locationStr, multi);

                if (type.equals("Omni")) {
                        List<SpawnerItem> spawners = plugin.getApi().convertFromList(plugin.dataFile.getConfig().getStringList("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(location) + ".entities"));
                        List<SpawnerItem> items = plugin.getApi().getOmniList(plugin.getApi().newOmniSpawner(spawners));
                        List<ItemStack> items2 = plugin.getApi().removeOmni(plugin.getApi().newOmniSpawner(spawners));
                        if (items.size() != 0) {
                            p.sendMessage(Lang.OMNI_TAKE.getConfigValue(Methods.compileName(items.get(items.size() - 1).getType(), items.get(items.size() - 1).getMulti(), true)));
                            plugin.getApi().clearOmni(location);
                            plugin.dataFile.getConfig().set("data.spawner." + Arconix.pl().serialize().serializeLocation(location), plugin.getApi().getIMulti(items2.get(1)));


                            boolean isCustom = false;
                            try {
                                setSpawner(EntityType.valueOf(type));
                            } catch (Exception ex) {
                                isCustom = true;
                            }
                            if (isCustom) {
                                setSpawner(EntityType.valueOf("DROPPED_ITEM"));
                                plugin.dataFile.getConfig().set("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(location) + ".type", plugin.getApi().getIType(items2.get(1)));
                            } else {
                                getSpawner().setSpawnedType(EntityType.valueOf(plugin.getApi().getIType(items2.get(1))));
                            }
                            spawner.update();
                        }
                    } else {
                        p.sendMessage(Lang.ON_DOWNGRADE.getConfigValue(Integer.toString(oldMulti)));
                }
            } else {
                if (plugin.getConfig().getBoolean("settings.Alert-place-break")) {

                    p.sendMessage(Lang.BREAK.getConfigValue(Methods.compileName(type, oldMulti, true)));
                }
                plugin.dataFile.getConfig().set("data.spawnerstats." + locationStr, null);
                plugin.dataFile.getConfig().set("data.spawner." + locationStr, null);

                Location nloc = location.clone();
                nloc.add(.5, -.4, .5);

                List<Entity> near = (List<Entity>) nloc.getWorld().getNearbyEntities(nloc, 8, 8, 8);
                for (Entity e : near) {
                    if (e.getLocation().getX() == nloc.getX() && e.getLocation().getY() == nloc.getY() && e.getLocation().getZ() == nloc.getZ()) {
                        e.remove();
                    }
                }
            }

            SpawnerChangeEvent event = new SpawnerChangeEvent(location, p, multi, oldMulti);
            plugin.getServer().getPluginManager().callEvent(event);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public int getCost(String type, String entity, int multi) {
        try {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");

            int cost = 0;
            if (type == "ECO") {
                if (plugin.spawnerFile.getConfig().getInt("Entities." + entity + ".Custom-ECO-Cost") != 0)
                    cost = plugin.spawnerFile.getConfig().getInt("Entities." + entity + ".Custom-ECO-Cost");
                else
                    cost = plugin.getConfig().getInt("settings.Upgrade-eco-cost");
                if (plugin.getConfig().getBoolean("settings.Use-equations")) {
                    String math = plugin.getConfig().getString("settings.ECO-cost-equation").replace("{ECOCost}", Integer.toString(cost)).replace("{Level}", Integer.toString(multi));
                    cost = (int) Math.round(Double.parseDouble(engine.eval(math).toString()));
                }
            } else if (type == "XP") {
                if (plugin.spawnerFile.getConfig().getInt("Entities." + entity + ".Custom-XP-Cost") != 0)
                    cost = plugin.spawnerFile.getConfig().getInt("Entities." + entity + ".Custom-XP-Cost");
                else
                    cost = plugin.getConfig().getInt("settings.Upgrade-xp-cost");
                if (plugin.getConfig().getBoolean("settings.Use-equations")) {
                    String math = plugin.getConfig().getString("settings.XP-cost-equation").replace("{XPCost}", Integer.toString(cost)).replace("{Level}", Integer.toString(multi));
                    cost = (int) Math.round(Double.parseDouble(engine.eval(math).toString()));
                }
            }
            return cost;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return 999999999;
    }

    public void view(Player p, int infPage) {
        try {
            if (p.hasPermission("epicspawners.view")) {
                int multi = getMulti();
                int spawns = getSpawns();
                String type = spawnedType;

                Inventory i = Bukkit.createInventory(null, 27, Arconix.pl().format().formatTitle(Methods.compileName(type, multi, false)));


                int showAmt = multi;
                if (showAmt > 64)
                    showAmt = 1;
                else if (showAmt == 0)
                    showAmt = 1;

                ItemStack item = new ItemStack(Material.SKULL_ITEM, showAmt, (byte) 3);
                try {
                    item = plugin.heads.addTexture(item, type);
                } catch (Exception e) {
                    item = new ItemStack(Material.MOB_SPAWNER, showAmt);
                }

                ItemMeta itemmeta = item.getItemMeta();
                itemmeta.setDisplayName(Lang.STATSTITLE.getConfigValue());
                ArrayList<String> lore = new ArrayList<>();

                String spawnBlocks = plugin.spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(Methods.getTypeFromString(type)) + ".Spawn-Block");

                List<String> blocks = Arrays.asList(spawnBlocks.split("\\s*,\\s*"));

                String only = blocks.get(0);

                int num = 1;
                for (String block : blocks) {
                    if (num != 1)
                        only += "&8, &6" + Methods.getTypeFromString(block);
                    num++;
                }

                lore.add(Arconix.pl().format().formatText(Lang.ONLY_SPAWNS.getConfigValue(only)));

                boolean omni = false;
                if (plugin.dataFile.getConfig().contains("data.spawnerstats." + locationStr + ".type")) {
                    if (plugin.dataFile.getConfig().getString("data.spawnerstats." + locationStr + ".type").equals("OMNI")) {
                        List<SpawnerItem> list = plugin.getApi().convertFromList(plugin.dataFile.getConfig().getStringList("data.spawnerstats." + locationStr + ".entities"));
                        lore.add(Arconix.pl().format().formatText(plugin.getApi().getOmniString(list)));
                        omni = true;
                    }
                }
                lore.add(Lang.STATSSPAWNS.getConfigValue(Integer.toString(spawns), type));
                if (p.hasPermission("epicspawners.convert") && !omni) {
                    lore.add("");
                    lore.add(Arconix.pl().format().formatText(Lang.CLICK_CONVERT.getConfigValue()));
                }
                if (p.hasPermission("epicspawners.canboost")) {
                    if (getBoost() == 0) {
                        if (!p.hasPermission("epicspawners.convert." + Methods.getTypeFromString(type))) {
                            lore.add("");
                        }
                        lore.add(Arconix.pl().format().formatText(Lang.CLICK_BOOST.getConfigValue()));
                    }
                }
                if (getBoost() != 0) {

                    Date today = new Date();

                    String[] parts = Lang.STATSBOOSTED.getConfigValue(Integer.toString(getBoost()), type, Arconix.pl().format().readableTime(getBoostEnd().getTime() - today.getTime())).split("\\|");
                    lore.add("");
                    for (String line : parts)
                        lore.add(Arconix.pl().format().formatText(line));
                }
                itemmeta.setLore(lore);
                item.setItemMeta(itemmeta);

                int xpCost = getCost("XP", type, multi);

                int ecoCost = getCost("ECO", type, multi);

                boolean maxed = false;
                if (multi == plugin.getConfig().getInt("settings.Spawner-max"))
                    maxed = true;

                ItemStack itemXP = new ItemStack(Material.valueOf(plugin.getConfig().getString("settings.XP-Icon")), 1);
                ItemMeta itemmetaXP = itemXP.getItemMeta();
                itemmetaXP.setDisplayName(Lang.XPTITLE.getConfigValue());
                ArrayList<String> loreXP = new ArrayList<>();
                if (!maxed)
                    loreXP.add(Lang.XPLORE.getConfigValue(Integer.toString(xpCost)));
                else
                    loreXP.add(Lang.MAXED.getConfigValue());
                itemmetaXP.setLore(loreXP);
                itemXP.setItemMeta(itemmetaXP);

                ItemStack itemECO = new ItemStack(Material.valueOf(plugin.getConfig().getString("settings.ECO-Icon")), 1);
                ItemMeta itemmetaECO = itemECO.getItemMeta();
                itemmetaECO.setDisplayName(Lang.ECOTITLE.getConfigValue());
                ArrayList<String> loreECO = new ArrayList<>();
                if (!maxed)
                    loreECO.add(Lang.ECOLORE.getConfigValue(Arconix.pl().format().formatEconomy(ecoCost)));
                else
                    loreECO.add(Lang.MAXED.getConfigValue());
                itemmetaECO.setLore(loreECO);
                itemECO.setItemMeta(itemmetaECO);

                int nu = 0;
                while (nu != 27) {
                    i.setItem(nu, Methods.getGlass());
                    nu++;
                }
                i.setItem(13, item);

                i.setItem(0, Methods.getBackgroundGlass(true));
                i.setItem(1, Methods.getBackgroundGlass(true));
                i.setItem(2, Methods.getBackgroundGlass(false));
                i.setItem(6, Methods.getBackgroundGlass(false));
                i.setItem(7, Methods.getBackgroundGlass(true));
                i.setItem(8, Methods.getBackgroundGlass(true));
                i.setItem(9, Methods.getBackgroundGlass(true));
                i.setItem(10, Methods.getBackgroundGlass(false));
                i.setItem(16, Methods.getBackgroundGlass(false));
                i.setItem(17, Methods.getBackgroundGlass(true));
                i.setItem(18, Methods.getBackgroundGlass(true));
                i.setItem(19, Methods.getBackgroundGlass(true));
                i.setItem(20, Methods.getBackgroundGlass(false));
                i.setItem(24, Methods.getBackgroundGlass(false));
                i.setItem(25, Methods.getBackgroundGlass(true));
                i.setItem(26, Methods.getBackgroundGlass(true));

                if (plugin.getConfig().getBoolean("settings.How-to")) {
                    ItemStack itemO = new ItemStack(Material.PAPER, 1);
                    ItemMeta itemmetaO = itemO.getItemMeta();
                    itemmetaO.setDisplayName(Arconix.pl().format().formatText(Lang.SPAWNER_INFO_TITLE.getConfigValue()));
                    ArrayList<String> loreO = new ArrayList<>();
                    String text = Lang.SPAWNER_INFO.getConfigValue();

                    int start = (14 * infPage) - 14;
                    int li = 1; // 12
                    int added = 0;
                    boolean max = false;

                    String[] parts = text.split("\\|");
                    for (String line : parts) {
                        line = compileHow(p, line);
                        if (line.equals(".") || line.equals("")) {

                        } else {
                            Pattern regex = Pattern.compile("(.{1,28}(?:\\s|$))|(.{0,28})", Pattern.DOTALL);
                            Matcher m = regex.matcher(line);
                            while (m.find()) {
                                if (li > start) {
                                    if (li < start + 15) {
                                        loreO.add(Arconix.pl().format().formatText("&7" + m.group()));
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
                        view(p, 1);
                        plugin.infPage.remove(p);
                        return;
                    }
                    if (max) {
                        loreO.add(Arconix.pl().format().formatText(Lang.SPAWNER_INFO_NEXT.getConfigValue()));
                    } else {
                        loreO.add(Arconix.pl().format().formatText(Lang.SPAWNER_INFO_BACK.getConfigValue()));
                    }
                    itemmetaO.setLore(loreO);
                    itemO.setItemMeta(itemmetaO);
                    i.setItem(8, itemO);
                }
                if (!type.equals("Omni")) {
                    if (plugin.spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(Methods.getTypeFromString(type)) + ".Upgradable")) {
                        if (plugin.getConfig().getBoolean("settings.Upgrade-with-xp"))
                            i.setItem(11, itemXP);
                        if (plugin.getConfig().getBoolean("settings.Upgrade-with-eco"))
                            i.setItem(15, itemECO);
                    }
                }
                p.openInventory(i);
                plugin.spawnerLoc.put(p, spawner.getBlock());
                plugin.lastSpawner.put(p, spawner.getBlock());
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public String compileHow(Player p, String text) {
        Matcher m = Pattern.compile("\\{(.*?)\\}").matcher(text);
        while (m.find()) {
            Matcher mi = Pattern.compile("\\[(.*?)\\]").matcher(text);
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
                                if (!p.hasPermission("epicspawners.combine." + spawnedType)) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                    a++;
                                }
                            } else if (nu == 2) {
                                if (!plugin.getConfig().getBoolean("settings.Upgrade-with-xp")) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                    a++;
                                }
                            } else if (nu == 3) {
                                if (!plugin.getConfig().getBoolean("settings.Upgrade-with-eco")) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                    a++;
                                }
                            }
                            break;
                        case "WATER":
                            if (nu == 1) {
                                if (!plugin.getConfig().getBoolean("settings.spawners-repel-liquid")) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                }
                            }
                            break;
                        case "INVSTACK":
                            if (nu == 1) {
                                if (!plugin.getConfig().getBoolean("settings.Inventory-Stacking")) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                }
                            }
                            break;
                        case "REDSTONE":
                            if (nu == 1) {
                                if (!plugin.getConfig().getBoolean("settings.redstone-activate")) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                }
                            }
                            break;
                        case "OMNI":
                            if (nu == 1) {
                                if (!plugin.getConfig().getBoolean("settings.OmniSpawners")) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                }
                            }
                            break;
                        case "DROP":
                            if (!plugin.getConfig().getBoolean("settings.Mob-kill-counting") || !p.hasPermission("epicspawners.Killcounter")) {
                                text = "";
                            } else {
                                text = text.replace("<TYPE>", spawnedType.toLowerCase());
                                if (plugin.spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(spawnedType) + ".CustomGoal") != 0)
                                    text = text.replace("<AMT>", Integer.toString(plugin.spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(spawnedType) + ".CustomGoal")));
                                else
                                    text = text.replace("<AMT>", Integer.toString(plugin.getConfig().getInt("settings.Goal")));
                            }
                            if (nu == 1) {
                                if (plugin.getConfig().getBoolean("settings.Count-unnatural-kills")) {
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
    }

    public String a(int a, String text) {
        if (a != 0) {
            text = ", " + text;
        }
        return text;
    }

    public void change(Player p, int page) {
        try {
            plugin.page.put(p, page);

            List<String> entities = new ArrayList<>();

            int num = 0;
            int show = 0;
            int start = (page - 1) * 32;
            ConfigurationSection cs = plugin.spawnerFile.getConfig().getConfigurationSection("Entities");
            for (String value : cs.getKeys(false)) {
                if (!value.toLowerCase().equals("omni")) {
                    if (plugin.spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(value) + ".Convertible")) {
                        if (p.hasPermission("epicspawners.*") || p.hasPermission("epicspawners.convert.*") || p.hasPermission("epicspawners.convert." + Methods.getTypeFromString(value).replaceAll(" ", "_"))) {
                            if (num >= start) {
                                if (show <= 32) {
                                    entities.add(value);
                                    show++;
                                }
                            }
                        }
                        num++;
                    }
                }
            }

            int amt = entities.size();
            Inventory i = Bukkit.createInventory(null, 54, Arconix.pl().format().formatTitle(Lang.SPAWNER_CONVERT.getConfigValue()));
            int max2 = 54;
            if (amt <= 7) {
                i = Bukkit.createInventory(null, 27, Arconix.pl().format().formatTitle(Lang.SPAWNER_CONVERT.getConfigValue()));
                max2 = 27;
            } else if (amt <= 15) {
                i = Bukkit.createInventory(null, 36, Arconix.pl().format().formatTitle(Lang.SPAWNER_CONVERT.getConfigValue()));
                max2 = 36;
            } else if (amt <= 25) {
                i = Bukkit.createInventory(null, 45, Arconix.pl().format().formatTitle(Lang.SPAWNER_CONVERT.getConfigValue()));
                max2 = 45;
            }

            final int max22 = max2;
            int place = 10;
            for (String value : entities) {
                if (place == 17)
                    place++;
                if (place == (max22 - 18))
                    place++;
                ItemStack it = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);

                ItemStack item = plugin.heads.addTexture(it, Methods.getTypeFromString(value));

                ItemMeta itemmeta = item.getItemMeta();
                String name = Methods.compileName(value, 0, true);
                ArrayList<String> lore = new ArrayList<>();
                String per = plugin.spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(value) + ".Convert-Price");
                double sprice = plugin.spawnerFile.getConfig().getDouble("Entities." + Methods.getTypeFromString(value) + ".Shop-Price");

                int ch = Integer.parseInt(per.replace("%", ""));

                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");
                double price = Math.round(Double.parseDouble(engine.eval("(" + ch + " / 100) * " + sprice).toString()) * getMulti());

                lore.add(Arconix.pl().format().formatText(Lang.BUY_PRICE.getConfigValue(Arconix.pl().format().formatEconomy(price))));
                String loreString = Lang.CONVERT_LORE.getConfigValue(Methods.getTypeFromString(Methods.getTypeFromString(value)));
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    loreString = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, loreString.replace(" ", "_")).replace("_", " ");
                }
                lore.add(loreString);
                itemmeta.setLore(lore);
                itemmeta.setDisplayName(name);
                item.setItemMeta(itemmeta);
                i.setItem(place, item);
                place++;
            }

            int max = (int) Math.ceil((double) num / (double) 36);
            num = 0;
            while (num != 9) {
                i.setItem(num, Methods.getGlass());
                num++;
            }
            int num2 = max2 - 9;
            while (num2 != max2) {
                i.setItem(num2, Methods.getGlass());
                num2++;
            }

            ItemStack exit = new ItemStack(Material.valueOf(plugin.getConfig().getString("settings.Exit-Icon")), 1);
            ItemMeta exitmeta = exit.getItemMeta();
            exitmeta.setDisplayName(Lang.EXIT.getConfigValue());
            exit.setItemMeta(exitmeta);

            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            ItemStack skull = head;
            if (!plugin.v1_7)
                skull = Arconix.pl().getGUI().addTexture(head, "http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b");
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            if (plugin.v1_7)
                skullMeta.setOwner("MHF_ArrowRight");
            skull.setDurability((short) 3);
            skullMeta.setDisplayName(Lang.NEXT.getConfigValue());
            skull.setItemMeta(skullMeta);

            ItemStack head2 = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            ItemStack skull2 = head2;
            if (!plugin.v1_7)
                skull2 = Arconix.pl().getGUI().addTexture(head2, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");
            SkullMeta skull2Meta = (SkullMeta) skull2.getItemMeta();
            if (plugin.v1_7)
                skull2Meta.setOwner("MHF_ArrowLeft");
            skull2.setDurability((short) 3);
            skull2Meta.setDisplayName(Lang.BACK.getConfigValue());
            skull2.setItemMeta(skull2Meta);

            i.setItem(8, exit);

            i.setItem(0, Methods.getBackgroundGlass(true));
            i.setItem(1, Methods.getBackgroundGlass(true));
            i.setItem(9, Methods.getBackgroundGlass(true));

            i.setItem(7, Methods.getBackgroundGlass(true));
            i.setItem(17, Methods.getBackgroundGlass(true));

            i.setItem(max22 - 18, Methods.getBackgroundGlass(true));
            i.setItem(max22 - 9, Methods.getBackgroundGlass(true));
            i.setItem(max22 - 8, Methods.getBackgroundGlass(true));

            i.setItem(max22 - 10, Methods.getBackgroundGlass(true));
            i.setItem(max22 - 2, Methods.getBackgroundGlass(true));
            i.setItem(max22 - 1, Methods.getBackgroundGlass(true));

            i.setItem(2, Methods.getBackgroundGlass(false));
            i.setItem(6, Methods.getBackgroundGlass(false));
            i.setItem(max22 - 7, Methods.getBackgroundGlass(false));
            i.setItem(max22 - 3, Methods.getBackgroundGlass(false));

            if (page != 1) {
                i.setItem(max22 - 8, skull2);
            }
            if (page != max) {
                i.setItem(max22 - 2, skull);
            }

            p.openInventory(i);
            plugin.change.add(p);
            plugin.spawnerLoc.put(p, spawner.getBlock());
            plugin.lastSpawner.put(p, spawner.getBlock());
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }


    public void convert(String type, Player p) {
        try {
            if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
                p.sendMessage("Vault is not installed.");
            } else {
                RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                net.milkbowl.vault.economy.Economy econ = rsp.getProvider();

                String per = plugin.spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(type) + ".Convert-Price");
                double sprice = plugin.spawnerFile.getConfig().getDouble("Entities." + Methods.getTypeFromString(type) + ".Shop-Price");

                int ch = Integer.parseInt(per.replace("%", ""));

                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");
                double price = Math.round(Double.parseDouble(engine.eval("(" + ch + " / 100) * " + sprice).toString())) * getMulti();

                if (econ.has(p, price) || p.isOp()) {
                    plugin.dataFile.getConfig().set("data.spawnerstats." + locationStr + ".type", Methods.getTypeFromString(type));
                    try {
                        getSpawner().setSpawnedType(EntityType.valueOf(type));
                        update();
                    } catch (Exception e) {
                    }

                    p.sendMessage(plugin.references.getPrefix() + Lang.CONVERT_SUCCESS.getConfigValue());

                    plugin.holo.processChange(location.getBlock());
                    p.closeInventory();
                    if (!p.isOp()) {
                        econ.withdrawPlayer(p, price);
                    }
                } else {
                    p.sendMessage(plugin.references.getPrefix() + Lang.CANNOT_AFFORD.getConfigValue());
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void playerBoost(Player p) {
        if (p.hasPermission("epicspawners.canboost")) {
            if (plugin.boostAmt.containsKey(p)) {
                if (plugin.boostAmt.get(p) > plugin.getConfig().getInt("settings.Max-Player-Boost")) {
                    plugin.boostAmt.put(p, plugin.getConfig().getInt("settings.Max-Player-Boost"));
                    return;
                } else if (plugin.boostAmt.get(p) < 1) {
                    plugin.boostAmt.put(p, 1);
                }
            }

            int amt = 1;

            if (plugin.boostAmt.containsKey(p))
                amt = plugin.boostAmt.get(p);
            else
                plugin.boostAmt.put(p, amt);


            int multi = plugin.dataFile.getConfig().getInt("data.spawner." + locationStr);

            String type = Methods.getType(spawner.getSpawnedType());

            if (plugin.dataFile.getConfig().contains("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(location) + ".type")) {
                if (plugin.dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(location) + ".type").equals("OMNI"))
                    type = "Omni";
                else
                    type = plugin.dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(location) + ".type");
            }

            Inventory i = Bukkit.createInventory(null, 27, Lang.BOOST_TITLE.getConfigValue(Integer.toString(amt), Methods.compileName(type, multi, false)));

            int num = 0;
            while (num != 27) {
                i.setItem(num, Methods.getGlass());
                num++;
            }

            ItemStack coal = new ItemStack(Material.COAL);
            ItemMeta coalMeta = coal.getItemMeta();
            coalMeta.setDisplayName(Arconix.pl().format().formatText(Lang.BOOST_FOR.getConfigValue("5")));
            ArrayList<String> coalLore = new ArrayList<>();
            coalLore.add(Arconix.pl().format().formatText("&7Costs &6&l" + Methods.getBoostCost(5, amt) + "."));
            coalMeta.setLore(coalLore);
            coal.setItemMeta(coalMeta);

            ItemStack iron = new ItemStack(Material.IRON_INGOT);
            ItemMeta ironMeta = iron.getItemMeta();
            ironMeta.setDisplayName(Arconix.pl().format().formatText(Lang.BOOST_FOR.getConfigValue("15")));
            ArrayList<String> ironLore = new ArrayList<>();
            ironLore.add(Arconix.pl().format().formatText("&7Costs &6&l" + Methods.getBoostCost(15, amt) + "."));
            ironMeta.setLore(ironLore);
            iron.setItemMeta(ironMeta);

            ItemStack diamond = new ItemStack(Material.DIAMOND);
            ItemMeta diamondMeta = diamond.getItemMeta();
            diamondMeta.setDisplayName(Arconix.pl().format().formatText(Lang.BOOST_FOR.getConfigValue("30")));
            ArrayList<String> diamondLore = new ArrayList<>();
            diamondLore.add(Arconix.pl().format().formatText("&7Costs &6&l" + Methods.getBoostCost(30, amt) + "."));
            diamondMeta.setLore(diamondLore);
            diamond.setItemMeta(diamondMeta);

            ItemStack emerald = new ItemStack(Material.EMERALD);
            ItemMeta emeraldMeta = emerald.getItemMeta();
            emeraldMeta.setDisplayName(Arconix.pl().format().formatText(Lang.BOOST_FOR.getConfigValue("60")));
            ArrayList<String> emeraldLore = new ArrayList<>();
            emeraldLore.add(Arconix.pl().format().formatText("&7Costs &6&l" + Methods.getBoostCost(60, amt) + "."));
            emeraldMeta.setLore(emeraldLore);
            emerald.setItemMeta(emeraldMeta);

            i.setItem(10, coal);
            i.setItem(12, iron);
            i.setItem(14, diamond);
            i.setItem(16, emerald);

            i.setItem(0, Methods.getBackgroundGlass(true));
            i.setItem(1, Methods.getBackgroundGlass(true));
            i.setItem(2, Methods.getBackgroundGlass(false));
            i.setItem(6, Methods.getBackgroundGlass(false));
            i.setItem(7, Methods.getBackgroundGlass(true));
            i.setItem(8, Methods.getBackgroundGlass(true));
            i.setItem(9, Methods.getBackgroundGlass(true));
            i.setItem(17, Methods.getBackgroundGlass(true));
            i.setItem(18, Methods.getBackgroundGlass(true));
            i.setItem(19, Methods.getBackgroundGlass(true));
            i.setItem(20, Methods.getBackgroundGlass(false));
            i.setItem(24, Methods.getBackgroundGlass(false));
            i.setItem(25, Methods.getBackgroundGlass(true));
            i.setItem(26, Methods.getBackgroundGlass(true));

            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            ItemStack skull = head;
            if (!plugin.v1_7)
                skull = Arconix.pl().getGUI().addTexture(head, "http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b");
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            if (plugin.v1_7)
                skullMeta.setOwner("MHF_ArrowRight");
            skull.setDurability((short) 3);
            skullMeta.setDisplayName(Arconix.pl().format().formatText("&6&l+1"));
            skull.setItemMeta(skullMeta);

            ItemStack head2 = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            ItemStack skull2 = head2;
            if (!plugin.v1_7)
                skull2 = Arconix.pl().getGUI().addTexture(head2, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");
            SkullMeta skull2Meta = (SkullMeta) skull2.getItemMeta();
            if (plugin.v1_7)
                skullMeta.setOwner("MHF_ArrowLeft");
            skull2.setDurability((short) 3);
            skull2Meta.setDisplayName(Arconix.pl().format().formatText("&6&l-1"));
            skull2.setItemMeta(skull2Meta);

            if (amt != 1) {
                i.setItem(0, skull2);
            }
            if (amt < plugin.getConfig().getInt("settings.Max-Player-Boost")) {
                i.setItem(8, skull);
            }

            p.openInventory(i);
            plugin.boosting.add(p);
        }
    }

    public void purchaseBoost(Player p, int time) {
        int amt = plugin.boostAmt.get(p);
        boolean yes = false;

        String un = plugin.getConfig().getString("settings.Boost-cost");

        String[] parts = un.split(":");

        String type = parts[0];
        String multi = parts[1];
        int cost = Methods.boostCost(multi, time, amt);
        if (!type.equals("ECO") && !type.equals("XP")) {
            ItemStack stack = new ItemStack(Material.valueOf(type));
            int invAmt = Arconix.pl().getGUI().getAmount(p.getInventory(), stack);
            if (invAmt >= cost) {
                stack.setAmount(cost);
                Arconix.pl().getGUI().removeFromInventory(p.getInventory(), stack);
                yes = true;
            } else {
                p.sendMessage(Lang.CANTAFFORD.getConfigValue());
            }
        } else if (type.equals("ECO")) {
            if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
                RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
                if (econ.has(p, cost)) {
                    econ.withdrawPlayer(p, cost);
                    yes = true;
                } else {
                    p.sendMessage(Lang.CANTAFFORD.getConfigValue());
                }
            } else {
                p.sendMessage("Vault is not installed.");
            }
        } else if (type.equals("XP")) {
            if (p.getLevel() >= cost || p.getGameMode() == GameMode.CREATIVE) {
                if (p.getGameMode() != GameMode.CREATIVE) {
                    p.setLevel(p.getLevel() - cost);
                }
                yes = true;
            } else {
                p.sendMessage(Lang.CANTAFFORD.getConfigValue());
            }
        }
        if (yes) {
            Calendar c = Calendar.getInstance();
            Date currentDate = new Date();
            c.setTime(currentDate);
            c.add(Calendar.MINUTE, time);

            String uuid = UUID.randomUUID().toString();
            plugin.dataFile.getConfig().set("data.boosts." + uuid + ".location", locationStr);
            plugin.dataFile.getConfig().set("data.boosts." + uuid + ".boosted", amt);

            plugin.dataFile.getConfig().set("data.boosts." + uuid + ".end", c.getTime().getTime());
            p.sendMessage(Arconix.pl().format().formatText(Lang.BOOST_APPLIED.getConfigValue()));
        }
        p.closeInventory();
    }

    public void setSpawner(EntityType ent) {
        try {
            spawner = ((CreatureSpawner) spawner.getBlock().getState());
            spawner.setSpawnedType(ent);
            update();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public CreatureSpawner getSpawner() {
        return spawner;
    }

    public void update() {
        spawner.update();
    }
}
