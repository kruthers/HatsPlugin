package com.kruthers.hats.commands

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.arguments.standard.BooleanArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.BukkitCommandManager
import cloud.commandframework.context.CommandContext
import cloud.commandframework.meta.CommandMeta
import com.kruthers.hats.classes.Hat
import com.kruthers.hats.HatsPlugin
import com.kruthers.hats.commands.arguments.UnusedHatModelArgument
import com.kruthers.hats.utils.HatNotFoundException
import com.kruthers.hats.utils.ModelIdNotUnique
import com.kruthers.hats.utils.isItemAHelmet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.incendo.interfaces.core.click.ClickHandler
import org.incendo.interfaces.core.transform.types.PaginatedTransform
import org.incendo.interfaces.core.util.Vector2
import org.incendo.interfaces.kotlin.paper.asElement
import org.incendo.interfaces.kotlin.paper.buildChestInterface
import org.incendo.interfaces.kotlin.paper.open
import org.incendo.interfaces.paper.PlayerViewer
import org.incendo.interfaces.paper.element.ItemStackElement
import org.incendo.interfaces.paper.pane.ChestPane
import org.incendo.interfaces.paper.view.PlayerInventoryView

class HatsManagementCommands(private val plugin: HatsPlugin, manager: BukkitCommandManager<CommandSender>) {
    private val mm = MiniMessage.miniMessage()
    private val builder = manager.commandBuilder("hats", ArgumentDescription.of("Core hats command"))
    init {
        //add hat management commands
        manager.command(builder
            .meta(CommandMeta.DESCRIPTION, "List all hats")
            .literal("list")
            .permission("hats.list")
            .senderType(Player::class.java)
            .handler(this::showMenu)
        )
        manager.command(builder
            .meta(CommandMeta.DESCRIPTION, "Add a new hat")
            .literal("add")
            .argument(StringArgument.of("name"))
            .argument(UnusedHatModelArgument.of("moduleData", plugin))
            .argument(StringArgument.quoted("displayName"))
            .argument(StringArgument.quoted("description"))
            .argument(BooleanArgument.optional("dyeable", true))
            .permission("hats.add")
            .handler(this::addHat)
        )
        manager.command(builder
            .meta(CommandMeta.DESCRIPTION, "Adds a new hat from a hat item already")
            .literal("add_from_item")
            .argument(StringArgument.of("name"))
            .argument(BooleanArgument.optional("dyeable", true))
            .senderType(Player::class.java)
            .permission("hats.add")
            .handler(this::addHatFromItem)
        )
        manager.command(builder
            .meta(CommandMeta.DESCRIPTION, "Remove a new hat")
            .literal("remove")
            .argument(StringArgument.of("name"))
            .permission("hats.remove")
            .handler(this::removeHat)
        )
        manager.command(builder
            .meta(CommandMeta.DESCRIPTION, "Modify a hat's display name")
            .literal("modify")
            .argument(StringArgument.of("name"))
            .literal("displayName")
            .argument(StringArgument.greedy("input"))
            .permission("hats.modify")
            .handler(this::modifyHatDisplayName)
        )
        manager.command(builder
            .meta(CommandMeta.DESCRIPTION, "Modify a hat's description")
            .literal("modify")
            .argument(StringArgument.of("name"))
            .literal("description")
            .argument(StringArgument.greedy("input"))
            .permission("hats.modify")
            .handler(this::modifyHatDescription)
        )
        manager.command(builder
            .meta(CommandMeta.DESCRIPTION, "Modify a hat's custom model data ID")
            .literal("modify")
            .argument(StringArgument.of("name"))
            .literal("modelData")
            .argument(UnusedHatModelArgument.of("input", plugin))
            .permission("hats.modify")
            .handler(this::modifyHatModelID)
        )
        manager.command(builder
            .meta(CommandMeta.DESCRIPTION, "Modify if a hat is dyeable")
            .literal("modify")
            .argument(StringArgument.of("name"))
            .literal("dyeable")
            .argument(BooleanArgument.of("input"))
            .permission("hats.modify")
            .handler(this::modifyHatDyeAble)
        )
    }


    private fun addHat(context: CommandContext<CommandSender>) {
        val id: String = context.get("name")
        val customModelData: Int = context.get("moduleData")
        val displayName: String = context.get("displayName")
        val description: String = context.get("description")
        val dyable: Boolean = context.get("dyeable")
        val hat = Hat(id, displayName, customModelData, description, dyable)
        this.plugin.addHat(hat)

        context.sender.sendMessage(mm.deserialize("<green>Created new hat with a custom model data of <gray>${customModelData}"))
    }

