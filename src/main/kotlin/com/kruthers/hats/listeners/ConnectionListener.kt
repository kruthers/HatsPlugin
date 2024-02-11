package com.kruthers.hats.listeners

import com.kruthers.hats.classes.HatPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class ConnectionListener: Listener {
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        HatPlayer.create(e.player)
    }

    @EventHandler
    fun onPlayerLeave(e: PlayerQuitEvent) {
        HatPlayer.destroy(e.player)
    }
}