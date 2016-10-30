package calin.proximity.android.impl

import android.app.Activity
import android.view.View
import android.widget.Toast
import calin.proximity.core.ProximityBomb
import calin.proximity.core.abstractions.UserInterface
import com.jakewharton.rxbinding.view.clicks
import rx.Observable

/**
 * Created by calin on 10/28/2016.
 */
class AndroidUserInterface(
        val activity: Activity,
        val centerButton: View,
        val placeBombButton: View,
        val defuseBombButton: View) : UserInterface {

    override var defuseButtonVisibility: Observable<Boolean>
        get() = throw UnsupportedOperationException()
        set(value) {
            value.subscribe {
                defuseBombButton.visibility = if(it) View.VISIBLE else View.INVISIBLE
            }
        }
    override var bombDefusingArea: Observable<ProximityBomb>
        get() = throw UnsupportedOperationException()
        set(value) {
            value.subscribe {
                Toast.makeText(activity, "You are in bomb area!", Toast.LENGTH_LONG).show()
            }
        }
    override var bombExploded: Observable<ProximityBomb>
        get() = throw UnsupportedOperationException()
        set(value) {
            value.subscribe {
                Toast.makeText(activity, "You died!!!!", Toast.LENGTH_LONG).show()
            }
        }

    override val map: UserInterface.Map = GoogleProximityMap

    override val centerButtonClicks: Observable<Unit>
        get() = centerButton.clicks()
    override val placeBombButtonClicks: Observable<Unit>
        get() = placeBombButton.clicks()
    override val defuseBombButtonClicks: Observable<Unit>
        get() = defuseBombButton.clicks()
}