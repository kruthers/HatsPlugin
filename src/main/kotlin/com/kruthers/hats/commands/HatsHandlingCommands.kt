package com.kruthers.hats.commands

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.BukkitCommandManager
import cloud.commandframework.bukkit.arguments.selector.SinglePlayerSelector
import cloud.commandframework.bukkit.parsers.selector.SinglePlayerSelectorArgument
import cloud.commandframework.context.CommandContext
import com.kruthers.hats.HatsPlugin
import com.kruthers.hats.utils.HatNotFoundException
import com.kruthers.hats.utils.NoPlayerFoundException
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class HatsHandlingCommands(private val plugin: HatsPlugin, private val manager: BukkitCommandManager<CommandSender>) {
    private val mm = MiniMessage.miniMessage()
    private val builder = manager.commandBuilder("hats", ArgumentDescription.of("Core hats command"))

    init {
        manager.command(builder
            .literal("give")
            .argument(StringArgument.of("name"))
            .senderType(Player::class.java)
            .permission("hats.give")
            .handler(this::giveHatCommand)
        )
        manager.command(builder
            .literal("give")
            .argument(StringArgument.of("name"))
            .argument(SinglePlayerSelectorArgument.of("player"))
            .permission("hats.give")
            .handler(this::giveOtherHatCommand)
        )
        manager.command(builder
            .literal("force_enable")
            .senderType(Player::class.java)
            .permission("hats.force_enable")
            .handler(this::enableHats)
        )
    }

    private fun giveOtherHatCommand(context: CommandContext<CommandSender>) {
        val id: String = context.get("name")
        val player: Player = context.get<SinglePlayerSelector>("player").player ?: throw NoPlayerFoundException()

        val hat = this.plugin.hats[id] ?: throw HatNotFoundException(id)

        player.inventory.addItem(hat.getItem(Material.LEATHER_HELMET,this.plugin.getHelmetBaseID()))
        context.sender.sendMessage(mm.deserialize("<gay><italic>Gave ${player.name} hat ").append(hat.getDisplayName()))
    }

    private fun giveHatCommand(context: CommandContext<CommandSender>) {
        val id: String = context.get("name")
        val player = context.sender as Player

        val hat = this.plugin.hats[id] ?: throw HatNotFoundException(id)

        player.inventory.addItem(hat.getItem(Material.LEATHER_HELMET,this.plugin.getHelmetBaseID()))
        context.sender.sendMessage(mm.deserialize("<gay><italic>Gave ${player.name} hat ").append(hat.getDisplayName()))
    }

    private fun enableHats(context: CommandContext<CommandSender>) {
        val player = context.sender as Player

        if (this.plugin.hasHatsDisabled(player)) {
            this.plugin.forceEnableHats(player)
            player.sendMessage(mm.deserialize("<green>Hats are now enabled by force."))
        } else {
            player.sendMessage(mm.deserialize("<gray>You can already see hats"))
        }
    }

}