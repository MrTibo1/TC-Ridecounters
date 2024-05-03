package be.mrtibo.ridecounters

import be.mrtibo.ridecounters.commands.*
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.events.JoinQuitEvent
import be.mrtibo.ridecounters.traincarts.SignActionRidecount
import cloud.commandframework.CommandTree
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.paper.PaperCommandManager
import com.bergerkiller.bukkit.tc.signactions.SignAction
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Function


class Ridecounters : JavaPlugin() {

    private val signActionRidecount = SignActionRidecount()

    override fun onEnable() {

        INSTANCE = this

        /*
        Cloud
        */
        val executionCoordinatorFunction : Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> =
            CommandExecutionCoordinator.simpleCoordinator();

        val mapperFunction : Function<CommandSender, CommandSender> = Function.identity()
        try {
            manager = PaperCommandManager( /* Owning plugin */
                this,  /* Coordinator function */
                executionCoordinatorFunction,  /* Command Sender -> C */
                mapperFunction,  /* C -> Command Sender */
                mapperFunction
            )
        } catch (e: Exception) {
            logger.severe("Failed to initialize the command this.manager")
            /* Disable the plugin */
            server.pluginManager.disablePlugin(this)
            return
        }

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
        lateinit var manager: PaperCommandManager<CommandSender>

    }

}