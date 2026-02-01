package be.mrtibo.ridecounters

import be.mrtibo.ridecounters.commands.RidecountCommands
import be.mrtibo.ridecounters.commands.DisplayCommands
import be.mrtibo.ridecounters.commands.RideCommands
import be.mrtibo.ridecounters.commands.suggestions.RideSuggestions
import be.mrtibo.ridecounters.concurrency.AsyncDispatcher
import be.mrtibo.ridecounters.concurrency.MainThreadDispatcher
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.events.JoinEvent
import be.mrtibo.ridecounters.traincarts.SignActionRidecount
import be.mrtibo.ridecounters.update.UpdateChecker
import com.bergerkiller.bukkit.tc.signactions.SignAction
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.*
import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.brigadier.BrigadierSetting
import org.incendo.cloud.brigadier.CloudBrigadierManager
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.kotlin.coroutines.annotations.installCoroutineSupport
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.setting.Configurable
import kotlin.coroutines.CoroutineContext

class Ridecounters : JavaPlugin(), CoroutineScope {

    private val signActionRidecount = SignActionRidecount()


    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Default

    override fun onEnable() {
        INSTANCE = this

        mainThreadDispatcher = MainThreadDispatcher(this)
        asyncDispatcher = AsyncDispatcher(this)

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

        annotationParser.installCoroutineSupport(this)

        annotationParser.parse(
            RideSuggestions,
            DisplayCommands,
            RidecountCommands,
//            PlayerCommands,
            RideCommands
        )

        /*
        Configuration
         */
        saveDefaultConfig()

        /*
        Database
         */
        try {
            Database.setupConnection()
            Database.createTables()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        /*
        Register TrainCarts sign
         */
        SignAction.register(signActionRidecount)

        Bukkit.getPluginManager().registerEvents(JoinEvent(), this)

        val updateChecker = config.getBoolean("check_for_updates", true)
        val pluginId = 29184
        val metrics = Metrics(this, pluginId)
        metrics.addCustomChart(
            // simpele taart
            SimplePie("database") { Database.type.typeName }
        )
        metrics.addCustomChart(
            SimplePie("update_checker") { updateChecker.toString() }
        )

        if (updateChecker) {
            launch {
                withContext(asyncDispatcher) {
                    delay(5000)
                    UpdateChecker.checkForUpdate()
                }
            }
        }
    }

    override fun onDisable() {
        /*
        Unregister TrainCarts sign
         */
        SignAction.unregister(signActionRidecount)

        Database.shutdown()

        runCatching {
            cancel()
        }
    }

    companion object {

        lateinit var INSTANCE: Ridecounters
        lateinit var commandManager : PaperCommandManager<Source>
        lateinit var mainThreadDispatcher: CoroutineDispatcher
        lateinit var asyncDispatcher: CoroutineDispatcher
    }

}