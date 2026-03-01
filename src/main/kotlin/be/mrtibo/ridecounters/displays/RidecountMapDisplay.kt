package be.mrtibo.ridecounters.displays

import be.mrtibo.ridecounters.Ridecounters
import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.data.records.RideRecord
import be.mrtibo.ridecounters.displays.texture.McMetaDeserializer
import be.mrtibo.ridecounters.displays.texture.StretchTexture
import com.bergerkiller.bukkit.common.map.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import javax.imageio.ImageIO

private const val ENTRY_SPACING = 23
private const val ENTRIES_PADDING = 34

class RidecountMapDisplay : MapDisplay(), RidecountLeaderboard {

    private var ride: RideRecord? = null
    private var taskId : Int = 0
    private lateinit var counterLayer : Layer
    private var hCenter : Int = 64

    override fun onAttached() {
        setSessionMode(MapSessionMode.VIEWING)
        hCenter = width/2
        try {
            val backgroundFile = properties.get("background", String::class.java)
            val imageFile = INSTANCE.dataPath.resolve("images").resolve(backgroundFile).toFile()
            val metaFile = INSTANCE.dataPath.resolve("images").resolve("$backgroundFile.mcmeta").toFile()

            val image = MapTexture.fromImage(ImageIO.read(imageFile))
            val texture = if (metaFile.exists()) McMetaDeserializer.deserialize(metaFile) else StretchTexture()
            texture.applyMap(image, bottomLayer)
        } catch (_: Exception) {
            bottomLayer.fill(MapColorPalette.COLOR_ORANGE)
            bottomLayer.fillRectangle(3, 3, width-6, height-6, MapColorPalette.COLOR_WHITE)
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

        val limit = (height - ENTRIES_PADDING) / ENTRY_SPACING

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

                    for ((i, count) in top.data.withIndex()) {
                        counterLayer.alignment = MapFont.Alignment.MIDDLE
                        counterLayer.draw(MapFont.MINECRAFT, hCenter, 28 + i*ENTRY_SPACING, MapColorPalette.COLOR_BLACK, "#${i+1} ${count.player.username} ")
                        counterLayer.draw(MapFont.MINECRAFT, hCenter, 37 + i*ENTRY_SPACING, MapColorPalette.COLOR_BLACK, count.total.toString())
                        if (i > 0) counterLayer.drawLine(hCenter-25, 24+i*ENTRY_SPACING, hCenter+25, 24+i*ENTRY_SPACING, MapColorPalette.getColor(150,150,150))
                    }
                    update()
                }
            }
        }
    }

}