package calin.proximity.android.impl

import android.app.Activity
import calin.proximity.R
import calin.proximity.android.activity.ProximityActivity
import calin.proximity.core.Location
import calin.proximity.core.ProximityBomb
import calin.proximity.core.abstractions.UserInterface
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import rx.Observable
import java.util.*


object GoogleProximityMap : ProximityActivity.ContainerPluggable, UserInterface.Map {
    private val TAG = GoogleProximityMap::class.java.canonicalName

    private val mapFragment by lazy { MapFragment.newInstance() }

    private val bombs = HashMap<Marker, ProximityBomb>()
    private val markers = HashMap<ProximityBomb, Marker>()

    private var googleMapObservable = Observable.create<GoogleMap> { s ->
        mapFragment.getMapAsync {
            when (it) {
                null -> s.onError(NullPointerException())
                else -> {
                    s.onNext(it); s.onCompleted()
                }
            }
        }
    }.replay()

    init {
        googleMapObservable.connect()
        googleMapObservable.subscribe { map -> setMapOptions(map) }
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

    override val bombClicks: Observable<ProximityBomb>
        get() = googleMapObservable.flatMap { map ->
            Observable.create<ProximityBomb> { subscriber ->
                map.setOnMarkerClickListener { marker ->
                    if(!subscriber.isUnsubscribed) subscriber.onNext(bombs[marker])
                    true
                }
            }
        }

    override val outsideClicks: Observable<Unit>
        get() = googleMapObservable.flatMap { map ->
            Observable.create<Unit> { subscriber ->
                map.setOnMapClickListener {
                    if(!subscriber.isUnsubscribed) subscriber.onNext(Unit)
                }
            }
        }

    override var removeBomb: Observable<ProximityBomb>
        get() = throw UnsupportedOperationException()
        set(value) {
            value.withLatestFrom(googleMapObservable) { bomb, map ->
                //se
                var m = markers[bomb]
                if (m != null) {
                    m.remove()
                    markers.remove(bomb)
                    bombs.remove(m)
                }
            }
        }

    override var addBomb: Observable<ProximityBomb>
        get() = throw UnsupportedOperationException()
        set(value) {
            value.withLatestFrom(googleMapObservable) { bomb, map ->
                //side effects
                val m: Marker = map.addMarker(MarkerOptions()
                        .position(LatLng(bomb.location.latitude, bomb.location.longitude))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)).alpha(0.8f))
                bombs[m] = bomb
                markers[bomb] = m
            }
        }

    override var center: Observable<Location>
        get() = throw UnsupportedOperationException()
        set(value) {
            value.withLatestFrom(googleMapObservable) { location, map ->
                {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 18f))
                }
            }
        }
}