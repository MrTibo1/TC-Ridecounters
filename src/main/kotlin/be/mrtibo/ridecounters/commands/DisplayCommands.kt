package be.mrtibo.ridecounters.commands

import be.mrtibo.ridecounters.Ridecounters.Companion.manager
import be.mrtibo.ridecounters.displays.RidecounterDisplay
import be.mrtibo.ridecounters.utils.ComponentUtil.mini
import cloud.commandframework.arguments.standard.IntegerArgument
import cloud.commandframework.arguments.standard.StringArgument
import com.bergerkiller.bukkit.common.map.MapDisplayProperties
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object DisplayCommands {

    init {

        val builder = manager.commandBuilder("ridecounter", "rc").literal("display", "map")

        manager.command(
            builder
                .permission("ridecounters.display.get")
                .argument(IntegerArgument.of("ride id"))
                .argument(StringArgument.builder<CommandSender?>("background file name").asOptional().build())
                .handler {ctx ->
                    if(ctx.sender !is Player) return@handler
                    val props = MapDisplayProperties.createNew(RidecounterDisplay::class.java)
                    props.set("rideId", ctx.get("ride id"))
                    if(ctx.contains("background file name")) props.set("background", ctx.get("background file name"))
                    val item = props.mapItem
                    (ctx.sender as Player).inventory.addItem(item)
                    ctx.sender.sendMessage("<green>Gave you a ridecount display for ride ${ctx.get<String>("ride id")}</green>".mini)
                }
        )

    }

}