package calin.proximity

import com.firebase.geofire.GeoLocation

/**
 * Created by calin on 10/15/2016.
 */
data class Landmine(
        var userId: String? = null,
        var userName: String? = null,
        var timestamp: Long? = null,
        var location: GeoLocation? = null) {
}
