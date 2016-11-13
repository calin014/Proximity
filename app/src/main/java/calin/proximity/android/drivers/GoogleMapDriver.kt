package calin.proximity.android.drivers

import android.app.Activity
import calin.proximity.R
import calin.proximity.android.activity.ProximityActivity
import calin.proximity.core.ProximityBomb
import calin.proximity.core.abstractions.drivers.MapDriver
import calin.proximity.core.abstractions.drivers.MapSinks
import calin.proximity.core.abstractions.drivers.MapSources
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import rx.Emitter
import rx.Observable
import java.util.*


class GoogleMapDriver : ProximityActivity.ContainerPluggable, MapDriver {

    private val TAG = GoogleMapDriver::class.java.canonicalName

    private val mapFragment by lazy { MapFragment.newInstance() }

    private val markers = HashMap<String, Marker>()
    private val bombs = HashMap<Marker, ProximityBomb>()

    private var googleMapObservable = Observable.fromEmitter<GoogleMap>({ emitter ->
        mapFragment.getMapAsync {
            when (it) {
                null -> emitter.onError(NullPointerException())
                else -> {
                    emitter.onNext(it); emitter.onCompleted()
                }
            }
        }
    }, Emitter.BackpressureMode.BUFFER).replay()

    override fun main(sinks: MapSinks): MapSources {

        googleMapObservable.connect()
        googleMapObservable.subscribe { map -> setMapOptions(map) }

        val sBombClicks = googleMapObservable.flatMap { map ->
            Observable.fromEmitter<ProximityBomb>({ emitter ->
                map.setOnMarkerClickListener { marker ->
                    emitter.onNext(bombs[marker])
                    true
                }
            }, Emitter.BackpressureMode.BUFFER)
        }

        val sOutsideClicks = googleMapObservable.flatMap { map ->
            Observable.fromEmitter<Unit>({ emitter ->
                map.setOnMapClickListener {
                    emitter.onNext(Unit)
                }
            }, Emitter.BackpressureMode.BUFFER)
        }

        sinks.sBombRemoved.withLatestFrom(googleMapObservable) { bomb, map ->
            //se
            var m = markers.remove(bomb.id)
            if (m != null) {
                m.remove()
                bombs.remove(m)
            }
        }.subscribe()

        sinks.sBombAdded.withLatestFrom(googleMapObservable) { bomb, map ->
            //side effects
            val m: Marker = map.addMarker(MarkerOptions()
                    .position(LatLng(bomb.location.latitude, bomb.location.longitude))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)).alpha(0.8f))
            bombs[m] = bomb
            markers[bomb.id] = m
        }.subscribe()

        sinks.sCenter.withLatestFrom(googleMapObservable) { location, map ->
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 18f))
        }.subscribe()

        return MapSources(sBombClicks, sOutsideClicks)
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
}