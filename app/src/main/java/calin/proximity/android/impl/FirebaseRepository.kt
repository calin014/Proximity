package calin.proximity.android.impl

import calin.proximity.core.Location
import calin.proximity.core.Player
import calin.proximity.core.ProximityBomb
import calin.proximity.core.abstractions.Repository
import rx.Observable

/**
 * Created by calin on 10/28/2016.
 */
class FirebaseRepository : Repository {
    class FirebaseBombs : Repository.Bombs {
        override fun setInterestArea(center: Location, radius: Double) {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override val bombAddedStream: Observable<ProximityBomb>
            get() = throw UnsupportedOperationException()
        override val bombRemovedStream: Observable<ProximityBomb>
            get() = throw UnsupportedOperationException()

        override fun getBombsInInterestArea(): List<ProximityBomb> {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addBomb(proximityBomb: ProximityBomb): Observable<Unit> {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun removeBomb(proximityBomb: ProximityBomb): Observable<Unit> {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
    override val bombs: Repository.Bombs
        get() = FirebaseBombs()
    override val player: Player
        get() = throw UnsupportedOperationException()

    override fun updatePlayerStats(player: Player) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}