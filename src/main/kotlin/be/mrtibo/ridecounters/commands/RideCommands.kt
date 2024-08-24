package be.mrtibo.ridecounters.commands

import be.mrtibo.ridecounters.Ridecounters
import be.mrtibo.ridecounters.Ridecounters.Companion.manager
import be.mrtibo.ridecounters.cache.OwnedRides
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.data.Database.canAlterRide
import be.mrtibo.ridecounters.utils.ComponentUtil.mini
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.incendo.cloud.parser.standard.IntegerParser
import org.incendo.cloud.parser.standard.StringParser


object RideCommands {

    val NO_ACCESS_MESSAGE = Ridecounters.INSTANCE.config.getString("messages.ride_no_access")?.mini
        ?: Component.text("You don't have access to this ride").color(TextColor.color(255, 0, 0))

    init {

        val builder = manager.commandBuilder("ridecounter", "rc").literal("ride")

            manager.command(builder.literal("create")
                .permission("ridecounters.ride.create")
                .required("ride name", StringParser.greedyStringParser())
                .senderType(Player::class.java)
                .handler{ ctx ->
                    val rideName : String = ctx.get("ride name")
                    Database.createRideAsync(rideName, ctx.sender().uniqueId) { msg, error ->
                        if(error == null && msg != null) {
                            ctx.sender().sendMessage(msg.mini)
                            OwnedRides.update(ctx.sender())
                            return@createRideAsync
                        }
                        ctx.sender().sendMessage("<red>Something went wrong!</red><br><dark_red>$error".mini)
                    }
                }
            )

            .command(builder.literal("delete")
                .permission("ridecounters.ride.delete")
                .required("ride id", IntegerParser.integerParser())
                .senderType(Player::class.java)
                .handler { ctx ->
                    val rideId : Int = ctx.get("ride id")
                    val p : Player = ctx.sender()
                    p.canAlterRide(rideId) { _, alter ->
                        if(alter) {
                            Database.deleteRideAsync(rideId) {linesChanged ->
                                if (linesChanged > 0){
                                    ctx.sender().sendMessage("<red>Deleted ride <yellow>$rideId</yellow>.".mini)
                                    return@deleteRideAsync
                                }
                                ctx.sender().sendMessage("<red>Nothing changed.</red>".mini)
                            }
                        } else p.sendMessage(NO_ACCESS_MESSAGE)

                    }
                }
            )

            .command(builder.literal("list")
                .permission("ridecounters.ride.list")
                .handler { ctx ->
                    Database.getAllRidesAsync { rides ->
                        if(rides.isEmpty()) {
                            ctx.sender().sendMessage("<red>No rides registered.</red>".mini)
                            return@getAllRidesAsync
                        }
                        var message = "<green>There are <yellow>${rides.size}</yellow> rides registered <gray>(id, name, owner)"
                        for (ride in rides) {
                            message += ("<br><yellow>${ride.id}</yellow> <gray>-</gray> <reset>${ride.name} <gray>-</gray> <reset><yellow>${ride.ownerName}</yellow>")
                        }
                        ctx.sender().sendMessage(message.mini)
                    }
                }
            )

            .command(builder.literal("name")
                .permission("ridecounters.ride.setname")
                .required("ride id", IntegerParser.integerParser())
                .required("new name", StringParser.greedyStringParser())
                .senderType(Player::class.java)
                .handler { ctx ->
                    val p = ctx.sender()
                    val rideId : Int = ctx.get("ride id")
                    val newName : String = ctx.get("new name")
                    p.canAlterRide(rideId) {_, alter ->
                        if(alter) {
                            Database.setName(rideId, newName) {rows ->
                                if(rows > 0) p.sendMessage("<green>Ride name updated to $newName".mini)
                                else p.sendMessage("<red>Something went wrong while updating this ride's name".mini)
                            }
                        } else p.sendMessage(NO_ACCESS_MESSAGE)
                    }
                }
            )

            .command(builder.literal("displayname")
                .permission("ridecounters.ride.setdisplayname")
                .required("ride id", IntegerParser.integerParser())
                .required("displayname", StringParser.greedyStringParser())
                .senderType(Player::class.java)
                .handler { ctx ->
                    val rideId : Int = ctx.get("ride id")
                    val p : Player = ctx.sender()
                    p.canAlterRide(rideId) { _, alter ->
                        if(alter) {
                            Database.setDisplayName(rideId, ctx.get("displayname")) { rowsChanged ->
                                if(rowsChanged > 0) {
                                    ctx.sender().sendMessage("<green>Display board name updated to ${ctx.get<String>("displayname")}".mini)
                                    return@setDisplayName
                                }
                                ctx.sender().sendMessage("<red>Something went wrong while updating the display board name</red>".mini)
                            }
                        } else p.sendMessage(NO_ACCESS_MESSAGE)
                    }
                }
            )
    }

}