    private fun addHatFromItem(context: CommandContext<CommandSender>) {
        val id: String = context.get("name")
        val dyable: Boolean = context.get("dyeable")

        val player = context.sender as Player

        val item = player.inventory.itemInMainHand
        if (item.type == Material.AIR) {
            player.sendMessage(Component.text("You must have a hat item in your hand to convert", NamedTextColor.RED))
            return
        } else if (isItemAHelmet(item, this.plugin)) {
            val customModelData = item.itemMeta.customModelData - this.plugin.config.getInt("model_data_start.leather_hat")
            val displayName = item.itemMeta.displayName()?: Component.text(id)
            val lore = item.lore()?: mutableListOf()

            val hat = Hat(id, displayName, customModelData, lore, dyable)
            try {
                this.plugin.addHat(hat)
                player.sendMessage(mm.deserialize("<green>Created new hat with a custom model data of <gray>${customModelData}"))
            } catch (error: ModelIdNotUnique) {
                player.sendMessage(Component.text("Unable to create hat, looks like the custom module data is already in use", NamedTextColor.RED))
            }

        }

    }

    private fun removeHat(context: CommandContext<CommandSender>) {
        val id: String = context.get("name")
        val hat = this.plugin.removeHat(id)
        if (hat != null) {
            val tags = TagResolver.resolver(
                Placeholder.parsed("command", "/hats add ${hat.getID()} ${hat.getModelData()} ${hat.getDisplayName()} ${hat.getDescription()} ${hat.isDyeable()}")
            )
            context.sender.sendMessage(mm.deserialize("<red>Removed hat $id from the system. <italic>Is this a mistake? Get it back <u><click:suggest_command:'<command>'>here</click></u>", tags))
        } else {
            throw HatNotFoundException(id)
        }
    }

    private fun modifyHatDisplayName(context: CommandContext<CommandSender>) {
        val id: String = context.get("name")
        val displayName: String = context.get("input")

        val hat = this.plugin.hats[id]
        if (hat != null) {
            hat.setDisplayName(mm.deserialize(displayName))
            this.plugin.hats[id] = hat
            val tags = TagResolver.resolver(
                Placeholder.parsed("name", id),
                Placeholder.parsed("display_name", displayName)
            )
            context.sender.sendMessage(mm.deserialize("<green>Successfully updated display name of <name> to: <gray>\"<display_name>\"", tags))
        } else {
            throw HatNotFoundException(id)
        }
    }

    private fun modifyHatDescription(context: CommandContext<CommandSender>) {
        val id: String = context.get("name")
        val description: String = context.get("input")

        val hat = this.plugin.hats[id]
        if (hat != null) {
            hat.setDescription(description)
            this.plugin.hats[id] = hat
            val tags = TagResolver.resolver(
                Placeholder.parsed("name", id),
                Placeholder.parsed("description", description)
            )
            context.sender.sendMessage(mm.deserialize("<green>Successfully updated description of <name> to: <gray>\"<description>\"", tags))
        } else {
            throw HatNotFoundException(id)
        }

    }

    private fun modifyHatModelID(context: CommandContext<CommandSender>) {
        val id: String = context.get("name")
        val customModelData: Int = context.get("input")

        val hat = this.plugin.hats[id]
        if (hat != null) {
            hat.setModelData(customModelData)
            this.plugin.addHat(hat)
            val tags = TagResolver.resolver(
                Placeholder.parsed("name", id),
                Placeholder.parsed("model_data", "$customModelData")
            )
            context.sender.sendMessage(mm.deserialize("<green>Successfully updated custom model data of <name> to: <gray><model_data>", tags))
        } else {
            throw HatNotFoundException(id)
        }
    }

    private fun modifyHatDyeAble(context: CommandContext<CommandSender>) {
        val id: String = context.get("name")
        val dyeable: Boolean = context.get("input")

        val hat = this.plugin.hats[id]
        if (hat != null) {
            hat.setDyeable(dyeable)
            this.plugin.hats[id] = hat
            val tags = TagResolver.resolver(
                Placeholder.parsed("name", id),
            )
            if (dyeable) {
                context.sender.sendMessage(mm.deserialize("<green>Hat <name> is now dye-able", tags))
            } else {
                context.sender.sendMessage(mm.deserialize("<red>Hat <name> is no longer dye-able. Warning this may cause issues with people who already have it", tags))
            }
        } else {
            throw HatNotFoundException(id)
        }
    }

