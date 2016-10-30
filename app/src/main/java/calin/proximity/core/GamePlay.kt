package calin.proximity.core

import calin.proximity.core.abstractions.Device
import calin.proximity.core.abstractions.DistanceCalculator
import calin.proximity.core.abstractions.Repository
import calin.proximity.core.abstractions.UserInterface
import rx.Observable
import java.util.concurrent.TimeUnit

//class Sources
//class Sinks
//fun main(sources: Sources): Sinks {
//    return Sinks()
//}

//TODO: MindYourStepsGamePlay, BombHuntGamePlay
class GamePlay(
        val device: Device,
        val distanceCalculator: DistanceCalculator,
        val repository: Repository,
        val userInterface: UserInterface
) {
    private val DETONATION_RADIUS = 5
    private val DEFUSING_RADIUS = 10
    private val TIME_TO_BECOME_ACTIVE = 1000 * 60

    fun setup() {
        centerMapFlow()
        playerNearBombFlow()

        placeBombFlows()
        defuseBombFlows()
    }

    private fun defuseBombFlows() {
        userInterface.defuseButtonVisibility = Observable.merge(
                userInterface.map.bombClicks.map { true },
                userInterface.map.outsideClicks.map { false }
        )

        repository.bombs.removeBomb = userInterface.defuseBombButtonClicks
                .withLatestFrom(userInterface.map.bombClicks, { click, bomb -> bomb })

        userInterface.map.removeBomb = repository.bombs.bombRemovedStream
    }

    private fun playerNearBombFlow() {
        val bombInArea = Observable.timer(1, TimeUnit.SECONDS)
                .withLatestFrom(device.locationStream, { tick, location -> location })
                .map { location ->
                    nearestBomb(location,
                            repository.bombs.getBombsInInterestArea()
                                    .filter { System.currentTimeMillis() - it.timestamp < TIME_TO_BECOME_ACTIVE }
                    )
                }
                .filter { it.bomb == null || it.distance > DEFUSING_RADIUS }

        userInterface.bombDefusingArea = bombInArea.filter { it.distance < DETONATION_RADIUS }.map { it.bomb }
        userInterface.bombExploded = bombInArea.filter { it.distance >= DETONATION_RADIUS }.map { it.bomb }
    }

    private fun placeBombFlows() {
        repository.bombs.addBomb = userInterface.placeBombButtonClicks
                .withLatestFrom(device.locationStream, { click, location -> location })
                .map {
                    ProximityBomb(it,
                            System.currentTimeMillis(/*this should be added on server*/),
                            repository.player)
                }

        userInterface.map.addBomb = repository.bombs.bombAddedStream

    }

    private fun centerMapFlow() {
        userInterface.map.center = userInterface.centerButtonClicks
                .withLatestFrom(device.locationStream, { click, location -> location })
    }

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