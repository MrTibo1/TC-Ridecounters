package be.mrtibo.ridecounters.commands

import be.mrtibo.ridecounters.Ridecounters.Companion.commandManager
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.data.RideCountEntry
import be.mrtibo.ridecounters.utils.ComponentUtil.mini
import org.bukkit.entity.Player
import org.incendo.cloud.paper.util.sender.PlayerSource

object PlayerCommands {

    init {

        commandManager.command(
            commandManager.commandBuilder("ridecount")
                .permission("ridecounter.ridecount")
                .senderType(PlayerSource::class.java)
                .handler{ ctx ->
                    val p = ctx.sender().source()
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