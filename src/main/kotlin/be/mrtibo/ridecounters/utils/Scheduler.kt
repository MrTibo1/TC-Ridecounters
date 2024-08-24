package be.mrtibo.ridecounters.utils

import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE
import org.bukkit.Bukkit

object Scheduler {

    fun sync(function: () -> Unit) {
        Bukkit.getScheduler().getMainThreadExecutor(INSTANCE).execute(function)
    }

    fun async(function: () -> Unit) {
        Bukkit.getScheduler().runTaskAsynchronously(INSTANCE, function)
    }

}