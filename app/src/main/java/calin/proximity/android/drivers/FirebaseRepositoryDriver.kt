package calin.proximity.android.drivers

import android.util.Log
import calin.proximity.android.auth.AuthRepository
import calin.proximity.core.Location
import calin.proximity.core.Player
import calin.proximity.core.abstractions.drivers.BombEvent
import calin.proximity.core.abstractions.drivers.RepositoryDriver
import calin.proximity.core.abstractions.drivers.RepositorySinks
import calin.proximity.core.abstractions.drivers.RepositorySources
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import rx.Observable
import rx.Single

/**
 * Created by calin on 10/28/2016.
 */
class FirebaseRepositoryDriver : RepositoryDriver {

    val TAG = FirebaseRepositoryDriver::class.java.canonicalName
    val BOMB_NODE = "_BOMBS"
    val GEOFIRE_NODE = "_GEOFIRE"

    private val bombRef = FirebaseDatabase.getInstance().getReference(BOMB_NODE)
    private val geofire = GeoFire(FirebaseDatabase.getInstance().getReference(GEOFIRE_NODE))

    private val geoQuery by lazy { geofire.queryAtLocation(GeoLocation(0.0, 0.0), 0.0) }

    override fun main(sinks: RepositorySinks): RepositorySources {

        sinks.sBombAdded.subscribe {
            val ref = bombRef.push()
            ref.setValue(it).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val location = toGeo(it.location)

                    geofire.setLocation(ref.key, location) { s, databaseError ->
                        if (databaseError != null) {
                            ref.removeValue()
                            Log.e(TAG, "err saving location: ", databaseError.toException())
                        }
                    }
                } else {
                    Log.e(TAG, "err saving bomb: ", task.exception)
                }
            }
        }

        sinks.sInterestArea.subscribe {
            geoQuery.setLocation(toGeo(it.center), it.radius)
        }

        val sPlayer = Single.just(Player(AuthRepository.user?.details?.displayName ?: "Anonymous"))
        val sBombEvent = bombEventStream()

        return RepositorySources(sPlayer, sBombEvent)
    }

    private fun bombEventStream(): Observable<BombEvent> = Observable.create<BombEvent> { subscriber ->
        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String, geoLocation: GeoLocation) {
                bombRef.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                                val landmine = dataSnapshot.getValue(Landmine::class.java)
//                                landmine.location = geoLocation
//
//                                landminePositions.put(key, landmine)
//                                if (mCurrentLocation != null) {
//                                    if (playerExplodes(key, landmine)) {
//                                        boom(key)
//                                    }
//                                }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
            }

            override fun onKeyExited(key: String) {
            }

            override fun onKeyMoved(s: String, geoLocation: GeoLocation) {
            }

            override fun onGeoQueryReady() {
            }

            override fun onGeoQueryError(databaseError: DatabaseError) {
            }
        })
    }


    private fun toGeo(it: Location) = GeoLocation(it.latitude, it.longitude)
}