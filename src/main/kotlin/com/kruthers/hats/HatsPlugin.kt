package com.kruthers.hats

import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.minecraft.extras.AudienceProvider
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler
import cloud.commandframework.minecraft.extras.MinecraftHelp
import cloud.commandframework.paper.PaperCommandManager
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.kruthers.hats.classes.Hat
import com.kruthers.hats.classes.HatsData
import com.kruthers.hats.commands.*
import com.kruthers.hats.listeners.*
import com.kruthers.hats.utils.ModelIdNotUnique
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.interfaces.paper.PaperInterfaceListeners
import java.util.*
import java.util.function.Function
import java.util.logging.Logger


class HatsPlugin: JavaPlugin() {
    private var hatsDisabled: Set<Player> = hashSetOf()
    private var forceEnabled: Set<UUID> = hashSetOf()
    private lateinit var protocolManager: ProtocolManager
    private val hatsData = HatsData(this)

    init {
        ConfigurationSerialization.registerClass(Hat::class.java, "Hat")
    }

    companion object {
        val hats: HashMap<String, Hat> = HashMap()

        /**
         * Gets a hat using only the model data value
         * @param modelData The model data value to use, should already be adjusted
         * @return the hat or null if there is no hat with that id
         */
        fun getHatFromModelData(modelData: Int): Hat? {
            return this.hats.values.firstOrNull{ it.modelData == modelData }
        }

        /**
         * Adds/ updates a hat on the plugin
         * @param hat The hat to add
         * @throws ModelIdNotUnique if the hats custom model data is already in
         */
        fun addHat(hat: Hat): Boolean  {
            val oldHat = this.getHatFromModelData(hat.modelData)
            if (oldHat != null && oldHat.id != hat.id) {
                throw ModelIdNotUnique(hat.modelData)
            }

            this.hats[hat.id] = hat
            return true
        }

        /**
         * Removes a hat from the plugin
         * @param id The id of the hat to remove
         * @return The hat if it was removed, null if no hat was removed
         */
        fun removeHat(id: String): Hat? {
            val hat = this.hats[id]
            return if (hat != null) {
                this.hats.remove(id)
                hat
            } else {
                null
            }
        }
    }

    override fun onEnable() {
        this.logger.info("Loading requirements")
        try {
            this.protocolManager = ProtocolLibrary.getProtocolManager()
        } catch (error: Error) {
            this.logger.warning("Unable to enable plugin, missing dependency ProtocolLibrary")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

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
        try {
            if (cmdManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
                cmdManager.registerBrigadier()
            }
        } catch (err: Exception) {
            this.logger.warning("Failed to link with brigadier")
        }
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
        CoreCommands(cmdManager, help)
        HatsManagementCommands(this,cmdManager)
        HatsHandlingCommands(this, cmdManager)

        //DEV
//        val dev = DevCommands(this, cmdManager)
//        if (hats.size == 0) dev.generateTestHats(1)

        this.logger.info("Registering listeners")
        PaperInterfaceListeners.install(this)
        protocolManager.addPacketListener(EquipmentPacket(this))
        this.server.pluginManager.registerEvents(InventoryEvents(this), this)
        this.server.pluginManager.registerEvents(InteractionEvents(this), this)
        this.server.pluginManager.registerEvents(ResourcePackListeners(this), this)

    }

    override fun onDisable() {
        this.logger.info("Saving hat data")
        this.hatsData.saveHats()
    }

    //Hat display tracking
    fun hasHatsDisabled(player: Player): Boolean {
        return hatsDisabled.contains(player)
    }

    fun disableHats(player: Player) {
        this.hatsDisabled = this.hatsDisabled.plus(player)
        this.forceEnabled = this.forceEnabled.minus(player.uniqueId)

        this.reloadEntities(player)
    }

    fun hasHatsForceEnabled(player: Player): Boolean {
        return forceEnabled.contains(player.uniqueId)
    }

    fun forceEnableHats(player: Player) {
        this.hatsDisabled = this.hatsDisabled.minus(player)
        this.forceEnabled = this.forceEnabled.plus(player.uniqueId)

        this.reloadEntities(player)
    }

    @Suppress("UnstableApiUsage")
    private fun reloadEntities(player: Player) {
        val viewDistance = ((player.viewDistance + 1)*16).toDouble()
        val entites = player.getNearbyEntities(viewDistance,viewDistance,356.0)
        entites.forEach {
            player.hideEntity(this, it)
            player.showEntity(this, it)
        }
    }


}