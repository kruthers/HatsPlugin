package com.kruthers.hats.listeners

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot
import com.kruthers.hats.HatsPlugin
import com.kruthers.hats.classes.AbstractPacket
import com.kruthers.hats.utils.convertToHat
import com.kruthers.hats.utils.isItemAHat
import com.kruthers.hats.utils.isItemAHelmet
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack


class EquipmentPacket(private val pl: HatsPlugin):
    PacketAdapter(pl, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_EQUIPMENT)
{
    override fun onPacketSending(event: PacketEvent) {
        val receiver = event.player
        val packet = WrapperPlayServerEntityEquipment(event.packet.deepClone())
        val item = packet.getItem(ItemSlot.HEAD)

        if (this.pl.hasHatsDisabled(receiver) && isItemAHat(item)) {
            packet.setSlotStackPair(ItemSlot.HEAD, item!!.clone().also {
                it.type = Material.LEATHER_HELMET
            })
            event.packet = packet.handle
        } else if (!this.pl.hasHatsDisabled(receiver) && isItemAHelmet(item)) {
            val hat = convertToHat(item!!.clone())
            packet.setSlotStackPair(ItemSlot.HEAD, hat)
            event.packet = packet.handle
        }
    }

    @Suppress("UNUSED")
    internal class WrapperPlayServerEntityEquipment : AbstractPacket {

        constructor() : super(PacketContainer(TYPE), TYPE) {
            handle.modifier.writeDefaults()
        }

        constructor(packet: PacketContainer?) : super(packet, TYPE) {}

        /**
         * Retrieve Entity ID.
         * Notes: entity's ID
         * @return The current Entity ID
         */
        fun getEntityID(): Int {
            return handle.integers.read(0)
        }

        /**
         * Set Entity ID.
         * @param value - new value.
         */
        fun setEntityID(value: Int) {
            handle.integers.write(0, value)
        }

        /**
         * Retrieve the entity whose equipment will be changed.
         * @param world - the current world of the entity.
         * @return The affected entity.
         */
        fun getEntity(world: World?): Entity {
            return handle.getEntityModifier(world!!).read(0)
        }

        /**
         * Retrieve the entity whose equipment will be changed.
         * @param event - the packet event.
         * @return The affected entity.
         */
        fun getEntity(event: PacketEvent): Entity {
            return getEntity(event.player.world)
        }

        /**
         * Retrieve list of ItemSlot - ItemStack pairs.
         * @return The current list of ItemSlot - ItemStack pairs.
         */
        fun getSlotStackPairs(): MutableList<com.comphenix.protocol.wrappers.Pair<ItemSlot, ItemStack>>? {
            return handle.slotStackPairLists.read(0)
        }

        /**
         * Set a ItemSlot - ItemStack pair.
         * @param slot The slot the item will be equipped in. If matches an existing pair, will overwrite the old one
         * @param item The item to equip
         * @return Whether a pair was overwritten.
         */
         fun setSlotStackPair(slot: ItemSlot?, item: ItemStack?): Boolean {
            val slotStackPairs: MutableList<com.comphenix.protocol.wrappers.Pair<ItemSlot, ItemStack>>? = handle.slotStackPairLists.read(0)
            val removed = slotStackPairs?.removeIf { pair: com.comphenix.protocol.wrappers.Pair<ItemSlot, ItemStack> ->
                pair.getFirst().equals(slot)
            }
            slotStackPairs?.add(com.comphenix.protocol.wrappers.Pair(slot, item))
            handle.slotStackPairLists.write(0, slotStackPairs)
            return removed?: false
        }

        /**
         * Removes the ItemSlot ItemStack pair matching the provided slot. If doesn't exist does nothing
         * @param slot the slot to remove the pair from
         * @return Whether a pair was removed.
         */
        fun removeSlotStackPair(slot: ItemSlot?): Boolean {
            val slotStackPairs: MutableList<com.comphenix.protocol.wrappers.Pair<ItemSlot, ItemStack>>? = handle.slotStackPairLists.read(0)
            val removed = slotStackPairs?.removeIf { pair: com.comphenix.protocol.wrappers.Pair<ItemSlot, ItemStack> ->
                pair.getFirst().equals(slot)
            }
            handle.slotStackPairLists.write(0, slotStackPairs)
            return removed?: false
        }

        /**
         * Check whether the provided is to be affected
         * @param slot the slot to check for
         * @return true if is set, false otherwise
         */
        fun isSlotSet(slot: ItemSlot): Boolean {
            return handle.slotStackPairLists.read(0).stream()
                .anyMatch { pair: com.comphenix.protocol.wrappers.Pair<ItemSlot, ItemStack?> -> pair.first == slot }
        }

        /**
         * Get the item being equipped to the provided slot
         * @param slot the slot to retrieve the item from
         * @return the equipping item, or null if doesn't exist
         */
        fun getItem(slot: ItemSlot?): ItemStack? {
            for (pair in handle.slotStackPairLists.read(0)) {
                if (pair.first.equals(slot)) {
                    return pair.second
                }
            }
            return null
        }

        @Deprecated(
            "This format is no longer supported in Minecraft 1.16+ For 1.16+ use the SlotStack methods",
            ReplaceWith("WrapperPlayServerEntityEquipment.getSlotStackPairs")
        )
        fun getSlot(): ItemSlot {
            return handle.itemSlots.read(0)
        }

        @Deprecated(
            "This format is no longer supported in Minecraft 1.16+ For 1.16+ use the SlotStack methods",
            ReplaceWith("WrapperPlayServerEntityEquipment.setSlotStackPair")
        )
        fun setSlot(value: ItemSlot?) {
            handle.itemSlots.write(0, value)
        }

        /**
         * @return The current Item
         */
        @Deprecated(
            "This format is no longer supported in Minecraft 1.16+\nFor 1.16+ use the SlotStack methodsRetrieve Item.\n"+
                    "<p> Notes: item in slot format",
            ReplaceWith("WrapperPlayServerEntityEquipment.getSlotStackPairs")
        )
        fun getItem(): ItemStack {
            return handle.itemModifier.read(0)
        }

        /**
         * @param value - new value.
         */
        @Deprecated(
            "This format is no longer supported in Minecraft 1.16+\nFor 1.16+ use the SlotStack methods\nSet Item.",
            ReplaceWith("WrapperPlayServerEntityEquipment.setSlotStackPair")
        )
        fun setItem(value: ItemStack?) {
            handle.itemModifier.write(0, value)
        }

        companion object {
            val TYPE = PacketType.Play.Server.ENTITY_EQUIPMENT
        }
    }
}