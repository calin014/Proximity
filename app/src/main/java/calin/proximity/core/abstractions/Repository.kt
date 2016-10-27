package calin.proximity.core.abstractions

import calin.proximity.core.Location
import calin.proximity.core.Player
import calin.proximity.core.ProximityBomb
import rx.Observable

/**
 * Created by calin on 10/27/2016.
 */
interface Repository {
    interface Bombs {
        fun setInterestArea(center: Location, radius: Double)
        fun addBomb(proximityBomb: ProximityBomb)
        fun bombAddedStream(): Observable<ProximityBomb>
        fun getBombs(): List<ProximityBomb> //TODO: decide if observable
    }

    fun getBombs(): Bombs

    fun getPlayer(): Player
    fun updateStats(player: Player)
}