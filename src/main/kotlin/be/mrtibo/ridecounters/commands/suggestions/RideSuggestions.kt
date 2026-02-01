package be.mrtibo.ridecounters.commands.suggestions

import be.mrtibo.ridecounters.data.Database
import org.incendo.cloud.annotations.suggestion.Suggestions
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.paper.util.sender.Source

object RideSuggestions {
    @Suggestions("rideIds")
    suspend fun ridesSuggestions(ctx: CommandContext<Source>, input: CommandInput): List<String> {
        return Database.getRides(input.peekString()).map { it.id }
    }
}