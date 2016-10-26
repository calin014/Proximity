package calin.proximity.core

/**
 * Created by calin on 10/17/2016.
 */
data class Location(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0
)

data class Player(
        var name: String,
        var kills: Int? = null,
        var deaths: Int? = null,
        var rank: Int? = null
)

data class ProximityBomb(
        var location: Location,
        var timestamp: Long,
        var placer: Player
)

data class ProximityGame(
        var name: String,
        var creator: Player,
        var player: Player,
        var bombs: List<ProximityBomb> = mutableListOf()
)
