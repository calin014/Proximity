package calin.proximity.android.drivers

import calin.proximity.core.Location
import calin.proximity.core.abstractions.drivers.LocationDriver
import calin.proximity.core.abstractions.drivers.LocationSources
import pl.charmas.android.reactivelocation.ReactiveLocationProvider

/**
 * Created by calin on 10/28/2016.
 */
class AndroidLocationDriver(val locationProvider: ReactiveLocationProvider) : LocationDriver {
    override fun main(sinks: Unit): LocationSources =
            LocationSources(sLocation =
                locationProvider.lastKnownLocation.map { Location(it.latitude, it.longitude) }
            )
}