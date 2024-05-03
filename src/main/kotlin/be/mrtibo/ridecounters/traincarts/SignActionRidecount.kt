package be.mrtibo.ridecounters.traincarts

import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE
import be.mrtibo.ridecounters.cache.OwnedRides
import be.mrtibo.ridecounters.commands.RideCommands.NO_ACCESS_MESSAGE
import be.mrtibo.ridecounters.data.Database
import be.mrtibo.ridecounters.utils.ComponentUtil.mini
import com.bergerkiller.bukkit.tc.events.SignActionEvent
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent
import com.bergerkiller.bukkit.tc.signactions.SignAction
import com.bergerkiller.bukkit.tc.signactions.SignActionType
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.entity.Player

class SignActionRidecount : SignAction() {

    override fun match(info: SignActionEvent): Boolean {
        return info.isType("ridecount", "rc", "ridecounter")
    }

    override fun execute(info: SignActionEvent) {
        val rideId: Int
        try {
            rideId = info.getLine(2).toInt()
        } catch (e: NumberFormatException) {
            return
        }
        if(info.isTrainSign && info.isAction(SignActionType.REDSTONE_ON, SignActionType.GROUP_ENTER) && info.isPowered && info.hasGroup()){
            for (member in info.members) {
                for (player in member.entity.playerPassengers) {
                    incrementCount(player, rideId)
                }
            }
        } else if (info.isCartSign && info.isAction(SignActionType.REDSTONE_ON, SignActionType.MEMBER_ENTER) && info.isPowered && info.hasMember()) {
            if (info.hasGroup()) {
                for (member in info.members) {
                    for (player in member.entity.playerPassengers) {
                        incrementCount(player, rideId)
                    }
                }
            } else if (info.hasMember()) {
                for (player in info.member.entity.playerPassengers) {
                    incrementCount(player, rideId)
                }
            }
        } else if (info.isRCSign && info.isAction(SignActionType.REDSTONE_ON) && info.isPowered) {
            for (trainGroup in info.rcTrainGroups) {
                for (member in trainGroup) {
                    for (player in member.entity.playerPassengers) {
                        incrementCount(player, rideId)
                    }
                }
            }
        }
    }

    private fun incrementCount(player: Player, rideId: Int){
        Database.incrementRideCounter(player, rideId) {success ->
            if(success) {
                Database.getRideCountAsync(player, rideId) {
                    it ?: return@getRideCountAsync
                    val message = MiniMessage.miniMessage().deserialize(INSTANCE.config.getString("messages.ridecount_update")!!,
                        Placeholder.component("ridecount", Component.text(it.count)),
                        Placeholder.component("ride", it.ride.name.mini))
                    player.sendMessage(message)
                }
                return@incrementRideCounter
            }
        }
    }

    override fun build(info: SignChangeActionEvent): Boolean {
        val type = when {
            info.isTrainSign -> "train"
            info.isCartSign -> "cart"
            info.isRCSign -> "remote"
            else -> ""
        }
        val rideId: Int
        try {
            rideId = info.getLine(2).toInt()
        } catch (_: NumberFormatException) {
            info.player.sendMessage("<red>You need to use the Ride ID on this sign".mini)
            return false
        }

        if(!info.player.hasPermission("ridecounters.admin") && OwnedRides.map[info.player]?.contains(rideId) != true) {
//            info.block.type = Material.AIR
            info.player.sendMessage(NO_ACCESS_MESSAGE)
            return false
        }

        return SignBuildOptions.create()
            .setName("$type ridecounter sign")
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