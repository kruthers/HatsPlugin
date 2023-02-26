package com.kruthers.hats.utils

import com.kruthers.hats.HatsPlugin
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

fun isItemAHat(item: ItemStack?, plugin: HatsPlugin): Boolean {
    return (item != null && item.type == Material.LEATHER_HORSE_ARMOR && item.itemMeta.hasCustomModelData() && plugin.getHatFromModelData(item.itemMeta.customModelData) != null)
}

fun isItemAHelmet(item: ItemStack?, plugin: HatsPlugin): Boolean {
    return (item != null && item.type == Material.LEATHER_HELMET && item.itemMeta.hasCustomModelData() && plugin.getHatFromModelData(item.itemMeta.customModelData) != null)
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