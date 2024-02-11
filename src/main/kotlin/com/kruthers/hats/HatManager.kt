package com.kruthers.hats

import com.kruthers.hats.classes.Hat
import com.kruthers.hats.utils.ModelIdNotUnique
import org.bukkit.entity.Player
import java.util.*

object HatManager {
    val hats: HashMap<String, Hat> = HashMap()
    private var hatsDisabled: Set<Player> = hashSetOf()
    private var forceEnabled: Set<UUID> = hashSetOf()

    /**
     * Gets a hat using only the model data value
     * @param modelData The model data value to use, should already be adjusted
     * @return the hat or null if there is no hat with that id
     */
    fun getHatFromModelData(modelData: Int): Hat? {
        return this.hats.values.firstOrNull{ it.modelData == modelData }
    }

    /**
     * Adds/ updates a hat on the plugin
     * @param hat The hat to add
     * @throws ModelIdNotUnique if the hats custom model data is already in
     */
    fun addHat(hat: Hat): Boolean  {
        val oldHat = this.getHatFromModelData(hat.modelData)
        if (oldHat != null && oldHat.id != hat.id) {
            throw ModelIdNotUnique(hat.modelData)
        }

        this.hats[hat.id] = hat
        return true
    }

    /**
     * Removes a hat from the plugin
     * @param id The id of the hat to remove
     * @return The hat if it was removed, null if no hat was removed
     */
    fun removeHat(id: String): Hat? {
        val hat = this.hats[id]
        return if (hat != null) {
            this.hats.remove(id)
            hat
        } else {
            null
        }
    }

    //Hat display tracking
    fun hasHatsDisabled(player: Player): Boolean {
        return hatsDisabled.contains(player)
    }

    fun disableHats(player: Player) {
        this.hatsDisabled = this.hatsDisabled.plus(player)
        this.forceEnabled = this.forceEnabled.minus(player.uniqueId)

        this.reloadEntities(player)
    }

    fun hasHatsForceEnabled(player: Player): Boolean {
        return forceEnabled.contains(player.uniqueId)
    }

    fun forceEnableHats(player: Player) {
        this.hatsDisabled = this.hatsDisabled.minus(player)
        this.forceEnabled = this.forceEnabled.plus(player.uniqueId)

        this.reloadEntities(player)
    }

    @Suppress("UnstableApiUsage")
    private fun reloadEntities(player: Player) {
        val viewDistance = ((player.viewDistance + 1)*16).toDouble()
        val entites = player.getNearbyEntities(viewDistance,viewDistance,356.0)
        entites.forEach {
            player.hideEntity(HatsPlugin.SELF, it)
            player.showEntity(HatsPlugin.SELF, it)
        }
    }

}