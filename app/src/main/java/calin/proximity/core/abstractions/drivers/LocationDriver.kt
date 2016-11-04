package calin.proximity.core.abstractions.drivers

import calin.proximity.Driver
import calin.proximity.core.Location
import rx.Observable

/**
 * Created by calin on 10/26/2016.
 */

data class LocationSources(val sLocation: Observable<Location>)
interface LocationDriver: Driver<Unit, LocationSources>