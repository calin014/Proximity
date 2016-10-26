package calin.proximity.core.abstractions

import calin.proximity.core.Location
import calin.proximity.core.ProximityBomb
import rx.Observable

interface ProximityMap {
    fun addBomb(proximityBomb: ProximityBomb)
    fun removeBomb(proximityBomb: ProximityBomb)
    fun centerAt(location: Location)

    fun bombClicked(): Observable<ProximityBomb>
}