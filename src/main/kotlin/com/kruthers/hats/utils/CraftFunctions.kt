package com.kruthers.hats.utils

import net.minecraft.server.level.ServerPlayer
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val Player.handle: ServerPlayer
    get() = (this as CraftPlayer).handle

val ItemStack.mcStack: net.minecraft.world.item.ItemStack
    get() = (CraftItemStack.asCraftCopy(this)).handle