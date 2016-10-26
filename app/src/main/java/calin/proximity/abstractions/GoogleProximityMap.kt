package calin.proximity.abstractions

import android.app.Activity
import calin.proximity.core.Location
import calin.proximity.core.ProximityBomb
import calin.proximity.core.abstractions.ProximityMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.LatLng
import rx.Observable


//fun GoogleMap.markerClicks(): Observable<Marker> =
//        Observable.create { subscriber ->
//            this.setOnMarkerClickListener {
//                if (!subscriber.isUnsubscribed) subscriber.onNext(it); true
//            }
//        }

interface ProximityAndroidMap: ProximityMap {
    fun addToContainer(activity: Activity, containerId: Int): Unit
}

object GoogleProximityMap : ProximityAndroidMap {
    private val TAG = GoogleProximityMap::class.java.canonicalName

    private val mapFragment by lazy { MapFragment.newInstance() }

    private var googleMapObservable = Observable.create<GoogleMap> { s ->
        mapFragment.getMapAsync {
            when(it) {
                null -> s.onError(NullPointerException())
                else -> {s.onNext(it); s.onCompleted()}
            }
        }
    }.replay()

    init {
        googleMapObservable.connect()
        googleMapObservable.subscribe { setMapOptions(it) }
    }

    private fun setMapOptions(googleMap: GoogleMap) {
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        googleMap.setMinZoomPreference(18f)
        googleMap.setMaxZoomPreference(21f)
    }

    override fun addToContainer(activity: Activity, containerId: Int) {
        activity.fragmentManager
                .beginTransaction()
                .replace(containerId, mapFragment)
                .commit()
    }

    override fun addBomb(proximityBomb: ProximityBomb) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeBomb(proximityBomb: ProximityBomb) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun bombClicked(): Observable<ProximityBomb> {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun centerAt(location: Location) {
        googleMapObservable.subscribe {
            it.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 18f))
        }
    }
}