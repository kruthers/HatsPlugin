package com.kruthers.hats.commands

import cloud.commandframework.arguments.standard.IntegerArgument
import cloud.commandframework.bukkit.BukkitCommandManager
import cloud.commandframework.context.CommandContext
import com.kruthers.hats.classes.Hat
import com.kruthers.hats.HatsPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender

class DevCommands(private val plugin: HatsPlugin, private val manager: BukkitCommandManager<CommandSender>) {

    init {
        val builder = manager.commandBuilder("devhat")
        manager.command(builder
            .literal("list")
            .handler(this::listCommand)
            .permission("hats.dev")
        )
        manager.command(builder
            .literal("test_hats")
            .argument(IntegerArgument.of("start"))
            .handler(this::testHatsCommand)
            .permission("hats.dev")
        )
        manager.command(builder
            .literal("check")
            .literal("model_data")
            .argument(IntegerArgument.of("id"))
            .handler(this::checkCommand)
            .permission("hats.dev")
        )
    }


    private fun listCommand(context: CommandContext<CommandSender>) {
        plugin.hatModelIDs.forEach { modelData, id ->
            context.sender.sendMessage(Component.text("$modelData: $id"))
        }
    }

//    private fun listDisabled(context: CommandContext<CommandSender>) {
//        context.sender.sendMessage(Component.text("Hast Hats disabled:"))
//        this.plugin.hatsDisabled.forEach{
//            context.sender.sendMessage(it.displayName())
//        }
//    }

    private fun testHatsCommand(context: CommandContext<CommandSender>) {
        val start = context.get<Int>("start")+1
        generateTestHats(start)
    }

    fun generateTestHats(start: Int) {
        for (i in start..start+18) {
            val hat = Hat("test_$i", Component.text("Test hat #$i"), i, Component.text("Auto generated Hat"), true)
            plugin.addHat(hat)
        }
    }

    private fun checkCommand(context: CommandContext<CommandSender>) {
        val id: Int = context.get("id")
        val hat = this.plugin.getHatFromModelData(id)
        if (hat == null) {
            context.sender.sendMessage(Component.text("No hat with provided id found", NamedTextColor.RED))
        } else {
            context.sender.sendMessage(Component.text("Got hat ${hat.getID()} with the model data $id", NamedTextColor.GREEN))
        }
    }

}