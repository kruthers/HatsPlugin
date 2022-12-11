package com.kruthers.hats.classes

import com.kruthers.hats.HatsPlugin
import com.kruthers.hats.utils.convertToHat
import com.kruthers.hats.utils.convertToHelmet
import com.kruthers.hats.utils.isItemAHat
import com.kruthers.hats.utils.isItemAHelmet
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.scheduler.BukkitRunnable

class UpdateInventoryRunnable(private val player: HumanEntity, private val plugin: HatsPlugin): BukkitRunnable() {
    init {
        this.runTaskLater(plugin,1)
    }

    override fun run() {
        //check main inv for any non helmet hats
        for (i in 0..35) {
            val item = player.inventory.getItem(i)
            if (isItemAHat(item, plugin)) {
                player.inventory.setItem(i, convertToHelmet(item!!, this.plugin))
            }
        }

        //check off hand
        var item = player.inventory.getItem(EquipmentSlot.OFF_HAND)
        if (isItemAHat(item, plugin)) {
            player.inventory.setItem(EquipmentSlot.OFF_HAND, convertToHelmet(item, this.plugin))
        }

        //check head
        item = player.inventory.getItem(EquipmentSlot.HEAD)
        if (isItemAHelmet(item, plugin)) {
            player.inventory.setItem(EquipmentSlot.HEAD, convertToHat(item, this.plugin))
        }
    }
}