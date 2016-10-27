package calin.proximity.core.abstractions

import calin.proximity.core.Location
import calin.proximity.core.ProximityBomb
import rx.Observable

/**
 * Created by calin on 10/27/2016.
 */


interface UserInterface {
    interface Map {
        val bombClicks: Observable<ProximityBomb>

        fun addBomb(proximityBomb: ProximityBomb)
        fun removeBomb(proximityBomb: ProximityBomb)
        fun centerAt(location: Location)
    }
    val map: Map

    val centerButtonClicks: Observable<Unit>
    val placeBombButtonClicks: Observable<Unit>
    val defuseBombButtonClicks: Observable<Unit>

    fun toggleDefuseButtonVisibility()
    fun alertBombDetonationArea(first: ProximityBomb)
    fun alertBombExploded(first: ProximityBomb)
}