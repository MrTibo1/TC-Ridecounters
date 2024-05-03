package be.mrtibo.ridecounters.data

import java.util.*


data class Ride(
    val id: Int,
    val name: String,
    val displayName: String? = null,
    val owner : UUID? = null,
    val ownerName : String? = null
)