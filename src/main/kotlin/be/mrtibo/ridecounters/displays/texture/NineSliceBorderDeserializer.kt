package be.mrtibo.ridecounters.displays.texture

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

object NineSliceBorderDeserializer : JsonDeserializer<NineSliceBorder> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): NineSliceBorder? {
        return if (json.isJsonObject) {
            Gson().fromJson(json, NineSliceBorder::class.java)
        } else if (json.isJsonPrimitive) {
            val border = json.asJsonPrimitive.asInt
            NineSliceBorder(border, border, border, border)
        } else null
    }
}