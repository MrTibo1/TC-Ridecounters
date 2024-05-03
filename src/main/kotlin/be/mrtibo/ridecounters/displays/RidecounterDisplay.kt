package be.mrtibo.ridecounters.displays

import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.utils.ComponentUtil.mini
import be.mrtibo.ridecounters.utils.ComponentUtil.string
import com.bergerkiller.bukkit.common.map.*
import org.bukkit.Bukkit
import java.awt.Point
import java.io.File
import javax.imageio.ImageIO

class RidecounterDisplay : MapDisplay() {

    private var rideId = 0
    private var taskId : Int = 0
    private lateinit var counterLayer : Layer
    private var hCenter : Int = 64

    override fun onAttached() {
        setSessionMode(MapSessionMode.VIEWING)
        hCenter = width/2
        rideId = properties.get("rideId", 0)
        try {
            val file = File(INSTANCE.dataFolder.toString() + "/" + properties.get("background", String::class.java))
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
        topLayer.drawLine(hCenter-49, 27, hCenter+49, 27, MapColorPalette.COLOR_BLACK)
        counterLayer = topLayer.next()

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(INSTANCE, {
            updateCounts()
        }, 0, 30*20)

    }

    private fun updateCounts() {
        val limit = when {
            height == 128 -> 4
            else -> height/128*4+2
        }
        topLayer.alignment = MapFont.Alignment.MIDDLE
        topLayer.draw(MapFont.MINECRAFT, hCenter, 18, MapColorPalette.COLOR_BLACK, "Top $limit Ridecounters")
        Database.getTopCountAsync(rideId, limit) {counts ->
            counterLayer.clear()
            if (counts.isNullOrEmpty()) {
                counterLayer.alignment = MapFont.Alignment.MIDDLE
                counterLayer.draw(MapFont.MINECRAFT, hCenter, height/2, MapColorPalette.COLOR_RED, "No Data")
                return@getTopCountAsync
            }

            counterLayer.alignment = MapFont.Alignment.MIDDLE
            counterLayer.draw(MapFont.MINECRAFT, width/2, 9, MapColorPalette.COLOR_BLACK, counts[0].ride.displayName ?: counts[0].ride.name.mini.string)

            val spacing = 22
            for ((i, count) in counts.withIndex()) {
                counterLayer.alignment = MapFont.Alignment.MIDDLE
                counterLayer.draw(MapFont.MINECRAFT, hCenter, 30 + i*spacing, MapColorPalette.COLOR_BLACK, "#${i+1} (${count.count}x)")
                counterLayer.draw(MapFont.MINECRAFT, hCenter, 38 + i*spacing, MapColorPalette.COLOR_BLACK, count.playerName)
                if (i > 0) counterLayer.drawLine(hCenter-25, 26+i*spacing, hCenter+25, 26+i*spacing, MapColorPalette.getColor(150,150,150))
            }
            update()
        }
    }

    override fun onDetached() {
        Bukkit.getScheduler().cancelTask(taskId)
    }

}