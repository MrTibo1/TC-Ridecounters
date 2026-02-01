package be.mrtibo.ridecounters.traincarts

import be.mrtibo.ridecounters.Ridecounters
import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.utils.ComponentUtil.mini
import com.bergerkiller.bukkit.tc.controller.MinecartGroup
import com.bergerkiller.bukkit.tc.controller.MinecartMember
import com.bergerkiller.bukkit.tc.events.SignActionEvent
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent
import com.bergerkiller.bukkit.tc.signactions.SignAction
import com.bergerkiller.bukkit.tc.signactions.SignActionType
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player

class SignActionRidecount : SignAction() {

    override fun match(info: SignActionEvent): Boolean {
        return info.isType("ridecount", "rc", "ridecounter")
    }

    override fun execute(info: SignActionEvent) {
        val rideId = info.getLine(2) + info.getLine(3)
        if (rideId.isEmpty()) return

        if(info.isTrainSign && info.isAction(SignActionType.REDSTONE_ON, SignActionType.GROUP_ENTER) && info.isPowered && info.hasGroup()){
            handleGroup(info.group, rideId)
        } else if (info.isCartSign && info.isAction(SignActionType.REDSTONE_ON, SignActionType.MEMBER_ENTER) && info.isPowered && info.hasMember()) {
            handleMember(info.member, rideId)
        } else if (info.isRCSign && info.isAction(SignActionType.REDSTONE_ON) && info.isPowered) {
            info.rcTrainGroups.forEach { handleGroup(it, rideId) }
        }
    }

    private fun handleGroup(group: MinecartGroup, rideId: String) {
        group.forEach { handleMember(it, rideId) }
    }

    private fun handleMember(member: MinecartMember<*>, rideId: String) {
        member.entity.playerPassengers.forEach { incrementCount(it, rideId) }
    }

    private fun incrementCount(player: Player, rideId: String) {
        INSTANCE.launch {
            val ride = Database.getRide(rideId) ?: return@launch
            val newCount = Database.incrementCounter(player.uniqueId.toString(), rideId) ?: return@launch
            withContext(Ridecounters.mainThreadDispatcher) {
                val message = MiniMessage.miniMessage().deserialize(INSTANCE.config.getString("messages.ridecount_update")!!,
                    Placeholder.component("ridecount", Component.text(newCount.total)),
                    Placeholder.component("ride", ride.name.mini))
                player.sendMessage(message)
            }
        }
    }

    override fun build(info: SignChangeActionEvent): Boolean {
        val type = when {
            info.isTrainSign -> "train"
            info.isCartSign -> "cart"
            info.isRCSign -> "remote trains"
            else -> ""
        }

        return SignBuildOptions.create()
            .setName("$type ridecounter sign")
            .setPermission("ridecounters.traincarts")
            .setDescription("increment the ridecount of the players in the $type for the specified ride")
            .handle(info.player)
    }

    override fun canSupportRC(): Boolean {
        return true
    }

    override fun canSupportFakeSign(info: SignActionEvent): Boolean {
        return true
    }
}