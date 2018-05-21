package com.songoda.epicspawners.api;

import java.time.Duration;

import com.songoda.epicspawners.api.boost.BoostType;
import com.songoda.epicspawners.api.boost.SpawnerBoost;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerManager;
import com.songoda.epicspawners.api.utils.ProtectionPluginHook;
import com.songoda.epicspawners.api.utils.SpawnerDataBuilder;

import org.bukkit.inventory.ItemStack;

/**
 * The main API class for the EpicSpawners plugin. This class will provide various
 * methods to access important features of the plugin's API. For static method
 * wrappers to all methods in this interface, see the {@link EpicSpawnersAPI} class
 */
public interface EpicSpawners {

    /**
     * Get an instance of the {@link SpawnerManager}
     * 
     * @return the spawner manager
     */
    SpawnerManager getSpawnerManager();

    /**
     * Create an item representation of the provided
     * SpawnerData.
     *
     * @param data the SpawnerData for which to create
     *             an item
     * @param amount the amount of items to create
     *
     * @return new spawner item
     *
     * @see SpawnerData#toItemStack(int)
     */
    ItemStack newSpawnerItem(SpawnerData data, int amount);

    /**
     * Create an item representation of the provided
     * SpawnerData.
     *
     * @param data the SpawnerData for which to create
     *             an item
     * @param amount the amount of items to create
     * @param stackSize the amount of stacked spawners
     *
     * @return new spawner item
     *
     * @see SpawnerData#toItemStack(int, int)
     */
    ItemStack newSpawnerItem(SpawnerData data, int amount, int stackSize);

    /**
     * Get the {@link SpawnerData} applicable to this
     * ItemStack.
     *
     * @param item the item from which to retrieve
     *             SpawnerData
     *
     * @return the SpawnerData. null if item is null
     * or has no meta
     */
    SpawnerData getSpawnerDataFromItem(ItemStack item);

    /**
     * Create a new {@link SpawnerDataBuilder} instance
     * to easily construct a new {@link SpawnerData} object
     * 
     * @param identifier the unique spawner data identifier (name)
     * 
     * @return the created SpawnerDataBuilder instance
     */
    SpawnerDataBuilder createSpawnerData(String identifier);

    /**
     * Get the amount of spawners stacked in an ItemStack.
     *
     * @param item the ItemStack to check
     *
     * @return the stack size. 1 if invalid or null.
     */
    int getStackSizeFromItem(ItemStack item);

    /**
     * Register a new {@link ProtectionPluginHook} implementation
     * in order for EpicSpawners to support plugins that protect
     * blocks from being interacted with
     * 
     * @param hook the hook to register
     */
    void registerProtectionHook(ProtectionPluginHook hook);

    /**
     * Create a new {@link SpawnerBoost} given the provided data
     * <p>
     * Each boost type requires different types of data. Due to external
     * dependencies, a specific type is impossible to declare explicitly.
     * For information on what type of object should be passed as data for
     * each type of boost, see the {@link BoostType} documentation. If an
     * incorrect data type is passed, an exception will be thrown
     * 
     * @param type the type of boost to create
     * @param amount the boost amount. Must be positive and non-zero
     * @param data the data for the boost type. This varies depending on
     * the type of boost. See {@link BoostType} for more information
     * 
     * @return the created SpawnerBoost instance. null if unsuccessful
     */
    SpawnerBoost createSpawnerBoost(BoostType type, int amount, Object data);

    /**
     * Create a new {@link SpawnerBoost} given the provided data.
     * The created boost will be temporary according to the specified
     * {@code duration} parameter. The boost will be in effect until
     * the duration of time has surpassed, after which it will be removed
     * from any spawners and will no longer be in effect. The returned
     * SpawnerBoost instance will then be invalidated and no longer usable
     * by the {@link Spawner#addBoost(SpawnerBoost)} method.
     * <p>
     * Each boost type requires different types of data. Due to external
     * dependencies, a specific type is impossible to declare explicitly.
     * For information on what type of object should be passed as data for
     * each type of boost, see the {@link BoostType} documentation. If an
     * incorrect data type is passed, an exception will be thrown
     * 
     * @param type the type of boost to create
     * @param amount the boost amount. Must be positive and non-zero
     * @param data the data for the boost type. This varies depending on
     * the type of boost. See {@link BoostType} for more information
     * @param duration the duration for which this boost will last
     * 
     * @return the created SpawnerBoost instance. null if unsuccessful
     */
    SpawnerBoost createSpawnerBoost(BoostType type, int amount, Object data, Duration duration);

}
