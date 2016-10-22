package calin.proximity.core

/**
 * Created by calin on 10/17/2016.
 */
data class Location(
        //TODO: decide if we externalize this: eg.: make it an interface with distanceTo(Location)
        var latitude: Double = 0.0,
        var longitude: Double = 0.0
)

data class Player(
        var name: String,
        var location: Location? = null,
        var kills: Int? = null,
        var deaths: Int? = null,
        var rank: Int? = null
)

data class ProximityBomb(
        var location: Location,
        var timestamp: Long,
        var placer: Player
)

data class Rules(
        var detonationAreaRadius: Double = 10.0,
        var defusingAreaRadius: Double = 20.0
)

data class ProximityGame(
        var name: String,
        var rules: Rules,
        var creator: Player,
        var player: Player,
        var bombs: List<ProximityBomb> = mutableListOf()
)
