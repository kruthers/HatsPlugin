package com.kruthers.hats.classes

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.serialization.ConfigurationSerializable

class RawHat(
    private val id: String, private val displayName: String, private val modelData: Int,
    private val description: String, private val dyeable: Boolean
    ): ConfigurationSerializable {
    companion object {
        fun fromHat(hat: Hat): RawHat {
            val mm = MiniMessage.miniMessage()
            val displayName = mm.serialize(hat.getDisplayName())
            val description = mm.serialize(hat.getDescription())

            return RawHat(hat.getID(), displayName, hat.getModelData(), description, hat.isDyeable())
        }
    }

    fun toHat(): Hat {
        val mm = MiniMessage.miniMessage()

        val displayName = mm.deserialize(this.displayName)
        val description = mm.deserialize(this.description)

        return Hat(this.id, displayName, this.modelData, description, this.dyeable)
    }

    override fun serialize(): MutableMap<String, Any> {
        val map: MutableMap<String, Any> = mutableMapOf()
        map["id"] = this.id

        return map
    }


}