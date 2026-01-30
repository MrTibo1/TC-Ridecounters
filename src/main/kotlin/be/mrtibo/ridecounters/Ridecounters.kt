package be.mrtibo.ridecounters

import be.mrtibo.ridecounters.commands.*
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.traincarts.SignActionRidecount
import com.bergerkiller.bukkit.tc.signactions.SignAction
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.brigadier.BrigadierSetting
import org.incendo.cloud.brigadier.CloudBrigadierManager
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.setting.Configurable


class Ridecounters : JavaPlugin() {

    private val signActionRidecount = SignActionRidecount()

    override fun onEnable() {

        INSTANCE = this

        /*
        Cloud
        */
        commandManager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
            .executionCoordinator(ExecutionCoordinator.simpleCoordinator<Source>())
            .buildOnEnable(this)

        val brigadierManager: CloudBrigadierManager<in Source, out CommandSourceStack> = commandManager.brigadierManager()
        val settings: Configurable<BrigadierSetting?> = brigadierManager.settings()
        settings.set(BrigadierSetting.FORCE_EXECUTABLE, true)
        val annotationParser: AnnotationParser<Source> = AnnotationParser(commandManager, Source::class.java)

        annotationParser.parse(
            CountCommands,
            DisplayCommands,
            PlayerCommands,
            RideCommands
        )

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
    }

    override fun onDisable() {
        /*
        Unregister TrainCarts sign
         */
        SignAction.unregister(signActionRidecount)
    }

    companion object {

        lateinit var INSTANCE: Ridecounters
        lateinit var commandManager : PaperCommandManager<Source>
    }

}