    private fun showMenu(context: CommandContext<CommandSender>) {
        val player = context.sender as Player

        val hats: ArrayList<Hat> = ArrayList(plugin.hats.values.sortedBy { it.getModelData() })

        val menu = buildChestInterface {
            title = Component.text("Hats Menu")
            rows = 6
            updates(true, 5)

            //create the footer
            withTransform { view ->
                val fill: ItemStackElement<ChestPane> = basicItem(Material.GRAY_STAINED_GLASS_PANE).asElement(ClickHandler.cancel())
                for (x in 0..8) {
                    view[x,0] = fill
                    view[x,5] = fill
                }

                fun getHelpComponent(click: String, action: String): Component {
                    return Component.text("[", NamedTextColor.GRAY)
                        .append(Component.text(click, NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("] ", NamedTextColor.GRAY))
                        .append(Component.text(action, NamedTextColor.AQUA))
                }
                view[8,0] = createSkull(Component.text("Help").color(NamedTextColor.GOLD), "MHF_Question").also {
                    it.lore(mutableListOf(
                        getHelpComponent("Right-Click", "Get modification command in chat"),
                        getHelpComponent("Left-Click", "Give the item to yourself"),
                        getHelpComponent("shift-Right-Click", "Generate vanilla give command")
                    ))
                }.asElement(ClickHandler.cancel())
            }

            val reactiveTransform: PaginatedTransform<ItemStackElement<ChestPane>, ChestPane, PlayerViewer> = PaginatedTransform(
                Vector2.at(0,1),
                Vector2.at(8,4),
                hats.map { hat ->
                    getHatElement(hat)
                }
            )
            reactiveTransform.backwardElement(Vector2.at(1,0)) { transform ->
                createSkull(Component.text("Previous Page"), "MHF_ArrowLeft").asElement{
                    transform.previousPage()
                }
            }
            reactiveTransform.backwardElement(Vector2.at(7,0)) { transform ->
                createSkull(Component.text("Previous Page"), "MHF_ArrowRight").asElement{
                    transform.nextPage()
                }
            }

            addTransform(reactiveTransform)
            withCloseHandler { _, view ->
                PlayerInventoryView.forPlayer(view.viewer().player())?.close()
            }
        }

        player.open(menu)
    }


    //util
    private fun getHatElement(hat: Hat): ItemStackElement<ChestPane> {
        return hat.getItem().also { it.also {
            val lore: MutableList<Component> = mutableListOf(
                Component.text("Hat ID: ", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(hat.getID(), NamedTextColor.GRAY)),
                Component.text("Custom Model Data: ", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(hat.getModelData(), NamedTextColor.GRAY)),
                Component.empty()
            )
            lore.addAll(hat.getDescription())
            it.lore(lore)
        }}.asElement { handler ->
            val player = handler.viewer().player()
            when (handler.cause().click) {
                ClickType.RIGHT -> {
                    val tags = TagResolver.resolver(
                        Placeholder.parsed("hat", hat.getID()),
                        Placeholder.parsed("command", "/hat modify <hat>")
                    )
                    player.sendMessage(mm.deserialize(
                        "<hover:show_text:'<command> (displayName | description | modelData | dyeable)'><click:suggest_command:'<command>'><grey>Click here to modify hat <i><hat></i></click></hover>",
                        tags
                    ))
                    handler.cancel(true)
                }
                ClickType.LEFT -> {
                    if (player.hasPermission("hats.give")) {
                        player.inventory.addItem(hat.getItem())
                        player.sendMessage(mm.deserialize("<green><i>You have been given 1x ${hat.getID()}"))
                    } else {
                        player.sendMessage(Bukkit.getServer().permissionMessage())
                    }
                    handler.cancel(true)
                }
                ClickType.SHIFT_RIGHT -> {
                    //gives a copy command for the vanilla give command
                    val gs = GsonComponentSerializer.gson()
                    val tags = TagResolver.resolver(
                        Placeholder.parsed("hat", hat.getID()),
                        Placeholder.parsed("command", "/give @s leather_helmet{display:{Name:${gs.serialize(hat.getDisplayName())},Lore:[${hat.getDescription().joinToString(","){ gs.serialize(it) }}]},CustomModelData:${hat.getModelData()},Unbreakable:1b}")
                    )
                    player.sendMessage(mm.deserialize(
                        "<hover:show_text:'<command>'><click:COPY_TO_CLIPBOARD:'<command>'><grey>Click here to copy a vanilla give command for the hat <i><hat></i></click></hover>",
                        tags
                    ))
                    handler.cancel(true)
                }
                else -> {
                    handler.cancel(true)
                }
            }
        }
    }

    private fun basicItem(material: Material, name: Component = Component.text("")) = ItemStack(material).also {
        it.itemMeta = it.itemMeta.also { meta -> meta.displayName(name) }
    }

    private fun createSkull(name: Component, owner: String): ItemStack = ItemStack(Material.PLAYER_HEAD).also {
        val owningPlayer = Bukkit.getOfflinePlayer(owner)
        it.itemMeta = it.itemMeta.also { meta ->
            meta.displayName(name)
            (meta as SkullMeta).owningPlayer = owningPlayer
        }
    }

}