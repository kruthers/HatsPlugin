package com.kruthers.hats.utils

import com.kruthers.hats.HatsPlugin
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

fun isItemAHat(item: ItemStack?, plugin: HatsPlugin): Boolean {
    return (item != null && item.type == Material.LEATHER_HORSE_ARMOR && item.itemMeta.hasCustomModelData() && item.itemMeta.customModelData > plugin.getHatBaseID())
}

fun isItemAHelmet(item: ItemStack?, plugin: HatsPlugin): Boolean {
    return (item != null && item.type == Material.LEATHER_HELMET && item.itemMeta.hasCustomModelData() && item.itemMeta.customModelData > plugin.getHelmetBaseID())
}

fun convertToHat(helmet: ItemStack, plugin: HatsPlugin): ItemStack {
    return helmet.also {
        it.type = Material.LEATHER_HORSE_ARMOR
        it.itemMeta = it.itemMeta.also { meta ->
            if (meta.hasCustomModelData()) {
                val modelData = meta.customModelData - plugin.getHelmetBaseID()
                meta.setCustomModelData(modelData + plugin.getHatBaseID())
            }
        }
    }
}

fun convertToHelmet(hat: ItemStack, plugin: HatsPlugin): ItemStack {
    return hat.also {
        it.type = Material.LEATHER_HELMET
        it.itemMeta = it.itemMeta.also { meta ->
            if (meta.hasCustomModelData()) {
                val modelData = meta.customModelData - plugin.getHatBaseID()
                meta.setCustomModelData(modelData + plugin.getHelmetBaseID())
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            }
        }
    }
}