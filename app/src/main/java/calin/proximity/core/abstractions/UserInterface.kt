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
        val outsideClicks: Observable<Unit>

        var removeBomb: Observable<ProximityBomb>
        var addBomb: Observable<ProximityBomb>
        var center: Observable<Location>
    }
    val map: Map

    val centerButtonClicks: Observable<Unit>
    val placeBombButtonClicks: Observable<Unit>
    val defuseBombButtonClicks: Observable<Unit>

    var defuseButtonVisibility: Observable<Boolean>
    var bombDefusingArea: Observable<ProximityBomb>
    var bombExploded: Observable<ProximityBomb>
}