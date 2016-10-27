package calin.proximity.android.impl

import calin.proximity.core.Location
import calin.proximity.core.abstractions.DistanceCalculator

/**
 * Created by calin on 10/27/2016.
 */
class AndroidDistanceCalculator : DistanceCalculator {
    override fun calculate(from: Location, to: Location): Double {
        val r = FloatArray(2)
        android.location.Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, r)
        return r[0].toDouble()
    }
}