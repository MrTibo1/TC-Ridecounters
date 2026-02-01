package be.mrtibo.ridecounters.commands

import be.mrtibo.ridecounters.Ridecounters
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.displays.RidecountMapDisplay
import be.mrtibo.ridecounters.utils.ComponentUtil.mini
import com.bergerkiller.bukkit.common.map.MapDisplayProperties
import kotlinx.coroutines.withContext
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.paper.util.sender.PlayerSource

@Command("ridecounter|rc")
object DisplayCommands {

    @Command("display <rideId> [backgroundImage]")
    @Permission("ridecounters.display")
    suspend fun getRideDisplay(
        source: PlayerSource,
        @Argument(value = "rideId", suggestions = "rideIds")
        rideId: String,
        @Argument(value = "backgroundImage")
        image: String?
    ) {
        val player = source.source()
        val ride = Database.getRide(rideId)
        withContext(Ridecounters.mainThreadDispatcher) {
            if (ride == null) {
                player.sendMessage("""<gray>Ride with ID "$rideId" doesn't exist""".mini)
                return@withContext
            }
            val props = MapDisplayProperties.createNew(RidecountMapDisplay::class.java)
            props.set("rideId", rideId)
            if(image != null) props.set("background", image)
            val item = props.mapItem
            player.inventory.addItem(item)
            player.sendMessage("<green>Gave you a ridecount display for ride ${ride.name}</green>".mini)
        }
    }
}