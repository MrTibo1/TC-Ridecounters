package be.mrtibo.ridecounters.data.records

import java.util.UUID

data class PlayerRecord(
    val uuid: UUID,
    val username: String,
    val hidden: Boolean
)
