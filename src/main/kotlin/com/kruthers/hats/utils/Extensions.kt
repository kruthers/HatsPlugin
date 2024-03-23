package com.kruthers.hats.utils

import com.kruthers.hats.HatsPlugin
import com.kruthers.hats.classes.Hat
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

fun ItemStack.toHat() : Hat? {
    val id = this.mcStack.tag?.getString("ps_hats") ?: return null

    val customModelData =  this.itemMeta.customModelData - HatsPlugin.instance.config.getInt("model_data_start.leather_hat")
    val displayName = this.itemMeta.displayName()?: Component.text(id)
    val lore = this.lore()?: mutableListOf()

    return Hat(id, displayName, customModelData, lore, true)
}