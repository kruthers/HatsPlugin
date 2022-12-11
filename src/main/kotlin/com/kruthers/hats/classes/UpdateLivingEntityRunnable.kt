package com.kruthers.hats.classes

import com.kruthers.hats.HatsPlugin
import com.kruthers.hats.utils.convertToHat
import com.kruthers.hats.utils.isItemAHelmet
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.scheduler.BukkitRunnable

class UpdateLivingEntityRunnable(private val entity: LivingEntity, private val plugin: HatsPlugin) : BukkitRunnable() {

    init {
        this.runTaskLater(plugin, 1)
    }

    override fun run() {
        val item = this.entity.equipment?.getItem(EquipmentSlot.HEAD)
        if (isItemAHelmet(item, this.plugin)) {
            this.entity.equipment?.setItem(EquipmentSlot.HEAD, convertToHat(item!!, this.plugin))
        }
    }
}