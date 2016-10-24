package calin.proximity.core

import rx.Observable

class GameInStreams(
        val deviceLocationStream: Observable<Location>,
        val usersPlaceBombActionStream: Observable<Unit>,
        val userSelectsBombStream: Observable<String>,
        val userDefusesActionStream: Observable<Unit>,

        val bombsInAreaStream: Observable<List<ProximityBomb>>,
        val bombHasBeenPlacedStream: Observable<ProximityBomb>,
        val bombHasBeenDefusedStream: Observable<ProximityBomb>
)

class GameOutStreams(
        val playerHasPlacedBombStream: Observable<ProximityBomb>,
        val playerWasKilledByBombStream: Observable<ProximityBomb>,
        val playerInDefusingAreaStream: Observable<ProximityBomb>
)

//TODO: MindYourStepsGamePlay, BombHuntGamePlay
class GamePlay(val player: Player, val inStreams: GameInStreams) {
    private val DETONATION_RADIUS = 5
    private val DEFUSING_RADIUS = 10

    val outStreams = GameOutStreams(
            //TODO: check if has enough bombs
            playerHasPlacedBombStream = inStreams.usersPlaceBombActionStream.withLatestFrom(inStreams.deviceLocationStream,
                    { drop: Unit, location: Location -> ProximityBomb(location, System.currentTimeMillis(), player) }
            ),
            playerInDefusingAreaStream = playerInBombAreaStream(DETONATION_RADIUS, DEFUSING_RADIUS),
            playerWasKilledByBombStream = playerInBombAreaStream(0, DETONATION_RADIUS)
    )

    private val distanceToTheNearestBombStream: Observable<Pair<ProximityBomb?, Double>> =
            Observable.combineLatest(inStreams.deviceLocationStream, inStreams.bombsInAreaStream,
                    { location: Location, bombs: List<ProximityBomb> -> nearestBomb(location, bombs) }
            )

    private fun playerInBombAreaStream(from: Int, to: Int): Observable<ProximityBomb> =
            distanceToTheNearestBombStream.filter { from > it.second || it.second >= to }.map { it.first }


    private fun nearestBomb(location: Location, bombs: List<ProximityBomb>) =
            bombs.fold(Pair(null as ProximityBomb?, Double.MAX_VALUE), {
                result, bomb ->
                val distance = location.distance(bomb.location)
                if (result.second <= distance) result else Pair(bomb, distance)
            })
}