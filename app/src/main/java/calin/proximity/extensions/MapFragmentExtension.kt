package calin.proximity.extensions

import android.support.v4.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.Marker
import rx.Observable
import rx.Single

/**
 * Created by calin on 10/24/2016.
 */

fun FragmentActivity.mapFragment(id:Int) = this.fragmentManager.findFragmentById(id) as MapFragment

fun MapFragment.ready(): Single<GoogleMap> =
        Single.create { subscriber ->
            this.getMapAsync { googleMap ->
                if (!subscriber.isUnsubscribed) subscriber.onSuccess(googleMap!!)
            }
        }

fun GoogleMap.markerClicks(): Observable<Marker> =
        Observable.create { subscriber ->
            this.setOnMarkerClickListener {
                if (!subscriber.isUnsubscribed) subscriber.onNext(it); true
            }
        }