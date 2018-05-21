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
 * The access point of the EpicSpawnersAPI, a class acting as a bridge between API
 * and plugin implementation. It is from here where developers should access the
 * important and core methods in the API. All static methods in this class will
 * call directly upon the implementation at hand (in most cases this will be the
 * EpicSpawners plugin itself), therefore a call to {@link #getImplementation()} is
 * not required and redundant in most situations. Method calls from this class are
 * preferred the majority of time, though an instance of {@link EpicSpawners} may
 * be passed if absolutely necessary.
 * 
 * @see EpicSpawners
 * @since 5.0.0
 */
public class EpicSpawnersAPI {

    private static EpicSpawners implementation;

    /**
     * Set the EpicSpawners implementation. Once called for the first time, this
     * method will throw an exception on any subsequent invocations. The implementation
     * may only be set a single time, presumably by the EpicSpawners plugin
     * 
     * @param implementation the implementation to set
     */
    public static void setImplementation(EpicSpawners implementation) {
        if (EpicSpawnersAPI.implementation != null) {
            throw new IllegalArgumentException("Cannot set API implementation twice");
        }

        EpicSpawnersAPI.implementation = implementation;
    }

    /**
     * Get the EpicSpawners implementation. This method may be redundant in most
     * situations as all methods present in {@link EpicSpawners} will be mirrored
     * with static modifiers in the {@link EpicSpawnersAPI} class
     * 
     * @return the EpicSpawners implementation
     */
    public static EpicSpawners getImplementation() {
        return implementation;
    }

    /**
     * Get an instance of the {@link SpawnerManager}
     * 
     * @return the spawner manager
     */
    public static SpawnerManager getSpawnerManager() {
        return implementation.getSpawnerManager();
    }

    /**
     * Create an item representation of the provided SpawnerData.
     * 
     * @param data the SpawnerData for which to create an item
     * @param amount the amount of items to create
     * 
     * @return new spawner item
     * 
     * @see SpawnerData#toItemStack(int)
     */
    public static ItemStack newSpawnerItem(SpawnerData data, int amount) {
        return implementation.newSpawnerItem(data, amount);
    }

    /**
     * Create an item representation of the provided SpawnerData.
     * 
     * @param data the SpawnerData for which to create an item
     * @param amount the amount of items to create
     * @param stackSize the amount of stacked spawners
     * 
     * @return new spawner item
     * 
     * @see SpawnerData#toItemStack(int, int)
     */
    public static ItemStack newSpawnerItem(SpawnerData data, int amount, int stackSize) {
        return implementation.newSpawnerItem(data, amount, stackSize);
    }

    /**
     * Get the {@link SpawnerData} applicable to this ItemStack.
     * 
     * @param item the item from which to retrieve SpawnerData
     * 
     * @return the SpawnerData. null if item is null or has no meta
     */
    public static SpawnerData getSpawnerDataFromItem(ItemStack item) {
        return implementation.getSpawnerDataFromItem(item);
    }

    /**
     * Create a new {@link SpawnerDataBuilder} instance to easily
     * construct a new {@link SpawnerData} object
     * 
     * @param identifier the unique spawner data identifier (name)
     * 
     * @return the created SpawnerDataBuilder instance
     */
    public static SpawnerDataBuilder createSpawnerData(String identifier) {
        return implementation.createSpawnerData(identifier);
    }

    /**
     * Get the amount of spawners stacked in an ItemStack.
     * 
     * @param item the ItemStack to check
     * 
     * @return the stack size. 1 if invalid or null.
     */
    public static int getStackSizeFromItem(ItemStack item) {
        return implementation.getStackSizeFromItem(item);
    }
    
    /**
     * Register a new {@link ProtectionPluginHook} implementation
     * in order for EpicSpawners to support plugins that protect
     * blocks from being interacted with
     * 
     * @param hook the hook to register
     */
    public static void registerProtectionHook(ProtectionPluginHook hook) {
        implementation.registerProtectionHook(hook);
    }

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
    public static SpawnerBoost createSpawnerBoost(BoostType type, int amount, Object data) {
        return implementation.createSpawnerBoost(type, amount, data);
    }

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
    public static SpawnerBoost createSpawnerBoost(BoostType type, int amount, Object data, Duration duration) {
        return implementation.createSpawnerBoost(type, amount, data, duration);
    }

}
