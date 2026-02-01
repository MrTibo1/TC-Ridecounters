package be.mrtibo.ridecounters.commands

import be.mrtibo.ridecounters.Ridecounters
import be.mrtibo.ridecounters.Ridecounters.Companion.commandManager
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.utils.ComponentUtil.mini
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotation.specifier.Range
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.parser.Parser
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.paper.util.sender.PlayerSource
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.standard.IntegerParser
import org.incendo.cloud.parser.standard.StringParser
import java.sql.SQLIntegrityConstraintViolationException

@Command("ridecounter|rc")
object RideCommands {

    @Command("create <rideId> <rideName>")
    @Permission("ridecounters.create")
    suspend fun createRide(
        source: Source,
        @Argument(value = "rideId")
        rideId: String,
        @Argument(value = "rideName") @Greedy
        rideName: String
    ) {
        val result = Database.runCatching {
            createRide(rideId, rideName)
        }
        withContext(Ridecounters.mainThreadDispatcher) {
            if (!result.isSuccess) {
                source.source().sendMessage("<red>Couldn't create ride. Ride with this ID already exists".mini)
            } else {
                source.source().sendMessage("<green>Ride created".mini)
            }
        }
    }

    @Command("delete <rideId>")
    @Permission("ridecounters.delete")
    suspend fun deleteRide(
        source: Source,
        @Argument(value = "rideId", suggestions = "rideIds")
        rideId: String
    ) {
        val deleted = Database.deleteRide(rideId)
        withContext(Ridecounters.mainThreadDispatcher) {
            if (deleted) {
                source.source().sendMessage("<green>Ride deleted".mini)
            } else {
                source.source().sendMessage("<gray>No ride with ID \"$rideId\" exists".mini)
            }
        }
    }

    @Command("list [page]")
    suspend fun listRides(
        source: Source,
        @Default("1") @Argument(value = "page") @Range(min = "1")
        page: Int
    ) {
        val rides = Database.getRides(20, (page-1) * 20)
        withContext(Ridecounters.mainThreadDispatcher) {
            if (rides.isEmpty()) {
                source.source().sendMessage("<gray>No rides found".mini)
            } else {
                var message = "<green>There are <yellow>${rides.size}</yellow> rides being tracked"
                for (ride in rides) {
                    message += ("<br><yellow>${ride.id}</yellow> <gray>-</gray> <yellow>${ride.name}")
                }
                message += ("<br><gray>Page $page")
                source.source().sendMessage(message.mini)
            }
        }
    }

    @Command("rename <rideId> <name>")
    suspend fun changeName(
        source: Source,
        @Argument(value = "rideId", suggestions = "rideIds") rideId: String,
        @Argument(value = "name") @Greedy name: String
    ) {
        var update = false
        val result = Database.runCatching {
            update = Database.changeName(rideId, name)
        }
        withContext(Ridecounters.mainThreadDispatcher) {
            if (result.isSuccess && update) {
                source.source().sendMessage("<green>Ride updated".mini)
            } else {
                source.source().sendMessage("<gray>No ride with this ID exists".mini)
            }
        }
    }

    @Command("alternative <rideId> <name>")
    suspend fun changeAlternative(
        source: Source,
        @Argument(value = "rideId", suggestions = "rideIds") rideId: String,
        @Argument(value = "name") @Greedy name: String
    ) {
        var update = false
        val result = Database.runCatching {
            update = Database.changeAltName(rideId, name)
        }
        withContext(Ridecounters.mainThreadDispatcher) {
            if (result.isSuccess && update) {
                source.source().sendMessage("<green>Ride updated".mini)
            } else {
                source.source().sendMessage("<gray>No ride with this ID exists".mini)
            }
        }
    }
}