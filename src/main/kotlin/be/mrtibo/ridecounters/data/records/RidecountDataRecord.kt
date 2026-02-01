package be.mrtibo.ridecounters.data.records

import be.mrtibo.ridecounters.Ridecounters
import java.time.LocalDateTime

data class RidecountDataRecord(
    val player: PlayerRecord,
    val ride: Ridecounters,
    val timestamp: LocalDateTime
)