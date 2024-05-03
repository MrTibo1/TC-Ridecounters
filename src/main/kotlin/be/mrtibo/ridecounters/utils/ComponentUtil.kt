package be.mrtibo.ridecounters.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

object ComponentUtil {

    val Component.string : String
        get() = PlainTextComponentSerializer.plainText().serialize(this)

    val String.mini : Component
        get() = MiniMessage.miniMessage().deserialize(this)

}