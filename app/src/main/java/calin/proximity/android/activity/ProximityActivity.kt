package calin.proximity.android.activity

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import calin.proximity.R
import calin.proximity.android.AndroidDistanceCalculator
import calin.proximity.android.auth.AuthRepository
import calin.proximity.android.drivers.AndroidLocationDriver
import calin.proximity.android.drivers.AndroidUserInterfaceDriver
import calin.proximity.android.drivers.FirebaseRepositoryDriver
import calin.proximity.android.drivers.GoogleMapDriver
import calin.proximity.core.GameRunner
import calin.proximity.core.ProximityGame
import com.tbruyelle.rxpermissions.RxPermissions
import kotlinx.android.synthetic.main.activity_proximity.*
import pl.charmas.android.reactivelocation.ReactiveLocationProvider


class ProximityActivity : AppCompatActivity() {

    interface ContainerPluggable {
        fun addToContainer(activity: Activity, containerId: Int): Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proximity)

        if (!RxPermissions.getInstance(this).isGranted(ACCESS_FINE_LOCATION)) {
            startActivity(Intent(this, RequestPermisionActivity::class.java))
        } else if (AuthRepository.user == null) {
            startActivity(Intent(this, SignInActivity::class.java))
//        } else if {
//            TODO: not in game
//        }
        } else {
            val googleMapDriver: GoogleMapDriver = GoogleMapDriver()
            val locationProvider = ReactiveLocationProvider(this)

            googleMapDriver.addToContainer(this, R.id.mapContainer)

            GameRunner.run(
                    ProximityGame(AndroidDistanceCalculator),
                    AndroidLocationDriver(locationProvider),
                    AndroidUserInterfaceDriver(this, centerButton, dropButton, defuseButton),
                    googleMapDriver,
                    FirebaseRepositoryDriver()
            )
        }
    }
}