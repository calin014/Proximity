package calin.proximity.android.activity

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.tbruyelle.rxpermissions.RxPermissions

class RequestPermisionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RxPermissions.getInstance(this)
                .request(ACCESS_FINE_LOCATION)
//                .bindToLifecycle(RxLifecycleAndroid.) //TODO: handle lifecycle
                .subscribe { granted ->
                    if (granted) {
                        startActivity(Intent(this, ProximityActivity::class.java))
                    } else {
                        Toast.makeText(this, "Sorry, you have to enable location...", Toast.LENGTH_LONG).show()
                    }
                }
    }
}
