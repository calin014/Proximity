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
        val bombStream: Observable<ProximityBomb>

        fun setInterestArea(center: Location, radius: Double)
        fun getBombsInInterestArea(): List<ProximityBomb>
        fun addBomb(proximityBomb: ProximityBomb): Observable<Unit>
    }

    val bombs: Bombs

    val player: Player
    fun updatePlayerStats(player: Player)
}