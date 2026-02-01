package be.mrtibo.ridecounters.displays

import be.mrtibo.ridecounters.data.records.RideRecord

interface RidecountLeaderboard {

    fun getRide(): RideRecord?
    fun updateLeaderboard()

}