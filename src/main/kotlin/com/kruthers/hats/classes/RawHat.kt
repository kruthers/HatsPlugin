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
            val description = hat.getDescription().joinToString("<br>"){ mm.serialize(it) }

            return RawHat(hat.getID(), displayName, hat.getModelData(), description, hat.isDyeable())
        }
    }

    fun toHat(): Hat {
        return Hat(this.id, this.displayName, this.modelData, this.description, this.dyeable)
    }

    override fun serialize(): MutableMap<String, Any> {
        val map: MutableMap<String, Any> = mutableMapOf()
        map["id"] = this.id

        return map
    }


}