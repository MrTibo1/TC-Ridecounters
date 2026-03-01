package be.mrtibo.ridecounters.displays.texture

import com.bergerkiller.bukkit.common.map.MapCanvas
import com.bergerkiller.bukkit.common.map.MapTexture

data class TileTexture(
    val width: Int,
    val height: Int
) : Texture {
    override fun applyMap(texture: MapTexture, canvas: MapCanvas) {
        val img = MapTexture.resize(texture, width, height)
        for (x in 0 .. canvas.width/width) {
            for (y in 0 .. canvas.height/height) {
                canvas.draw(img, x*width, y*height)
            }
        }
    }
}