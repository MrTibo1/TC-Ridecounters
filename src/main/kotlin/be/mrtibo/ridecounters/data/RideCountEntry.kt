package be.mrtibo.ridecounters.data

import java.util.UUID

data class RideCountEntry(val playerUUID: UUID, val playerName: String, val ride: Ride, val count: Int)
