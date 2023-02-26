package com.kruthers.hats.commands

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.BukkitCommandManager
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.minecraft.extras.MinecraftHelp
import org.bukkit.command.CommandSender
import java.util.*

class CoreCommands(manager: BukkitCommandManager<CommandSender>, private val help: MinecraftHelp<CommandSender>) {
    private val builder = manager.commandBuilder("hats", ArgumentDescription.of("Core hats command"))

    init {
        manager.command(builder
            .meta(CommandMeta.DESCRIPTION, "Help command")
            .literal("help")
            .argument(StringArgument.optional("query", StringArgument.StringMode.GREEDY))
            .permission("hats.help")
            .handler{ context ->
                val query: Optional<String> = context.getOptional("query")
                if (query.isPresent) {
                    this.help.queryCommands(query.get(), context.sender)
                } else {
                    this.help.queryCommands("", context.sender)
                }
            }
        )
    }


}