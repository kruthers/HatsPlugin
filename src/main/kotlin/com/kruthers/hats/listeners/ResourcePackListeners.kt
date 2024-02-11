package com.kruthers.hats.listeners

import com.kruthers.hats.HatManager
import com.kruthers.hats.HatsPlugin
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerResourcePackStatusEvent

class ResourcePackListeners(private val plugin: HatsPlugin): Listener {
    private val trackedStatus = mutableListOf(
        PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD,
        PlayerResourcePackStatusEvent.Status.DECLINED
    )
    private val mm = MiniMessage.miniMessage()

    @EventHandler
    fun onRPStatus(event: PlayerResourcePackStatusEvent) {
        if (
            plugin.config.getBoolean("no_rp_auto_disable") && trackedStatus.contains(event.status) &&
            !HatManager.hasHatsForceEnabled(event.player)
        ) {
            this.plugin.logger.info("Hats disabled for ${event.player.name}")
            HatManager.disableHats(event.player)
            event.player.sendMessage(mm.deserialize(
                plugin.config.getString("messages.disabled")?:
                "<red><b>Hats Notice</b><br>Looks like you have refused the server resource pack, hats have been disabled.<br><i>Override this with <click:run_command:'/hats force_enable'>/hats force_enable</click>"
            ))
        }
    }

}