package be.mrtibo.ridecounters

import be.mrtibo.ridecounters.commands.*
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.events.JoinQuitEvent
import be.mrtibo.ridecounters.traincarts.SignActionRidecount
import com.bergerkiller.bukkit.tc.signactions.SignAction
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.LegacyPaperCommandManager


class Ridecounters : JavaPlugin() {

    private val signActionRidecount = SignActionRidecount()

    override fun onEnable() {

        INSTANCE = this

        /*
        Cloud
        */
        manager = LegacyPaperCommandManager.createNative(this, ExecutionCoordinator.simpleCoordinator())

        RideCommands
        CountCommands
        PlayerCommands
        DisplayCommands
        DatabaseCommands

        /*
        Configuration
         */
        saveDefaultConfig()

        /*
        Database
         */
        Database

        /*
        Register TrainCarts sign
         */
        SignAction.register(signActionRidecount)

        /*
        Events
         */
        JoinQuitEvent

        /*
        All done!
         */
        server.consoleSender.sendMessage("§e===================================")
        server.consoleSender.sendMessage("§e       Ridecounters Plugin")
        server.consoleSender.sendMessage("§a             Enabled")
        server.consoleSender.sendMessage("§e===================================")
    }

    override fun onDisable() {

        /*
        Unregister TrainCarts sign
         */
        SignAction.unregister(signActionRidecount)

        server.consoleSender.sendMessage("§e===================================")
        server.consoleSender.sendMessage("§e       Ridecounters Plugin")
        server.consoleSender.sendMessage("§c             Disabled")
        server.consoleSender.sendMessage("§e===================================")
    }

    companion object {

        lateinit var INSTANCE: Ridecounters
        lateinit var manager : LegacyPaperCommandManager<CommandSender>

    }

}