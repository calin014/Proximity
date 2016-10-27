package calin.proximity.core

import calin.proximity.core.abstractions.Device
import calin.proximity.core.abstractions.DistanceCalculator
import calin.proximity.core.abstractions.Repository
import calin.proximity.core.abstractions.UserInterface
import rx.Observable
import java.util.concurrent.TimeUnit

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

    fun start() {
        //TODO: threads: eg: each abstraction provides a scheduler
        centerMapFlow()
        playerNearBombFlow()

        placeBombFlows()
        defuseBombFlows()
    }

    private fun defuseBombFlows() {
        userInterface.map.bombClicks
                .subscribe { userInterface.setDefuseButtonVisibility(if (it == null) false else true) }

        userInterface.defuseBombButtonClicks
                .withLatestFrom(userInterface.map.bombClicks, { click, bomb -> bomb })
                .subscribe { if (it != null) repository.bombs.removeBomb(it) }

        repository.bombs.bombRemovedStream.subscribe { userInterface.map.removeBomb(it) }
    }

    private fun playerNearBombFlow() {
        Observable.timer(1, TimeUnit.SECONDS)
                .withLatestFrom(device.locationStream, { tick, location -> location })
                .map { location ->
                    nearestBomb(location,
                            repository.bombs.getBombsInInterestArea()
                                    .filter { System.currentTimeMillis() - it.timestamp < TIME_TO_BECOME_ACTIVE }
                    )
                }
                .filter { it.bomb != null && it.distance > DEFUSING_RADIUS }
                .subscribe {
                    when {
                        it.distance > DETONATION_RADIUS -> userInterface.alertBombDetonationArea(it.bomb!!)
                        else -> userInterface.alertBombExploded(it.bomb!!)
                    }
                }
    }

    private fun placeBombFlows() {
        userInterface.placeBombButtonClicks
                .withLatestFrom(device.locationStream, { click, location -> location })
                .map {
                    ProximityBomb(it,
                            System.currentTimeMillis(/*this should be added on server*/),
                            repository.player)
                }
                .subscribe { repository.bombs.addBomb(it) }

        //bombs from repository
        repository.bombs.bombAddedStream.subscribe { userInterface.map.addBomb(it) }
    }

    private fun centerMapFlow() {
        userInterface.centerButtonClicks
                .withLatestFrom(device.locationStream, { click, location -> location })
                .subscribe { userInterface.map.centerAt(it) }
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