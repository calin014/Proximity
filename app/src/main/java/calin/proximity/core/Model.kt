package calin.proximity.core

/**
 * Created by calin on 10/17/2016.
 */
data class Location(
        var latitude: Double = 0.0,
        var longitude: Double = 0.0
)

data class Player(
        var name: String? = null,
        var location: Location? = null,
        var kills: Int = 0,
        var deaths: Int = 0,
        var rank: Int = 0
)

data class ProximityBomb(
        var location: Location? = null,
        var timestamp: Long = 0,
        var placer: Player? = null
)

enum class Type {
    OPEN_WORLD, LOCAL_GAME //???
}

data class Rules(
        var type: Type = Type.OPEN_WORLD,
        var detonationAreaRadius: Double = 0.0,
        var defusingAreaRadius: Double = 0.0
)

data class ProximityGame(
        var name: String? = null,
        var rules: Rules? = null,
        var creator: Player? = null,
        var player: Player? = null,
        var bombs: List<ProximityBomb>? = null
)
