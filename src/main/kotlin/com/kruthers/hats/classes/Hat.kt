package com.kruthers.hats.classes

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class Hat: ConfigurationSerializable {
    private val id: String
    private var displayName: Component
    private var modelData: Int
    private var description: Component
    private var dyeable: Boolean

    constructor(id: String, displayName: Component, modelData: Int, description: Component, dyeable: Boolean) {
        this.id = id
        this.displayName = displayName
        this.modelData = modelData
        this.description = description
        this.dyeable = dyeable
    }

    //Deserializer
    constructor(args: Map<String, Object>) {
        val mm = MiniMessage.miniMessage()
        val displayName = mm.deserialize(args["display_name"]!! as String)
        val description = mm.deserialize(args["description"]!! as String)

        this.id = args["id"]!! as String
        this.displayName = displayName
        this.modelData = args["model_date"]!! as Int
        this.description = description
        this.dyeable = args["dyeable"]!! as Boolean
    }

    public fun getItem(material: Material, baseId: Int): ItemStack {
        val item = ItemStack(material, 1).also {
            it.itemMeta = it.itemMeta.also { meta ->
                meta.displayName(this.displayName)
                meta.setCustomModelData(this.modelData + baseId)
                meta.lore( mutableListOf(this.description))

                meta.isUnbreakable = true
                meta.itemFlags.add(ItemFlag.HIDE_UNBREAKABLE)
            }
        }

        return item
    }

    /**
     * Gets the item display lore
     * @return the lore formatted
     */
    public fun getLore(): MutableList<Component> {
        val mm = MiniMessage.miniMessage()
        return mm.serialize(this.description)
            .split(Regex("<br>|<newline>|\n"))
            .map { mm.deserialize(it)}.toMutableList()
    }

    //Getters/ Setters
    public fun getID(): String { return this.id}

    public fun getDisplayName(): Component { return this.displayName }
    public fun setDisplayName(name: Component) { this.displayName = name }
    
    public fun getModelData(): Int { return this.modelData }
    public fun setModelData(modelData: Int) { this.modelData = modelData }

    public fun getDescription(): Component { return this.description }
    public fun setDescription(description: Component) { this.description = description }

    public fun isDyeable(): Boolean { return this.dyeable }
    public fun setDyeable(dyeable: Boolean) { this.dyeable = dyeable }

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
        map["description"] = mm.serialize(this.description)
        map["dyeable"] = this.dyeable

        return map
    }

}