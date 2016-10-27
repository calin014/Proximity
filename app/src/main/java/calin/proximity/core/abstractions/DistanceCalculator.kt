package calin.proximity.core.abstractions

import calin.proximity.core.Location

/**
 * Created by calin on 10/26/2016.
 */
interface DistanceCalculator {
    fun calculate(from: Location, to: Location): Double
}