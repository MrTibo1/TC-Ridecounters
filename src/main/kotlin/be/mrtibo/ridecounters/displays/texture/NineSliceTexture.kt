package be.mrtibo.ridecounters.displays.texture

import com.bergerkiller.bukkit.common.map.MapCanvas
import com.bergerkiller.bukkit.common.map.MapTexture
import com.google.common.primitives.Ints.min
import com.google.gson.annotations.SerializedName

data class NineSliceTexture(
    val width: Int,
    val height: Int,
    @SerializedName("stretch_inner")
    val shouldStretch: Boolean = false,
    val border: NineSliceBorder
) : Texture {

    override fun applyMap(texture: MapTexture, canvas: MapCanvas) {
        val texture = MapTexture.resize(texture, width, height)

        // Corners
        val topLeft = texture.getView(0, 0, border.left, border.top)
        val topRight = texture.getView(width-border.right, 0, border.right, border.top)
        val bottomLeft = texture.getView(0, height-border.bottom, border.left, border.bottom)
        val bottomRight = texture.getView(width-border.right, height-border.bottom, border.left, border.bottom)

        canvas.draw(topLeft, 0, 0)
        canvas.draw(topRight, canvas.width-border.right, 0)
        canvas.draw(bottomLeft, 0, canvas.height-border.bottom)
        canvas.draw(bottomRight, canvas.width-border.right, canvas.height-border.bottom)

        // Sides
        val top = texture.getView(border.left, 0, width-border.left-border.right, border.top)
        val bottom = texture.getView(border.left, height-border.bottom, width-border.left-border.right, border.bottom)
        val left = texture.getView(0, border.top, border.left, height-border.top-border.bottom)
        val right = texture.getView(width-border.right, border.top, border.right, height-border.top-border.bottom)

        if (shouldStretch) {
            canvas.draw(MapTexture.resize(top, canvas.width-border.left-border.right, border.top), border.left, 0)
            canvas.draw(MapTexture.resize(bottom, canvas.width-border.left-border.right, border.bottom), border.left, canvas.height-border.bottom)
            canvas.draw(MapTexture.resize(left, border.left, canvas.height-border.top-border.bottom), 0, border.top)
            canvas.draw(MapTexture.resize(right, border.right, canvas.height-border.top-border.bottom), canvas.width-border.right, border.top)
        } else {
            for (x in 0 .. (canvas.width-border.left-border.right)/(width-border.left-border.right)) {
                val xCoord = border.left+x*(width-border.left-border.right)
                val size = min(xCoord+width, canvas.width-border.right)-xCoord
                canvas.draw(top.getView(0, 0, size, top.height), xCoord, 0)
                canvas.draw(bottom.getView(0, 0, size, bottom.height), xCoord, canvas.height-border.bottom)
            }

            for (y in 0 .. (canvas.height-border.top-border.bottom)/(height-border.top-border.bottom)) {
                val yCoord = border.top+y*(height-border.top-border.bottom)
                val size = min(yCoord+height, canvas.height-border.bottom)-yCoord
                canvas.draw(left.getView(0, 0, left.width, size), 0, yCoord)
                canvas.draw(right.getView(0, 0, right.width, size), canvas.width-border.right, yCoord)
            }
        }

        // Inner
        val inner = texture.getView(border.left, border.top, width-border.left-border.right, height-border.top-border.bottom)

        if (shouldStretch) {
            canvas.draw(MapTexture.resize(inner, canvas.width-border.left-border.right, canvas.height-border.top-border.bottom), border.left, border.top)
        } else {
            for (x in 0 .. (canvas.width-border.left-border.right)/inner.width) {
                for (y in 0 .. (canvas.height-border.top-border.bottom)/inner.height) {
                    val xCoord = border.left+x*inner.width
                    val yCoord = border.top+y*inner.height
                    val w = min(xCoord+inner.width, canvas.width-border.right)-xCoord
                    val h = min(yCoord+inner.height, canvas.height-border.bottom)-yCoord
                    canvas.draw(inner.getView(0, 0, w, h), xCoord, yCoord)
                }
            }
        }
    }

}

data class NineSliceBorder(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)