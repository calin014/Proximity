package calin.proximity.android.impl

import calin.proximity.core.Location
import calin.proximity.core.abstractions.Device
import pl.charmas.android.reactivelocation.ReactiveLocationProvider
import rx.Observable

/**
 * Created by calin on 10/28/2016.
 */
class AndroidDevice(val locationProvider: ReactiveLocationProvider) : Device {
    override val locationStream: Observable<Location>
        get() = locationProvider.lastKnownLocation
                .map { Location(it.latitude, it.longitude) }
}