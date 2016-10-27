package calin.proximity.core.abstractions

import calin.proximity.core.Location
import rx.Observable

/**
 * Created by calin on 10/26/2016.
 */
interface Device {
    val locationStream: Observable<Location>
}