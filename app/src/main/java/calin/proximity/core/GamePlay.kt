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

    init {
        centerMapFlow()

        placeBombFlow()

//        Observable.combineLatest(device.locationStream, repository.bombs.bombsInAreaStream,
//                { location, bombs -> nearestBomb(location, bombs.filter { it.timestamp  }) }
//        )
//                .filter { it.distance > DEFUSING_RADIUS }
//                .subscribe {
//                    when {
//                        it.distance > DETONATION_RADIUS
//                        -> userInterface.alertBombDetonationArea(it.bomb!!)
//                        else -> userInterface.alertBombExploded(it.bomb!!)
//                    }
//                }
    }

    private fun placeBombFlow() {
        userInterface.placeBombButtonClicks
                .withLatestFrom(device.locationStream, { click, location -> location })
                .map {
                    ProximityBomb(it,
                            System.currentTimeMillis(/*this should be added on server*/),
                            repository.player)
                }
                .flatMap({ repository.bombs.addBomb(it) }, { bomb, result -> bomb })
                .subscribe { userInterface.map.addBomb(it) }
    }

    private fun centerMapFlow() {
        userInterface.centerButtonClicks
                .withLatestFrom(device.locationStream, { click, location -> location })
                .subscribe { userInterface.map.centerAt(it) }
    }
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

    //
//    private fun playerInBombAreaStream(from: Int, to: Int): Observable<ProximityBomb> =
//            distanceToTheNearestBombStream.filter { from > it.second || it.second >= to }.map { it.first }
//
//
    class Holder(var bomb: ProximityBomb?, var distance: Double)

    private fun nearestBomb(location: Location, bombs: List<ProximityBomb>) =
            bombs.fold(Holder(null, Double.MAX_VALUE), {
                result, bomb ->
                val distance = distanceCalculator.calculate(location, bomb.location)
                if (result.distance > distance) {
                    result.bomb = bomb
                    result.distance = distance
                }
                result
            })
}