package com.kruthers.hats

import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.minecraft.extras.AudienceProvider
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler
import cloud.commandframework.minecraft.extras.MinecraftHelp
import cloud.commandframework.paper.PaperCommandManager
import com.kruthers.hats.classes.Hat
import com.kruthers.hats.classes.HatsData
import com.kruthers.hats.commands.CoreCommands
import com.kruthers.hats.commands.HatsHandlingCommands
import com.kruthers.hats.commands.HatsManagementCommands
import com.kruthers.hats.listeners.ConnectionListener
import com.kruthers.hats.listeners.InteractionEvents
import com.kruthers.hats.listeners.InventoryEvents
import com.kruthers.hats.listeners.ResourcePackListeners
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.interfaces.paper.PaperInterfaceListeners
import java.util.function.Function


class HatsPlugin: JavaPlugin() {
    private val hatsData = HatsData(this)

    init {
        ConfigurationSerialization.registerClass(Hat::class.java, "Hat")
    }

    companion object {
        lateinit var SELF: HatsPlugin
        val instance: HatsPlugin get() = SELF
    }

    override fun onEnable() {
        SELF = this
        this.logger.info("Loading Config")
        this.config.options().copyDefaults(true)
        this.saveConfig()

        this.hatsData.init()

        this.logger.info("Registering Commands")
        //create command manager
        val cmdManager = PaperCommandManager(
            this,
            CommandExecutionCoordinator.simpleCoordinator(),
            Function.identity(),
            Function.identity()
        )
        MinecraftExceptionHandler<CommandSender>()
            .withInvalidSyntaxHandler()
            .withInvalidSenderHandler()
            .withArgumentParsingHandler()
            .withNoPermissionHandler()
            .withCommandExecutionHandler()
            .withDecorator { component ->
                Component.text()
                    .append(component)
                    .build()
            }
            .apply(cmdManager, AudienceProvider.nativeAudience())
        val help = MinecraftHelp<CommandSender>(
            "hats help",
            AudienceProvider.nativeAudience(),
            cmdManager
        )

        try {
            cmdManager.registerBrigadier()
        } catch (err: Exception) {
            this.logger.warning("Failed to link with brigadier")
        }

        CoreCommands(cmdManager, help)
        HatsManagementCommands(this,cmdManager)
        HatsHandlingCommands(this, cmdManager)

        //DEV
//        val dev = DevCommands(this, cmdManager)
//        if (hats.size == 0) dev.generateTestHats(1)

        this.logger.info("Registering listeners")
        PaperInterfaceListeners.install(this)
        this.server.pluginManager.registerEvents(InventoryEvents(this), this)
        this.server.pluginManager.registerEvents(InteractionEvents(this), this)
        this.server.pluginManager.registerEvents(ResourcePackListeners(this), this)
        this.server.pluginManager.registerEvents(ConnectionListener(), this)

        SELF = this
    }

    override fun onDisable() {
        this.logger.info("Saving hat data")
        this.hatsData.saveHats()
    }
}