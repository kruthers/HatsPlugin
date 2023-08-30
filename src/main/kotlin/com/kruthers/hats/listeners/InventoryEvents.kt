package com.kruthers.hats.listeners

import com.kruthers.hats.HatsPlugin
import com.kruthers.hats.classes.UpdateInventoryRunnable
import com.kruthers.hats.utils.convertToHelmet
import com.kruthers.hats.utils.isItemAHat
import com.kruthers.hats.utils.isItemAHelmet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.inventory.EquipmentSlot

class InventoryEvents(val plugin: HatsPlugin): Listener {
    private val singleSlotActions = mutableListOf(
        InventoryAction.SWAP_WITH_CURSOR,
        InventoryAction.DROP_ALL_SLOT,
        InventoryAction.DROP_ONE_SLOT,
        InventoryAction.PICKUP_ALL,
        InventoryAction.PICKUP_ALL
    )

    @EventHandler
    fun onClickEvent(event: InventoryClickEvent) {
        val slot = event.slot
        val player = event.whoClicked
        if (slot == 39 && singleSlotActions.contains(event.action) && !event.isCancelled) {
            val item = player.inventory.getItem(39)
            if (isItemAHat(item)) {
                item!!
                player.inventory.setItem(EquipmentSlot.HEAD, convertToHelmet(item))
            }
        }

        UpdateInventoryRunnable(player, this.plugin)
    }

    @EventHandler
    fun onItemPickup(event: PlayerAttemptPickupItemEvent) {
        val item = event.item.itemStack
        if (isItemAHat(item) && event.isCancelled) {
            event.item.itemStack = convertToHelmet(item)
        }
    }

    @EventHandler
    fun onEnchant(event: EnchantItemEvent) {
        if (isItemAHelmet(event.item) && !this.plugin.config.getBoolean("hat_enchating")) {
            event.isCancelled = true
            event.enchanter.sendMessage(Component.text("You are unable to enchant hats", NamedTextColor.RED))
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!plugin.config.getBoolean("soul_bound")) return
        event.drops.forEach { drop ->
            if (isItemAHelmet(drop) || isItemAHat(drop)) {
                event.drops.remove(drop)
                event.itemsToKeep.add(drop)
            }
        }
    }
}