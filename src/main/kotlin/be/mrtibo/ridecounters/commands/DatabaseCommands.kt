package be.mrtibo.ridecounters.commands

import be.mrtibo.ridecounters.Ridecounters
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.utils.ComponentUtil.mini

object DatabaseCommands {

    init {

        val builder = Ridecounters.manager.commandBuilder("ridecounter", "rc").literal("database", "db")

        Ridecounters.manager.command(builder.literal("reconnect")
            .permission("ridecounters.database.reconnect")
            .handler { ctx ->
                ctx.sender.sendMessage("<yellow>Attempting to reconnect database</yellow>".mini)
                Ridecounters.INSTANCE.reloadConfig()
                Database.connectAsync {
                    val message = if (it) "<green>☑☑☑ Database connected ☑☑☑</green>" else "<red>Something didn't go quite right. Check the console for more info!</red>"
                    ctx.sender.sendMessage(message.mini)
                }
            }
        )
    }

}