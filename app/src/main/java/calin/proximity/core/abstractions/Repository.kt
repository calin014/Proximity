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
        val bombAddedStream: Observable<ProximityBomb>
        val bombRemovedStream: Observable<ProximityBomb>

        var addBomb: Observable<ProximityBomb>
        var removeBomb: Observable<ProximityBomb>

        //TODO: nono
        fun setInterestArea(center: Location, radius: Double)
        fun getBombsInInterestArea(): List<ProximityBomb>
    }

    val bombs: Bombs

    val player: Player
    fun updatePlayerStats(player: Player)
}