package calin.proximity.core.abstractions

import rx.Observable

/**
 * Created by calin on 10/26/2016.
 */
interface ProximityUserInterface {
    fun centerButtonClicks(): Observable<Unit>
    fun placeBombButtonClicks(): Observable<Unit>
    fun defuseBombButtonClicks(): Observable<Unit>

    fun toggleDefuseButtonVisibility()
}