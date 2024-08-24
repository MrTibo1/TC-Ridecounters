package be.mrtibo.ridecounters.commands

import be.mrtibo.ridecounters.Ridecounters.Companion.manager
import be.mrtibo.ridecounters.displays.RidecounterDisplay
import be.mrtibo.ridecounters.utils.ComponentUtil.mini
import com.bergerkiller.bukkit.common.map.MapDisplayProperties
import org.bukkit.entity.Player
import org.incendo.cloud.parser.standard.IntegerParser
import org.incendo.cloud.parser.standard.StringParser

object DisplayCommands {

    init {

        val builder = manager.commandBuilder("ridecounter", "rc").literal("display", "map")

        manager.command(
            builder
                .permission("ridecounters.display.get")
                .required("ride id", IntegerParser.integerParser())
                .optional("background file name", StringParser.stringParser())
                .senderType(Player::class.java)
                .handler {ctx ->
                    val props = MapDisplayProperties.createNew(RidecounterDisplay::class.java)
                    props.set("rideId", ctx.get("ride id"))
                    if(ctx.contains("background file name")) props.set("background", ctx.get("background file name"))
                    val item = props.mapItem
                    (ctx.sender()).inventory.addItem(item)
                    ctx.sender().sendMessage("<green>Gave you a ridecount display for ride ${ctx.get<String>("ride id")}</green>".mini)
                }
        )

    }

}