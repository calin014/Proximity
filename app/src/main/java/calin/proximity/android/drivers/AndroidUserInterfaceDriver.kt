package calin.proximity.android.drivers

import android.app.Activity
import android.view.View
import android.widget.Toast
import calin.proximity.core.abstractions.drivers.UserInterfaceDriver
import calin.proximity.core.abstractions.drivers.UserInterfaceSinks
import calin.proximity.core.abstractions.drivers.UserInterfaceSources
import com.jakewharton.rxbinding.view.clicks

/**
 * Created by calin on 10/28/2016.
 */
class AndroidUserInterfaceDriver(
        val activity: Activity,
        val centerButton: View,
        val placeBombButton: View,
        val defuseBombButton: View) : UserInterfaceDriver {

    override fun main(sinks: UserInterfaceSinks): UserInterfaceSources {
        sinks.sDefuseButtonVisibility.subscribe {
            defuseBombButton.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }

        sinks.sBombDefusingArea.subscribe {
            Toast.makeText(activity, "You are in bomb area!", Toast.LENGTH_LONG).show()
        }

        sinks.sBombExploded.subscribe {
            Toast.makeText(activity, "You died!!!!", Toast.LENGTH_LONG).show()
        }

        return UserInterfaceSources(
                centerButton.clicks(), placeBombButton.clicks(), defuseBombButton.clicks()
        )
    }
}