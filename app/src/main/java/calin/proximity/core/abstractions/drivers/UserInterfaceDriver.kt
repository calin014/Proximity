package calin.proximity.core.abstractions.drivers

import calin.proximity.Driver
import calin.proximity.core.ProximityBomb
import rx.Observable

/**
 * Created by calin on 10/27/2016.
 */

data class UserInterfaceSources (
        val sCenterButtonClicks: Observable<Unit>,
        val sPlaceBombButtonClicks: Observable<Unit>,
        val sDefuseBombButtonClicks: Observable<Unit>
)

data class UserInterfaceSinks (
        val sDefuseButtonVisibility: Observable<Boolean>,
        val sBombDefusingArea: Observable<ProximityBomb>,
        val sBombExploded: Observable<ProximityBomb>,
        val sUserMessage: Observable<Message>
)

data class Message(val text:String)

interface UserInterfaceDriver: Driver<UserInterfaceSinks, UserInterfaceSources>
