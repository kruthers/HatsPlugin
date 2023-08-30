package com.kruthers.hats.listeners

import com.kruthers.hats.HatsPlugin
import com.kruthers.hats.classes.UpdateInventoryRunnable
import com.kruthers.hats.classes.UpdateLivingEntityRunnable
import com.kruthers.hats.utils.isItemAHat
import com.kruthers.hats.utils.isItemAHelmet
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseArmorEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class InteractionEvents(private val plugin: HatsPlugin): Listener {

    @EventHandler
    fun onItemEquip(event: PlayerInteractEvent) {
        val item = event.item
        if (isItemAHelmet(item)) {
            UpdateInventoryRunnable(event.player, plugin)
        }
    }

    @EventHandler
    fun onEntityInteraction(event: PlayerInteractAtEntityEvent) {
        val entity = event.rightClicked
        if (entity.type != EntityType.ARMOR_STAND || entity !is ArmorStand) return
        if (isItemAHelmet(event.player.inventory.getItem(event.hand))) {
            UpdateLivingEntityRunnable(entity, this.plugin)
        } else {
            if (isItemAHat(entity.getItem(EquipmentSlot.HEAD))) {
                UpdateInventoryRunnable(event.player, plugin)
            }
        }
    }

    @EventHandler
    fun onDispenseArmourEvent(event: BlockDispenseArmorEvent) {
        val item = event.item
        val entity = event.targetEntity
        if (isItemAHelmet(item)){
            UpdateLivingEntityRunnable(entity, this.plugin)
        }
    }
}