package be.mrtibo.ridecounters.events

import be.mrtibo.ridecounters.Ridecounters
import be.mrtibo.ridecounters.data.Database
import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class JoinEvent: Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        Ridecounters.INSTANCE.launch {
            Database.savePlayer(event.player)
        }
    }

}