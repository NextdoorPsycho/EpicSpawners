package com.songoda.epicspawners.api.spawner;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Set;

import com.songoda.epicspawners.api.CostType;
import com.songoda.epicspawners.api.boost.BoostType;
import com.songoda.epicspawners.api.boost.SpawnerBoost;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a spawner stack container in the game world.
 *
 * @since 5.0
 */
public interface Spawner {

    /**
     * Get identifying name of this spawner.
     *
     * <p>Will return Omni if multiple {@link SpawnerData}
     * objects are present.</p>
     *
     * @return name of this spawner
     */
    String getIdentifyingName();

    /**
     * Get custom display name as to be used when
     * displaying this spawner.
     * 
     * <p>Will return Omni if multiple {@link SpawnerData}
     * objects are present.</p>
     *
     * @return display name
     */
    String getDisplayName();

    /**
     * Get location of the spawner object.
     *
     * @return location of spawner
     */
    Location getLocation();

    /**
     * Get the {@link CreatureSpawner} object from this
     * spawner blocks {@link BlockState}
     *
     * @return this blocks CreatureSpawner
     */
    CreatureSpawner getCreatureSpawner();

    /**
     * Get the SpawnerStacks contained in this spawner.
     *
     * @return SpawnerStacks
     */
    Collection<SpawnerStack> getSpawnerStacks();

    /**
     * Add a SpawnerStack to this spawner.
     *
     * @param spawnerStack the desired SpawnerStack.
     */
    void addSpawnerStack(SpawnerStack spawnerStack);

    /**
     * This will clear the {@link SpawnerStack} objects from
     * this spawner.
     */
    void clearSpawnerStacks();

    /**
     * This will return the {@link SpawnerStack} located at the
     * top of the {@link ArrayDeque}.
     *
     * @return first SpawnerStack
     */
    SpawnerStack getFirstStack();

    /**
     * Get the player who placed this Spawner.
     *
     * @return the placer. null if not placed by player
     */
    OfflinePlayer getPlacedBy();

    /**
     * Set the amount of spawns that this spawner has initiated
     *
     * @param count number of spawns
     */
    void setSpawnCount(int count);

    /**
     * Get the amount of spawns that this spawner has initiated.
     *
     * @return amount of spawns
     */
    int getSpawnCount();

    /**
     * Get the total number of {@link SpawnerData} objects
     * contained by this spawner.
     *
     * @return count
     */
    int getSpawnerDataCount();

    /**
     * Get the cost needed to upgrade this spawner with
     * either Experience or Economy.
     *
     * @param type upgrade cost type
     * @return cost to upgrade
     */
    int getUpgradeCost(CostType type);

    /**
     * Extracts the spawner type and amount from the provided
     * {@link ItemStack} then forwards the Stack method.
     *
     * @param player the player performing the stacking
     * @param item   the spawner item to be stacked
     * @return true if successful, false otherwise
     */
    boolean preStack(Player player, ItemStack item);

    /**
     * Converts the provided ItemStack to a Spawner stack and
     * adds it to this Spawner.
     *
     * @param player the player performing the stacking
     * @param type   the type of spawner to stack
     * @param amt    the amount of that spawner type to stack
     * @return true if successful, false otherwise
     * @deprecated see {@link #stack(Player, SpawnerData, int)}
     */
    @Deprecated
    boolean stack(Player player, String type, int amt);

    /**
     * Converts the provided ItemStack to a Spawner stack and
     * adds it to this Spawner.
     *
     * @param player the player performing the stacking
     * @param data   the type of spawner to stack
     * @param amount the amount of that spawner type to stack
     * @return true if successful, false otherwise
     */
    boolean stack(Player player, SpawnerData data, int amount);

    /**
     * Removes the topmost {@link SpawnerData} from this
     * spawner.
     *
     * @param player the player performing the unstacking
     * @return true if successful, false otherwise
     */
    boolean unstack(Player player);

    /**
     * Add a {@link SpawnerBoost} to this {@link Spawner} instance.
     * If the provided boost is temporary and has expired, (i.e.
     * {@link SpawnerBoost#getEndTime()} > {@link System#currentTimeMillis()}),
     * this method will take no effect and false will be returned.
     * 
     * @param boost the boost to add
     * 
     * @return true if successfully added, false if already added
     * or boost is expired
     */
    boolean addBoost(SpawnerBoost boost);

    /**
     * Create a new {@link SpawnerBoost} and add it to this spawner.
     * The created boost will be permanent on this spawner until it
     * has been manually removed using the {@link #removeBoost(SpawnerBoost)}
     * method.
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
    SpawnerBoost addBoost(BoostType type, int amount, Object data);

    /**
     * Create a new {@link SpawnerBoost} and add it to this spawner.
     * The created boost will be temporary according to the specified
     * {@code duration} parameter. The boost will be in effect until
     * the duration of time has surpassed, after which it will be removed
     * from this spawner and will no longer be in effect. The returned
     * SpawnerBoost instance will then be invalidated and no longer usable
     * by the {@link #addBoost(SpawnerBoost)} method.
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
    SpawnerBoost addBoost(BoostType type, int amount, Object data, Duration duration);

    /**
     * Remove a specific {@link SpawnerBoost} from this spawner.
     * If this spawner does not have an active boost matching the
     * criteria of that passed (i.e. {@link #equals(Object)} returns
     * false), this method will fail silently and false will be
     * returned
     * 
     * @param boost the boost to remove
     * 
     * @return true if successful, false otherwise
     */
    boolean removeBoost(SpawnerBoost boost);

    /**
     * Remove all boosts of the specified type from this spawner
     * 
     * @param type the type of boost to remove
     * 
     * @return an array of all removed boosts
     */
    SpawnerBoost[] removeBoost(BoostType type);

    /**
     * Check whether this spawner has any active boosts or not
     * 
     * @return true if boosts are present, false otherwise
     */
    boolean hasBoost();

    /**
     * Check whether the provided boost is active on this spawner
     * or not
     * 
     * @param boost the boost to check
     * 
     * @return true if present, false otherwise
     */
    boolean hasBoost(SpawnerBoost boost);

    /**
     * Check whether this spawner has any active boosts of the
     * specified type or not
     * 
     * @param type the boost type to check
     * 
     * @return true if boost of specified type is present, false
     * otherwise
     */
    boolean hasBoost(BoostType type);

    /**
     * Get an immutable Set of all {@link SpawnerBoost}s active
     * on this spawner
     * 
     * @return all active boosts
     */
    Set<SpawnerBoost> getBoosts();

    /**
     * Get the total boosted amount from the spawner.
     *
     * @return the total boost
     */
    int getBoost();

    /**
     * Get the end of life for the current closest to end boost.
     *
     * @return the instant at which the boosts on this spawner
     * will end. If no boosts are active, {@link Instant#now()}
     * will be returned
     */
    Instant getBoostEnd();

    /**
     * Clear all boosts from this spawner
     */
    void clearBoosts();

    /**
     * Updates the delay of the spawner to use the equation
     * defined by EpicSpawners as apposed to using the default
     * Minecraft delay.
     *
     * @return delay set
     */
    int updateDelay();

    /**
     * You can use this method to force a spawn of this spawner.
     */
    void spawn();

}