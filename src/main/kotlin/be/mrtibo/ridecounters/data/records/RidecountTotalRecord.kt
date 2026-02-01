package be.mrtibo.ridecounters.data.records

data class RidecountTotalRecord(
    val player: PlayerRecord,
    val ride: RideRecord,
    val total: Int
)
