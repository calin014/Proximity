package calin.proximity.core

import rx.Observable
import rx.Observer

/**
 * Created by calin on 10/17/2016.
 */

interface ProximityRepository {
    fun place(bomb: ProximityBomb): Observable<String>
    fun bombPlacedStream(): Observable<ProximityBomb>
    fun bombDefusedStream(): Observable<String>
}

//TODO: MindYourStepsGamePlay
//TODO: BombHuntGamePlay
class GamePlay(
        var game: ProximityGame,
        var playerLocation: Observable<Location>,
        var playerDropsBomb: Observable<Unit>,
        var bombPlacedByAnotherPlayer: Observable<ProximityBomb>,
        var defuseBomb: Observable<ProximityBomb>,

        var bombExploded: Observer<ProximityBomb>,
        var playerInDefusingArea: Observer<ProximityBomb>) {


    init {
//        playerLocation.first() {  }
    }


//    fun changePlayerLocationFlow(o: Observable<Location>): Observable<Location> =
//            o.doOnNext {
//                game.player.location = it
//                val (bomb, distance) = nearestBomb(it)
//
//                when {
//                    distance < game.rules.detonationAreaRadius -> bombExplodedSubject.onNext(bomb)
//                    distance < game.rules.defusingAreaRadius -> playerInDefusingAreaSubject.onNext(bomb)
//                }
//            }

    private fun nearestBomb(location: Location): Pair<ProximityBomb, Double> {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}