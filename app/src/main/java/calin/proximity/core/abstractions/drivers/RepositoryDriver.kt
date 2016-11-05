package calin.proximity.core.abstractions.drivers

import calin.proximity.Driver
import calin.proximity.core.Location
import calin.proximity.core.Player
import calin.proximity.core.ProximityBomb
import rx.Observable

/**
 * Created by calin on 10/27/2016.
 */

enum class BombEventType {ADDED, REMOVED}
data class BombEvent(val type: BombEventType, val proximityBomb: ProximityBomb)
data class InterestArea(val center: Location, val radius: Double)

data class RepositorySources(
        val sPlayer: Observable<Player>,
        val sBombEvent: Observable<BombEvent>
)

data class RepositorySinks(
        val sBombAdded: Observable<ProximityBomb>,
        val sBombRemoved: Observable<ProximityBomb>,
        val sInterestArea: Observable<InterestArea>
)

interface RepositoryDriver: Driver<RepositorySinks, RepositorySources>

