package be.mrtibo.ridecounters.commands

import be.mrtibo.ridecounters.Ridecounters.Companion.manager
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.data.RideCountEntry
import be.mrtibo.ridecounters.utils.ComponentUtil.mini
import org.bukkit.entity.Player

object PlayerCommands {

    init {

        manager.command(
            manager.commandBuilder("ridecount")
                .permission("ridecounter.ridecount")
                .handler{ ctx ->
                    if(ctx.sender !is Player) return@handler
                    val p = ctx.sender as Player
                    Database.getPlayerRidecounters(p) {
                        if(it.isNullOrEmpty()) {
                            p.sendMessage("<red>You haven't been on any rides yet!</red>".mini)
                            return@getPlayerRidecounters
                        }
                        var message = "<dark_aqua>You have been on <aqua>${it.size}</aqua> ride"
                        if (it.size > 1) message += "s"
                        for (entry : RideCountEntry in it) {
                            message += "<br><aqua>${entry.count}<gray>x <aqua>${entry.ride.name}"
                        }
                        p.sendMessage(message.mini)
                    }
                }
        )

    }

}