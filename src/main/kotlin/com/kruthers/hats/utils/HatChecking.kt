package com.kruthers.hats.utils

import com.kruthers.hats.HatManager
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

fun isAHat(item: ItemStack?): Boolean {
    if (item != null && (item.type == Material.LEATHER_HORSE_ARMOR || item.type == Material.LEATHER_HELMET) && item.itemMeta.hasCustomModelData()) {
        if (HatManager.getHatFromModelData(item.itemMeta.customModelData) != null) return true
        //Gamemode 4 check
        return if (item.mcStack.tag?.contains("ps_hats") == true) {
            item.toHat()?.let { HatManager.addHat(it) }
            true
        } else false
    }
    return false
}

fun isItemAHat(item: ItemStack?): Boolean {
    return isAHat(item) && item!!.type == Material.LEATHER_HORSE_ARMOR
}

fun isItemAHelmet(item: ItemStack?): Boolean {
    return isAHat(item) && item!!.type == Material.LEATHER_HELMET
}

fun convertToHat(helmet: ItemStack): ItemStack {
    return helmet.also {
        it.type = Material.LEATHER_HORSE_ARMOR
        it.itemMeta = it.itemMeta.also { meta ->
            if (meta.hasCustomModelData()) {
                meta.setCustomModelData(meta.customModelData)
                meta.isUnbreakable = true
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
            }
        }
    }
}

fun convertToHelmet(hat: ItemStack): ItemStack {
    return hat.also {
        it.type = Material.LEATHER_HELMET
        it.itemMeta = it.itemMeta.also { meta ->
            if (meta.hasCustomModelData()) {
                meta.setCustomModelData(meta.customModelData)
                meta.isUnbreakable = true
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
            }
        }
    }
}