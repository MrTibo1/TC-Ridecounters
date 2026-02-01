package be.mrtibo.ridecounters.commands

import be.mrtibo.ridecounters.Ridecounters
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.data.records.RidecountTotalRecord
import be.mrtibo.ridecounters.utils.ComponentUtil.mini
import kotlinx.coroutines.withContext
import org.incendo.cloud.annotation.specifier.Range
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.bukkit.data.SinglePlayerSelector
import org.incendo.cloud.paper.util.sender.Source

@Command("ridecounter|rc")
object RidecountCommands {

    @Command("total <player> <rideId>")
    @Permission("ridecounters.total")
    suspend fun getRideCount(
        source: Source,
        @Argument(value = "player")
        playerSelector: SinglePlayerSelector,
        @Argument(value = "rideId", suggestions = "rideIds")
        rideId: String
    ) {
        val ridecount = Database.getTotalRidecount(playerSelector.single().uniqueId.toString(), rideId)
        val player = playerSelector.single()
        withContext(Ridecounters.mainThreadDispatcher) {
            if (ridecount == null) {
                source.source().sendMessage("<gray>${player.name} hasn't been on this ride".mini)
            } else {
                source.source().sendMessage("<green>${player.name} has been on ${ridecount.ride.name}<reset> <green>${ridecount.total} times".mini)
            }
        }
    }

    @Command("set <player> <rideId> <value>")
    @Permission("ridecounters.set")
    suspend fun setCounter(
        source: Source,
        @Argument("player")
        playerSelector: SinglePlayerSelector,
        @Argument(value = "rideId", suggestions = "rideIds")
        rideId: String,
        @Argument(value = "value") @Range(min = "0")
        value: Int
    ) {
        val player = playerSelector.single()
        val sender = source.source()
        val new = Database.setCounter(player.uniqueId.toString(), rideId, value)
        withContext(Ridecounters.mainThreadDispatcher) {
            if (new == null) {
                sender.sendMessage("<gray>Ride with this ID doesn't exist".mini)
            } else {
                sender.sendMessage("<green>Updated ridecounter of ${new.player.username} for ${new.ride.name}<reset> <green>to ${new.total}".mini)
            }
        }
    }

    @Command("increment <player> <rideId>")
    @Permission("ridecounters.increment")
    suspend fun incrementCounter(
        source: Source,
        @Argument(value = "player")
        playerSelector: SinglePlayerSelector,
        @Argument(value = "rideId", suggestions = "rideIds")
        rideId: String
    ) {
        val new = Database.incrementCounter(playerSelector.single().uniqueId.toString(), rideId)
        val sender = source.source()
        withContext(Ridecounters.mainThreadDispatcher) {
            if (new == null) {
                sender.sendMessage("<gray>Ride with this ID doesn't exist".mini)
            } else {
                sender.sendMessage("<green>Incremented ridecounter of ${new.player.username} for ${new.ride.name}<reset> <green>to ${new.total}".mini)
            }
        }
    }

    @Command("top <rideId> <limit>")
    @Permission("ridecounters.top")
    suspend fun topCounters(
        source: Source,
        @Argument(value = "rideId", suggestions = "rideIds")
        rideId: String,
        @Argument(value = "limit") @Range(min = "1")
        limit: Int
    ) {
        val topTotalCounters = Database.getTopTotalRidecounter(rideId, limit)
        val ride = topTotalCounters?.ride
        val sender = source.source()
        withContext(Ridecounters.mainThreadDispatcher) {
            if(ride == null || topTotalCounters.data.isEmpty()) {
                sender.sendMessage("<gray>No top ridecount known".mini)
                return@withContext
            }

            var message = "<green>This is the top <yellow>$limit</yellow> ridecount for <yellow>${ride.name}<reset>"

            for ((i, count: RidecountTotalRecord) in topTotalCounters.data.withIndex()) {
                message += "<br><yellow>#${i+1} (${count.total}) <gray>-</gray> ${count.player.username}"
            }
            sender.sendMessage(message.mini)

        }
    }

}