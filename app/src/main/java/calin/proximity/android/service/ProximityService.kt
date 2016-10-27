//package calin.proximity
//
//import android.app.Notification
//import android.app.NotificationManager
//import android.app.Service
//import android.content.Context
//import android.content.Intent
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.location.Location
//import android.os.Bundle
//import android.os.IBinder
//import android.os.Vibrator
//import android.support.v7.app.NotificationCompat
//import android.util.Log
//import android.widget.Toast
//import calin.proximity.android.activity.ProximityActivity.Companion.CACHE_RADIUS
//import calin.proximity.android.activity.ProximityActivity.Companion.GEOFIRE_NODE
//import calin.proximity.android.activity.ProximityActivity.Companion.LANDMINES_NODE
//import com.firebase.geofire.GeoFire
//import com.firebase.geofire.GeoLocation
//import com.firebase.geofire.GeoQuery
//import com.firebase.geofire.GeoQueryEventListener
//import com.google.android.gms.common.ConnectionResult
//import com.google.android.gms.common.api.GoogleApiClient
//import com.google.android.gms.location.LocationListener
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationServices
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.database.*
//import java.util.*
//
//
//class ProximityService : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
//    /**
//     * Provides the entry point to Google Play services.
//     */
//    protected var mGoogleApiClient: GoogleApiClient? = null
//
//    /**
//     * Stores parameters for requests to the FusedLocationProviderApi.
//     */
//    protected var mLocationRequest: LocationRequest? = null
//
//    /**
//     * Represents a geographical location.
//     */
//    protected var mCurrentLocation: Location? = null
//
//    private var mFirebaseAuth: FirebaseAuth? = null
//    private var mFirebaseUser: FirebaseUser? = null
//
//    private var mLandminesRef: DatabaseReference? = null
//    private var mGeoFire: GeoFire? = null
//    private var mGeoQuery: GeoQuery? = null
//
//    private val landminePositions = HashMap<String, Landmine>()
//
//    private var mNotificationManager: NotificationManager? = null
//
//    override fun onCreate() {
//        super.onCreate()
//
//        prepareStickyService()
//
//        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        buildGoogleApiClient()
//        mGoogleApiClient?.connect()
//
//        mFirebaseAuth = FirebaseAuth.getInstance()
//        mFirebaseUser = mFirebaseAuth!!.currentUser
//        mLandminesRef = FirebaseDatabase.getInstance().getReference(LANDMINES_NODE)
//        val ref = FirebaseDatabase.getInstance().getReference(GEOFIRE_NODE)
//        mGeoFire = GeoFire(ref)
//    }
//
//    private fun prepareStickyService() {
//        val notification = createNotification("Bombs bombz mommzzz")
//        startForeground(NOTIFICATION_ID, notification)
//    }
//
//    private fun createNotification(content: String): Notification {
//        val icon = BitmapFactory.decodeResource(resources,
//                R.mipmap.ic_launcher)
//
//        return NotificationCompat.Builder(this).setContentTitle("Proximity").setTicker("Proximity").setContentText(content).setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(
//                Bitmap.createScaledBitmap(icon, 128, 128, false)).setOngoing(true).build()
//    }
//
//    private fun prepareGeoQuery(loc: Location) {
//        val myPosition = GeoLocation(loc.latitude, loc.longitude)
//
//        if (mGeoQuery == null) {
//            mGeoQuery = mGeoFire!!.queryAtLocation(myPosition, CACHE_RADIUS.toDouble())
//            mGeoQuery!!.addGeoQueryEventListener(object : GeoQueryEventListener {
//                override fun onKeyEntered(key: String, geoLocation: GeoLocation) {
//                    mLandminesRef!!.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
//                        override fun onDataChange(dataSnapshot: DataSnapshot) {
//                            val landmine = dataSnapshot.getValue(Landmine::class.java)
//                            landmine.location = geoLocation
//
//                            landminePositions.put(key, landmine)
//                            if (mCurrentLocation != null) {
//                                if (playerExplodes(key, landmine)) {
//                                    boom(key)
//                                }
//                            }
//                        }
//
//                        override fun onCancelled(databaseError: DatabaseError) {
//
//                        }
//                    })
//
//                }
//
//                override fun onKeyExited(key: String) {
//                    landminePositions.remove(key)
//                }
//
//                override fun onKeyMoved(s: String, geoLocation: GeoLocation) {
//
//                }
//
//                override fun onGeoQueryReady() {
//
//                }
//
//                override fun onGeoQueryError(databaseError: DatabaseError) {
//
//                }
//            })
//        } else {
//            mGeoQuery!!.center = myPosition
//        }
//    }
//
//    private fun playerExplodes(key: String, landmine: Landmine): Boolean {
//        //        boolean isCurrentUser = mFirebaseUser.getUid() != null && mFirebaseUser.getUid().equals(landmine.userId);
//        val fuseTimeHasPassed = (System.currentTimeMillis() - landmine.timestamp!!).toFloat() / 1000f > 30f
//        return fuseTimeHasPassed && distanceToCurrentLocation(landmine.location!!) < BOOM_DISTANCE
//    }
//
//    private fun boom(key: String) {
//        Toast.makeText(this, "You died!!!!", Toast.LENGTH_SHORT).show()
//        mNotificationManager!!.notify(NOTIFICATION_ID, createNotification("So long sucker"))
//        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        // Vibrate for 500 milliseconds
//        v.vibrate(1000)
//        mGeoFire!!.removeLocation(key)
//    }
//
//    private fun distanceToCurrentLocation(geoLocation: GeoLocation): Float {
//        val r = FloatArray(2)
//        Location.distanceBetween(mCurrentLocation!!.latitude, mCurrentLocation!!.longitude, geoLocation.latitude, geoLocation.longitude, r)
//        return r[0]
//    }
//
//
//    override fun onConnected(bundle: Bundle?) {
//        LocationServices.FusedLocationApi.requestLocationUpdates(
//                mGoogleApiClient, mLocationRequest, this)
//    }
//
//    override fun onDestroy() {
//        if (mGoogleApiClient!!.isConnected) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
//            mGoogleApiClient!!.disconnect()
//        }
//        super.onDestroy()
//    }
//
//    override fun onConnectionSuspended(i: Int) {
//        mGoogleApiClient!!.connect()
//    }
//
//    override fun onConnectionFailed(connectionResult: ConnectionResult) {
//        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.errorCode)
//    }
//
//    override fun onLocationChanged(location: Location) {
//        mCurrentLocation = location
//        if (mGeoQuery == null) { //TODO: or we are out of cache range
//            prepareGeoQuery(location)
//        }
//
//        for ((key, value) in landminePositions) {
//            if (playerExplodes(key, value)) {
//                boom(key)
//                break
//            }
//        }
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        return Service.START_STICKY
//    }
//
//    /**
//     * Builds a GoogleApiClient. Uses the `#addApi` method to request the
//     * LocationServices API.
//     */
//    @Synchronized protected fun buildGoogleApiClient() {
//        mGoogleApiClient = GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build()
//        createLocationRequest()
//    }
//
//    /**
//     * Sets up the location request. Android has two location request settings:
//     * `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION`. These settings control
//     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
//     * the AndroidManifest.xml.
//     *
//     *
//     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
//     * interval (5 seconds), the Fused Location Provider API returns location updates that are
//     * accurate to within a few feet.
//     *
//     *
//     * These settings are appropriate for mapping applications that show real-time location
//     * updates.
//     */
//    protected fun createLocationRequest() {
//        mLocationRequest = LocationRequest()
//        mLocationRequest!!.apply {
//            interval = UPDATE_INTERVAL_IN_MILLISECONDS
//            fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
//            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        }
//    }
//
//    override fun onBind(intent: Intent): IBinder? {
//        return null
//    }
//
//    companion object {
//        private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000
//        private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000
//        val TAG = "PROXIMITY"
//        val BOOM_DISTANCE = 4
//        val NOTIFICATION_ID = 101
//    }
//}
