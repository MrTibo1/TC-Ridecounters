package be.mrtibo.ridecounters.displays.texture

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.io.File
import java.lang.reflect.Type

object McMetaDeserializer : JsonDeserializer<Texture> {

    private val gson = Gson().newBuilder()
        .registerTypeAdapter(Texture::class.java, this)
        .registerTypeAdapter(NineSliceBorder::class.java, NineSliceBorderDeserializer)
        .create()

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Texture? {
        if (!json.isJsonObject) throw JsonParseException("Could not deserialize texture")
        val scaling = json.asJsonObject.getAsJsonObject("gui").getAsJsonObject("scaling")

        return context.deserialize(scaling, when (val type = scaling.get("type").asString) {
            "stretch" -> StretchTexture::class
            "tile" -> TileTexture::class
            "nine_slice" -> NineSliceTexture::class
            else -> throw JsonParseException("Unknown scaling type '$type'.")
        }.java)
    }

    fun deserialize(file: File): Texture {
        val reader = file.bufferedReader()
        return gson.fromJson(reader, Texture::class.java).also { reader.close() }
    }

}