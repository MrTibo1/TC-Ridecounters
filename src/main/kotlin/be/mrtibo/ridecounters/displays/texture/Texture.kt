package be.mrtibo.ridecounters.displays.texture

import com.bergerkiller.bukkit.common.map.MapCanvas
import com.bergerkiller.bukkit.common.map.MapTexture

interface Texture {

    fun applyMap(texture: MapTexture, canvas: MapCanvas)

}