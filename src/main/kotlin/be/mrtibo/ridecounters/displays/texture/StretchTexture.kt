package be.mrtibo.ridecounters.displays.texture

import com.bergerkiller.bukkit.common.map.MapCanvas
import com.bergerkiller.bukkit.common.map.MapTexture

class StretchTexture : Texture {

    override fun applyMap(texture: MapTexture, canvas: MapCanvas) {
        canvas.draw(MapTexture.resize(texture, canvas.width, canvas.height), 0, 0)
    }

}