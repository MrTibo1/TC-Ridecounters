package be.mrtibo.ridecounters.displays

import be.mrtibo.ridecounters.Ridecounters
import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.data.records.RideRecord
import com.bergerkiller.bukkit.common.map.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import java.awt.Point
import javax.imageio.ImageIO

class RidecountMapDisplay : MapDisplay(), RidecountLeaderboard {

    private var ride: RideRecord? = null
    private var taskId : Int = 0
    private lateinit var counterLayer : Layer
    private var hCenter : Int = 64

    override fun onAttached() {
        setSessionMode(MapSessionMode.VIEWING)
        hCenter = width/2
        try {
            val file = INSTANCE.dataPath.resolve("images").resolve(properties.get("background", String::class.java)).toFile()
            val texture = MapTexture.fromImage(ImageIO.read(file))
            val image = MapTexture.resize(texture, width, height)
            bottomLayer.draw(image, 0, 0)
        } catch (_: Exception) {
            bottomLayer.fill(MapColorPalette.COLOR_WHITE)
            bottomLayer.drawContour(
                listOf(Point(0,0), Point(width-1, 0), Point(width-1, height-1), Point(0, height-1)),
                MapColorPalette.COLOR_ORANGE
            )
        }
        counterLayer = topLayer.next()

        INSTANCE.launch {
            val id = properties.get("rideId", String::class.java) ?: return@launch

            ride = Database.getRide(id)
            if (ride == null) return@launch
            withContext(Ridecounters.mainThreadDispatcher) {
                taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(INSTANCE, ::updateLeaderboard, 0, 120*20)
            }
        }


    }

    override fun onDetached() {
        Bukkit.getScheduler().cancelTask(taskId)
    }

    override fun getRide(): RideRecord? = ride

    override fun updateLeaderboard() {
        if (ride == null) return
        val limit = when {
            height == 128 -> 4
            else -> height/128*4+2
        }
        topLayer.alignment = MapFont.Alignment.MIDDLE
        topLayer.draw(MapFont.MINECRAFT, hCenter, 15, MapColorPalette.COLOR_BLACK, "Top $limit Riders")
        INSTANCE.launch {
            Database.runCatching {
                val top = getTopTotalRidecounter(ride!!.id, limit) ?: return@launch
                withContext(Ridecounters.mainThreadDispatcher) {
                    counterLayer.clear()
                    counterLayer.alignment = MapFont.Alignment.MIDDLE
                    counterLayer.draw(MapFont.MINECRAFT, width/2, 6, MapColorPalette.COLOR_BLACK, ride?.alternativeName ?: ride?.name)

                    if (top.data.isEmpty()) {
                        counterLayer.alignment = MapFont.Alignment.MIDDLE
                        counterLayer.draw(MapFont.MINECRAFT, hCenter, height/2, MapColorPalette.COLOR_BLACK, "None")
                        return@withContext
                    }

                    val spacing = 23
                    for ((i, count) in top.data.withIndex()) {
                        counterLayer.alignment = MapFont.Alignment.MIDDLE
                        counterLayer.draw(MapFont.MINECRAFT, hCenter, 28 + i*spacing, MapColorPalette.COLOR_BLACK, "#${i+1} ${count.player.username} ")
                        counterLayer.draw(MapFont.MINECRAFT, hCenter, 37 + i*spacing, MapColorPalette.COLOR_BLACK, count.total.toString())
                        if (i > 0) counterLayer.drawLine(hCenter-25, 24+i*spacing, hCenter+25, 24+i*spacing, MapColorPalette.getColor(150,150,150))
                    }
                    update()
                }
            }
        }
    }
}