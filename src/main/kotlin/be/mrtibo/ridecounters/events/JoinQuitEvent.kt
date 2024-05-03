package be.mrtibo.ridecounters.events

import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE
import be.mrtibo.ridecounters.cache.OwnedRides
import be.mrtibo.ridecounters.data.Database
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object JoinQuitEvent : Listener {

    init {
        INSTANCE.server.pluginManager.registerEvents(this, INSTANCE)
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        Database.rememberPlayer(e.player)
        OwnedRides.update(e.player)
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        OwnedRides.map.remove(e.player)
    }

}