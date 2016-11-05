package calin.proximity.core

/**
 * Created by calin on 10/17/2016.
 */
data class Location(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0
)

data class Player(
        var name: String = "anonymous",
        var kills: Int? = null,
        var deaths: Int? = null,
        var rank: Int? = null
)

data class ProximityBomb(
        var id: String = "",
        var location: Location = Location(),
        var timestamp: Long = 0,
        var placer: Player = Player()
)