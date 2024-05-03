package be.mrtibo.ridecounters.cache

import be.mrtibo.ridecounters.data.Database
import org.bukkit.entity.Player

object OwnedRides {

    fun update(p: Player) {
        Database.getOwnedRides(p) {
            map[p] = it
        }
    }

    val map = mutableMapOf<Player, List<Int>>()

}