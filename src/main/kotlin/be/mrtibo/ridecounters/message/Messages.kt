package be.mrtibo.ridecounters.message

import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE
import be.mrtibo.ridecounters.utils.ComponentUtil.mini
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

object Messages {
    fun announceRidecountUpdate(rideName: String, newCount: Int): Component {
        return MiniMessage.miniMessage().deserialize(INSTANCE.config.getString("messages.ridecount_update")!!,
            Placeholder.component("ridecount", Component.text(newCount)),
            Placeholder.component("ride", rideName.mini))
    }
}