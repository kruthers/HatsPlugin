package com.kruthers.hats.classes

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class Hat(
    private val id: String,
    private var displayName: Component,
    private var modelData: Int,
    private var description: List<Component>,
    private var dyeable: Boolean
    ): ConfigurationSerializable {

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
    fun getID(): String { return this.id}

    fun getDisplayName(): Component { return this.displayName }
    fun setDisplayName(name: Component) { this.displayName = name }
    
    fun getModelData(): Int { return this.modelData }
    fun setModelData(modelData: Int) { this.modelData = modelData }

    fun getDescription(): List<Component> { return this.description }
    fun setDescription(description: List<Component>) { this.description = description }
    fun setDescription(description: String) {
        val mm = MiniMessage.miniMessage()
        this.description = description.split("<br>").map { mm.deserialize(it) }
    }

    fun isDyeable(): Boolean { return this.dyeable }
    fun setDyeable(dyeable: Boolean) { this.dyeable = dyeable }

    //overrides
    override fun toString(): String {
        return this.getID()
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