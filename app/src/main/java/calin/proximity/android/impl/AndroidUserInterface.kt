package calin.proximity.android.impl

import calin.proximity.core.ProximityBomb
import calin.proximity.core.abstractions.UserInterface
import rx.Observable

/**
 * Created by calin on 10/28/2016.
 */
class AndroidUserInterface : UserInterface{
    override val map: UserInterface.Map
        get() = throw UnsupportedOperationException()
    override val centerButtonClicks: Observable<Unit>
        get() = throw UnsupportedOperationException()
    override val placeBombButtonClicks: Observable<Unit>
        get() = throw UnsupportedOperationException()
    override val defuseBombButtonClicks: Observable<Unit>
        get() = throw UnsupportedOperationException()

    override fun setDefuseButtonVisibility(b: Boolean) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun alertBombDetonationArea(first: ProximityBomb) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun alertBombExploded(first: ProximityBomb) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}