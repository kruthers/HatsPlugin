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


class HatsPlugin: JavaPlugin() {
    val hats: HashMap<String, Hat> = HashMap()
    val hatModelIDs: HashMap<Int, String> = HashMap()
    private var hatsDisabled: Set<Player> = hashSetOf()
    private var forceEnabled: Set<UUID> = hashSetOf()
    private lateinit var protocolManager: ProtocolManager
    private val hatsData = HatsData(this)

    init {
        ConfigurationSerialization.registerClass(Hat::class.java, "Hat")
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
        this.hats.putAll(this.hatsData.getHats())

        this.logger.info("Registering Commands")
        //create command manager
        val cmdManager = PaperCommandManager(
            this,
            CommandExecutionCoordinator.simpleCoordinator(),
            Function.identity(),
            Function.identity()
        )
        if (cmdManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            cmdManager.registerBrigadier()
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
        this.hatsData.saveHats(this.hats)
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

    private fun reloadEntities(player: Player) {
        val viewDistance = ((player.viewDistance + 1)*16).toDouble()
        val entites = player.getNearbyEntities(viewDistance,viewDistance,356.0)
        entites.forEach {
            player.hideEntity(this, it)
            player.showEntity(this, it)
        }
    }

    /**
     * Gets a hat using only the model data value
     * @param modelData The model data value to use, should already be adjusted
     * @return the hat or null if there is no hat with that id
     */
    fun getHatFromModelData(modelData: Int): Hat? {
        val id = this.hatModelIDs[modelData]

        if (id != null) {
            val hat = this.hats[id]
            if (hat != null) {
                return hat
            } else {
                this.hatModelIDs.remove(modelData)
            }
        }

        return null
    }

    /**
     * Adds/ updates a hat on the plugin
     * @param hat The hat to add
     * @throws ModelIdNotUnique if the hats custom model data is already in
     */
    fun addHat(hat: Hat): Boolean  {
        val modelID = hat.getModelData()
        val id = hat.getID()

        val oldHat = this.getHatFromModelData(modelID)
        if (oldHat != null) {
            if (oldHat.getID() != id) {
                throw ModelIdNotUnique(modelID)
            } else {
                this.hatModelIDs.remove(oldHat.getModelData())
            }
        }

        this.hats[id] = hat
        this.hatModelIDs[modelID] = id
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
            this.hatModelIDs
            hat
        } else {
            null
        }
    }


}