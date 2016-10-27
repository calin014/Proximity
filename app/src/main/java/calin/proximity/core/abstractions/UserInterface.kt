package calin.proximity.core.abstractions

import calin.proximity.core.Location
import calin.proximity.core.ProximityBomb
import rx.Observable

/**
 * Created by calin on 10/27/2016.
 */


interface UserInterface {
    interface Map {
        fun addBomb(proximityBomb: ProximityBomb)
        fun removeBomb(proximityBomb: ProximityBomb)
        fun centerAt(location: Location)

        fun bombClicked(): Observable<ProximityBomb>
    }

    fun centerButtonClicks(): Observable<Unit>
    fun placeBombButtonClicks(): Observable<Unit>
    fun defuseBombButtonClicks(): Observable<Unit>

    fun toggleDefuseButtonVisibility()
    fun getMap()
}