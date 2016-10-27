package calin.proximity.core

import calin.proximity.core.abstractions.Device
import calin.proximity.core.abstractions.DistanceCalculator
import calin.proximity.core.abstractions.Repository
import calin.proximity.core.abstractions.UserInterface

//TODO: MindYourStepsGamePlay, BombHuntGamePlay
class GamePlay(
        val device: Device,
        val distanceCalculator: DistanceCalculator,
        val repository: Repository,
        val userInterface: UserInterface
) {
    private val DETONATION_RADIUS = 5
    private val DEFUSING_RADIUS = 10

//    val outStreams = GameOutStreams(
//            //TODO: check if has enough bombs
//            playerHasPlacedBombStream = inStreams.usersPlaceBombActionStream.withLatestFrom(inStreams.deviceLocationStream,
//                    { drop: Unit, location: Location -> ProximityBomb(location, System.currentTimeMillis(), player) }
//            ),
//            playerInDefusingAreaStream = playerInBombAreaStream(DETONATION_RADIUS, DEFUSING_RADIUS),
//            playerWasKilledByBombStream = playerInBombAreaStream(0, DETONATION_RADIUS)
//    )
//
//    private val distanceToTheNearestBombStream: Observable<Pair<ProximityBomb?, Double>> =
//            Observable.combineLatest(inStreams.deviceLocationStream, inStreams.bombsInAreaStream,
//                    { location: Location, bombs: List<ProximityBomb> -> nearestBomb(location, bombs) }
//            )
//
//    private fun playerInBombAreaStream(from: Int, to: Int): Observable<ProximityBomb> =
//            distanceToTheNearestBombStream.filter { from > it.second || it.second >= to }.map { it.first }
//
//
//    private fun nearestBomb(location: Location, bombs: List<ProximityBomb>) =
//            bombs.fold(Pair(null as ProximityBomb?, Double.MAX_VALUE), {
//                result, bomb ->
//                val distance = calculator.distance(location, bomb.location)
//                if (result.second <= distance) result else Pair(bomb, distance)
//            })
}