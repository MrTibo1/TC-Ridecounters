package be.mrtibo.ridecounters.commands

import be.mrtibo.ridecounters.Ridecounters
import be.mrtibo.ridecounters.Ridecounters.Companion.commandManager
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.utils.ComponentUtil.mini
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.incendo.cloud.paper.util.sender.PlayerSource
import org.incendo.cloud.parser.standard.IntegerParser
import org.incendo.cloud.parser.standard.StringParser


object RideCommands {

    val NO_ACCESS_MESSAGE = Ridecounters.INSTANCE.config.getString("messages.ride_no_access")?.mini
        ?: Component.text("You don't have access to this ride").color(TextColor.color(255, 0, 0))

    init {

        val builder = commandManager.commandBuilder("ridecounter", "rc").literal("ride")

            commandManager.command(builder.literal("create")
                .permission("ridecounters.ride.create")
                .required("ride name", StringParser.greedyStringParser())
                .senderType(PlayerSource::class.java)
                .handler{ ctx ->
                    val rideName : String = ctx.get("ride name")
                    val sender = ctx.sender().source()
                    Database.createRideAsync(rideName, sender.uniqueId) { msg, error ->
                        if(error == null && msg != null) {
                            sender.sendMessage(msg.mini)
                            return@createRideAsync
                        }
                        sender.sendMessage("<red>Something went wrong!</red><br><dark_red>$error".mini)
                    }
                }
            )

            .command(builder.literal("delete")
                .permission("ridecounters.ride.delete")
                .required("ride id", IntegerParser.integerParser())
                .senderType(PlayerSource::class.java)
                .handler { ctx ->
                    val rideId : Int = ctx.get("ride id")
                    val p : Player = ctx.sender().source()
                    Database.deleteRideAsync(rideId) {linesChanged ->
                        if (linesChanged > 0){
                            p.sendMessage("<red>Deleted ride <yellow>$rideId</yellow>.".mini)
                            return@deleteRideAsync
                        }
                        p.sendMessage("<red>Nothing changed.</red>".mini)
                    }
                }
            )

            .command(builder.literal("list")
                .permission("ridecounters.ride.list")
                .handler { ctx ->
                    val sender = ctx.sender().source()
                    Database.getAllRidesAsync { rides ->
                        if(rides.isEmpty()) {
                            sender.sendMessage("<red>No rides registered.</red>".mini)
                            return@getAllRidesAsync
                        }
                        var message = "<green>There are <yellow>${rides.size}</yellow> rides registered <gray>(id, name, owner)"
                        for (ride in rides) {
                            message += ("<br><yellow>${ride.id}</yellow> <gray>-</gray> <reset>${ride.name} <gray>-</gray> <reset><yellow>${ride.ownerName}</yellow>")
                        }
                        sender.sendMessage(message.mini)
                    }
                }
            )

            .command(builder.literal("name")
                .permission("ridecounters.ride.setname")
                .required("ride id", IntegerParser.integerParser())
                .required("new name", StringParser.greedyStringParser())
                .handler { ctx ->
                    val source = ctx.sender().source()
                    val rideId : Int = ctx.get("ride id")
                    val newName : String = ctx.get("new name")
                    Database.setName(rideId, newName) {rows ->
                        if(rows > 0) source.sendMessage("<green>Ride name updated to $newName".mini)
                        else source.sendMessage("<red>Something went wrong while updating this ride's name".mini)
                    }
                }
            )

            .command(builder.literal("displayname")
                .permission("ridecounters.ride.setdisplayname")
                .required("ride id", IntegerParser.integerParser())
                .required("displayname", StringParser.greedyStringParser())
                .handler { ctx ->
                    val rideId : Int = ctx.get("ride id")
                    val source = ctx.sender().source()
                    Database.setDisplayName(rideId, ctx.get("displayname")) { rowsChanged ->
                        if(rowsChanged > 0) {
                            source.sendMessage("<green>Display board name updated to ${ctx.get<String>("displayname")}".mini)
                            return@setDisplayName
                        }
                        source.sendMessage("<red>Something went wrong while updating the display board name</red>".mini)
                    }
                }
            )
    }

}