package be.mrtibo.ridecounters.data.records

data class RidecountTopRecord(
    val ride: RideRecord,
    val data: Set<RidecountTotalRecord>
)
