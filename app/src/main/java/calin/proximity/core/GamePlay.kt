package calin.proximity.core

/**
 * Created by calin on 10/17/2016.
 */

interface GamePlay {
    interface GameObserver {
        fun playerIsInDefusingArea(bomb: ProximityBomb)

        fun playerIsInDetonationArea(bomb: ProximityBomb)

        fun playerHasDied(bomb: ProximityBomb)
    }

    fun createGame(player: Player, rules: Rules, observer: GameObserver)

    fun movePlayer(newLocation: Location)

    fun placeBomb(bomb: ProximityBomb)

    fun defuseBomb(bomb: ProximityBomb)
}
