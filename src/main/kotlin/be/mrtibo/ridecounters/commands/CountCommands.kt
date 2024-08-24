package be.mrtibo.ridecounters.commands

import be.mrtibo.ridecounters.Ridecounters.Companion.manager
import be.mrtibo.ridecounters.commands.RideCommands.NO_ACCESS_MESSAGE
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.data.Database.canAlterRide
import be.mrtibo.ridecounters.data.RideCountEntry
import be.mrtibo.ridecounters.utils.ComponentUtil.mini
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser
import org.incendo.cloud.bukkit.parser.PlayerParser
import org.incendo.cloud.parser.standard.IntegerParser

object CountCommands {

    init {

        val builder = manager.commandBuilder("ridecounter", "rc").literal("count")

        manager.command(builder.literal("get")
            .permission("ridecounters.count.get")
            .required("ride id", IntegerParser.integerParser())
            .required("player", PlayerParser.playerParser())
            .handler { ctx ->
                val rideId : Int = ctx.get("ride id")
                val player : Player = ctx.get("player")
                Database.getRideCountAsync(player, rideId) {
                    it ?: run {
                        ctx.sender().sendMessage("<red>This player hasn't been on this ride yet</red>".mini)
                        return@getRideCountAsync
                    }
                    ctx.sender().sendMessage("<green>The ridecount of <yellow>${player.name}</yellow> on <yellow>${it.ride.name}</yellow> is <yellow>${it.count}</yellow></green>".mini)
                }
            }
        )

            .command(builder.literal("increment")
                .permission("ridecounters.count.increment")
                .required("ride id", IntegerParser.integerParser())
                .required("player", OfflinePlayerParser.offlinePlayerParser())
                .senderType(Player::class.java)
                .handler{ ctx ->
                    val rideId : Int = ctx.get("ride id")
                    val sender = ctx.sender()
                    val player : OfflinePlayer = ctx.get("player")
                    sender.canAlterRide(rideId) { _, alter ->
                        if(alter) {
                            Database.incrementRideCounter(player, rideId) {
                                if(it) {
                                    ctx.sender().sendMessage("<green>Ridecount incremented".mini)
                                } else {
                                    ctx.sender().sendMessage("<red>Something went wrong, nothing changed.".mini)
                                }
                            }
                        } else sender.sendMessage(NO_ACCESS_MESSAGE)
                    }
                }
            )

            .command(builder.literal("top")
                .permission("ridecounters.count.top")
                .required("ride id", IntegerParser.integerParser())
                .required("limit", IntegerParser.integerParser(1, 50))
                .handler { ctx ->
                    val rideId : Int = ctx.get("ride id")
                    val limit : Int = ctx.get("limit")
                    Database.getTopCountAsync(rideId, limit) { counts ->
                        if(counts.isNullOrEmpty()) {
                            ctx.sender().sendMessage("<red>No top ridecount known".mini)
                            return@getTopCountAsync
                        }

                        var message = "<green>This is the top <yellow>$limit</yellow> ridecount for <yellow>${counts[0].ride.name}"

                        for ((i, count: RideCountEntry) in counts.withIndex()) {
                            message += "<br><yellow>#${i+1} (${count.count}) <gray>-</gray> ${count.playerName}"
                        }

                        ctx.sender().sendMessage(message.mini)

                    }
                }
            )

            .command(builder.literal("set")
                .permission("ridecounters.count.set")
                .required("ride id", IntegerParser.integerParser())
                .required("player", OfflinePlayerParser.offlinePlayerParser())
                .required("new count", IntegerParser.integerParser(0, 1000000))
                .senderType(Player::class.java)
                .handler { ctx ->
                    val sender = ctx.sender()
                    val rideId : Int = ctx.get("ride id")
                    sender.canAlterRide(rideId) { _, alter ->
                        if(alter) {
                            Database.setRidecount(rideId, ctx.get<OfflinePlayer>("player"), ctx.get("new count")) {
                                if(it > 0) {
                                    ctx.sender().sendMessage("<green>Ridecount updated".mini)
                                    return@setRidecount
                                }
                                ctx.sender().sendMessage("<red>Something went wrong".mini)
                            }
                        } else sender.sendMessage(NO_ACCESS_MESSAGE)

                    }
                }
            )

            .command(builder.literal("removeplayer")
                .permission("ridecounters.count.removeplayer")
                .required("ride id", IntegerParser.integerParser())
                .required("player", OfflinePlayerParser.offlinePlayerParser())
                .senderType(Player::class.java)
                .handler { ctx ->
                    val sender = ctx.sender()
                    val player : OfflinePlayer = ctx.get("player")
                    val rideId : Int = ctx.get("ride id")
                    sender.canAlterRide(rideId) {ride, canAlter ->
                        if(canAlter) {
                            Database.clearRidecount(rideId, player) {rows ->
                                if(rows > 0) sender.sendMessage("<green>Ridecount of ${player.name} removed from ${ride?.name}".mini)
                                else sender.sendMessage("<red>Nothing changed, this player probably hasn't been on this ride before".mini)
                            }
                        } else sender.sendMessage(NO_ACCESS_MESSAGE)
                    }
                }
            )

            .command(builder.literal("clear")
                .permission("ridecounters.count.clear")
                .required("ride id", IntegerParser.integerParser())
                .senderType(Player::class.java)
                .handler { ctx ->
                    val sender = ctx.sender()
                    val rideId : Int = ctx.get("ride id")
                    sender.canAlterRide(rideId) { _, canAlter ->
                        if(canAlter) Database.clearRidedata(rideId) { rows -> sender.sendMessage("<green>Data was cleared from this ride, <yellow>$rows</yellow> entries were removed.".mini) }
                        else sender.sendMessage(NO_ACCESS_MESSAGE)
                    }
                }
            )
    }
}