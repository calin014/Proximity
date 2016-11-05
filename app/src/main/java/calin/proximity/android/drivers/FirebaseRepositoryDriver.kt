package calin.proximity.android.drivers

import android.util.Log
import calin.proximity.android.auth.AuthRepository
import calin.proximity.core.Location
import calin.proximity.core.Player
import calin.proximity.core.ProximityBomb
import calin.proximity.core.abstractions.drivers.BombEvent
import calin.proximity.core.abstractions.drivers.BombEventType.ADDED
import calin.proximity.core.abstractions.drivers.BombEventType.REMOVED
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
import rx.Emitter
import rx.Observable

/**
 * Created by calin on 10/28/2016.
 */
class FirebaseRepositoryDriver : RepositoryDriver {

    val TAG = FirebaseRepositoryDriver::class.java.canonicalName
    val BOMB_NODE = "_BOMBS"
    val GEOFIRE_NODE = "_GEOFIRE"

    private val bombRef = FirebaseDatabase.getInstance().getReference(BOMB_NODE)
    private val geofire = GeoFire(FirebaseDatabase.getInstance().getReference(GEOFIRE_NODE))

    private val geoQuery by lazy { geofire.queryAtLocation(GeoLocation(0.0, 0.0), 1.0) }

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

        sinks.sBombRemoved.subscribe {
            bombRef.child(it.id).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    geofire.removeLocation(it.id) {s, databaseError ->
                        if (databaseError != null) {
//                            ref.removeValue()
                            //TODO: this leaves inconsistencies
                            Log.e(TAG, "err removing location: ", databaseError.toException())
                        }
                    }
                } else {
                    Log.e(TAG, "err removing bomb: ", task.exception)
                }
            }
        }

        sinks.sInterestArea.subscribe {
            geoQuery.setLocation(toGeo(it.center), it.radius)
        }

        val sPlayer = Observable.just(Player(AuthRepository.user?.details?.displayName ?: "Anonymous"))
        val sBombEvent = bombEventStream()

        return RepositorySources(sPlayer, sBombEvent)
    }

    private fun loadBombByKey(key:String, callback: (ProximityBomb) -> Unit) {
        bombRef.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    val bomb = dataSnapshot.getValue(ProximityBomb::class.java)
                    bomb.id = key
                    callback(bomb)
                    bombRef.child(key).removeEventListener(this) //may not work
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "err getting bomb: ", databaseError.toException())
            }
        })
    }

    private fun bombEventStream(): Observable<BombEvent> = Observable.fromEmitter<BombEvent>({ emitter ->
        val listener = object : GeoQueryEventListener {
            override fun onKeyEntered(key: String, geoLocation: GeoLocation) {
                loadBombByKey(key, {emitter.onNext(BombEvent(ADDED, it))})
            }

            override fun onKeyExited(key: String) {
                emitter.onNext(BombEvent(REMOVED, ProximityBomb(id = key)))
//                loadBombByKey(key, {})
            }

            override fun onKeyMoved(s: String, geoLocation: GeoLocation) {}
            override fun onGeoQueryReady() {}
            override fun onGeoQueryError(databaseError: DatabaseError) {}
        }

        geoQuery.addGeoQueryEventListener(listener)
        emitter.setCancellation { geoQuery.removeGeoQueryEventListener(listener) }
    }, Emitter.BackpressureMode.BUFFER)


    private fun toGeo(it: Location) = GeoLocation(it.latitude, it.longitude)
}