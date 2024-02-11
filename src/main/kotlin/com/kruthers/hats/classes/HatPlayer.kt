package com.kruthers.hats.classes

import com.kruthers.hats.HatManager
import com.kruthers.hats.utils.handle
import com.kruthers.hats.utils.isItemAHat
import com.kruthers.hats.utils.isItemAHelmet
import com.mojang.datafixers.util.Pair
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket
import net.minecraft.world.item.ItemStack
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

class HatPlayer(val player: Player): ChannelDuplexHandler() {
    companion object {
        private val players: HashMap<UUID, HatPlayer> = hashMapOf()

        fun create(player: Player) {
            players[player.uniqueId] = HatPlayer(player)
        }

        fun destroy(player: Player) {
            players.remove(player.uniqueId)?.destroy()
        }
    }

    init {
        val pipeline = player.handle.connection.connection.channel.pipeline()

        if (pipeline.names().contains("hats")) pipeline.remove("hats")

        pipeline.addBefore("packet_handler", "hats", this)
    }

    fun destroy() {
        val pipeline = player.handle.connection.connection.channel.pipeline()
        if (pipeline.names().contains("hats")) pipeline.remove("hats")
    }

    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
        if (msg is ClientboundSetEquipmentPacket) {
            msg.slots.iterator().forEach {
                if (it.first == net.minecraft.world.entity.EquipmentSlot.HEAD) {
                    val stack = it.second.bukkitStack
                    if (HatManager.hasHatsDisabled(this.player) && isItemAHat(stack)) {
                        stack.type = Material.LEATHER_HELMET
                        msg.slots.remove(it)
                        msg.slots.add(Pair(it.first, ItemStack.fromBukkitCopy(stack)))
                    } else if (!HatManager.hasHatsDisabled(this.player) && isItemAHelmet(stack)) {
                        stack.type = Material.LEATHER_HORSE_ARMOR
                        msg.slots.remove(it)
                        msg.slots.add(Pair(it.first, ItemStack.fromBukkitCopy(stack)))
                    }
                }
            }
        }
        super.write(ctx, msg, promise)
    }
}
