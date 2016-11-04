package calin.proximity.core.abstractions.drivers

import calin.proximity.Driver
import calin.proximity.core.Location
import calin.proximity.core.ProximityBomb
import rx.Observable

/**
 * Created by calin on 11/4/2016.
 */

data class MapSources(
        val sBombClicks: Observable<ProximityBomb>,
        val sOutsideClicks: Observable<Unit>
)

data class MapSinks(
        //TODO: bomb event???
        val sBombRemoved: Observable<ProximityBomb>,
        val sBombAdded: Observable<ProximityBomb>,
        val sCenter: Observable<Location>
)

interface MapDriver: Driver<MapSinks, MapSources>