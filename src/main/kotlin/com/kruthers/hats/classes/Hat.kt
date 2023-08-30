package com.kruthers.hats.classes

import com.kruthers.hats.HatsPlugin
import com.kruthers.hats.utils.ModelIdNotUnique
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class Hat(
    val id: String,
    var displayName: Component,
    modelData: Int,
    private var description: List<Component>,
    var dyeable: Boolean
    ): ConfigurationSerializable {

    var modelData: Int = modelData
        set(value) {
            val oldHat = HatsPlugin.getHatFromModelData(value)
            if (oldHat != null &&  oldHat.id != this.id) {
                throw ModelIdNotUnique(value)
            }
            field = value
        }

    constructor(id: String, displayName: String, modelData: Int, description: String, dyeable: Boolean): this(
        id, Component.empty(), modelData, mutableListOf(), dyeable
    ) {
        val mm = MiniMessage.miniMessage()
        this.displayName = mm.deserialize(displayName)
        this.setDescription(description)
    }

    //Deserializer
    @Suppress("Unused")
    constructor(args: Map<String, Any>): this(
        args["id"]!! as String,
        args["display_name"]!! as String,
        args["model_date"]!! as Int,
        args["description"]!! as String,
        args["dyeable"]!! as Boolean
    )

    fun getItem(): ItemStack {
        val item = ItemStack(Material.LEATHER_HELMET, 1).also {
            it.itemMeta = it.itemMeta.also { meta ->
                meta.displayName(this.displayName)
                meta.setCustomModelData(this.modelData)
                meta.lore(this.description)

                meta.isUnbreakable = true
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
            }
        }

        return item
    }

    //Getters/ Setters
    fun getDescription(): List<Component> { return this.description }
    @Suppress("UNUSED")
    fun setDescription(description: List<Component>) { this.description = description }
    fun setDescription(description: String) {
        val mm = MiniMessage.miniMessage()
        this.description = description.split("<br>").map { mm.deserialize(it) }
    }

    //overrides
    override fun toString(): String {
        return this.id
    }

    override fun serialize(): MutableMap<String, Any> {
        val mm = MiniMessage.miniMessage()
        val map: MutableMap<String, Any> = mutableMapOf()
        map["id"] = this.id
        map["model_date"] = this.modelData
        map["display_name"] = mm.serialize(this.displayName)
        map["description"] = this.description.joinToString("<br>"){ mm.serialize(it) }
        map["dyeable"] = this.dyeable

        return map
    }

}