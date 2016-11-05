package calin.proximity.android.drivers

import calin.proximity.core.Location
import calin.proximity.core.abstractions.drivers.LocationDriver
import calin.proximity.core.abstractions.drivers.LocationSources
import com.google.android.gms.location.LocationRequest
import pl.charmas.android.reactivelocation.ReactiveLocationProvider

/**
 * Created by calin on 10/28/2016.
 */
class AndroidLocationDriver(val locationProvider: ReactiveLocationProvider) : LocationDriver {
    override fun main(sinks: Unit): LocationSources =
            LocationSources(sLocation =
            locationProvider.getUpdatedLocation(LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(1000)
                    .setFastestInterval(100)).map { Location(it.latitude, it.longitude) }
            )
}