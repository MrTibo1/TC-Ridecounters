package be.mrtibo.ridecounters.utils

import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE
import org.bukkit.Bukkit

object Scheduler {

    fun sync(function: () -> Unit) {
        Bukkit.getScheduler().runTask(INSTANCE, function)
    }

    fun async(function: () -> Unit) {
        Bukkit.getScheduler().runTaskAsynchronously(INSTANCE, function)
    }

}