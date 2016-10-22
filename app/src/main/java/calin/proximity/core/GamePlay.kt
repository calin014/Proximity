package calin.proximity.core

import rx.Observable

/**
 * Created by calin on 10/17/2016.
 */

interface ProximityRepository {
    fun place(bomb: ProximityBomb): Observable<String>
    fun placedBombsStream(): Observable<List<ProximityBomb>>
    fun bombDefusedStream(): Observable<String>
}

//TODO: MindYourStepsGamePlay
//TODO: BombHuntGamePlay
class GamePlay(
        var repository: ProximityRepository,

        //in streams
        var playerLocationStream: Observable<Location>,
        var playerPlacesBombStream: Observable<Unit>,
        var playerSelectsBombStream: Observable<String>,
        var playerTriesToDefuseBombStream: Observable<String>) {

    private val distanceToTheNearestBombStream: Observable<Pair<ProximityBomb, Double>?> =
            Observable.combineLatest(
                    playerLocationStream, repository.placedBombsStream(),
                    { location: Location, bombs: List<ProximityBomb> -> nearestBomb(location, bombs) }
            ).filter { it != null }

    //out streams
    fun playerWasKilledByBombStream(): Observable<ProximityBomb?> =
            distanceToTheNearestBombStream.map {
                val (bomb, distance) = it!!
                when {
                    distance < 5 -> bomb
                    else -> null
                }
            }.filter { it != null }

    fun playerInDefusingAreaStream(): Observable<ProximityBomb?> =
            distanceToTheNearestBombStream.map {
                val (bomb, distance) = it!!
                when {
                    5 <= distance && distance < 10 -> bomb
                    else -> null
                }
            }.filter { it != null }


    private fun nearestBomb(location: Location, bombs: List<ProximityBomb>): Pair<ProximityBomb, Double>? {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}