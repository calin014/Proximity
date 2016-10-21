package calin.proximity

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.View
import calin.proximity.R.id.defuseButton
import calin.proximity.R.id.map
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class ProximityActivity : FragmentActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private var mMap: GoogleMap? = null
    private var mGoogleApiClient: GoogleApiClient? = null

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseUser: FirebaseUser? = null

    private var mLandminesRef: DatabaseReference? = null
    private var mGeoFire: GeoFire? = null
    private var mGeoQuery: GeoQuery? = null

    private var mLastSelected: Marker? = null
    private var mDefuseButton: FloatingActionButton? = null
    private val keysToMarkers = HashMap<String, Marker>()
    private val markersToKeys = HashMap<Marker, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!prepareAndCheckAuth()) {
            // Not signed in, launch the Sign In activity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_proximity)

        prepareMap()
        prepareGoogleApi()
        prepareFirebase()
        registerButtonHandlers()
        startService(Intent(this, ProximityService::class.java))
    }

    private fun prepareAndCheckAuth(): Boolean {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth!!.currentUser
        if (mFirebaseUser == null) {
            return false
        }
        return true
    }

    private fun prepareMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun registerButtonHandlers() {
        registerHandlerForCenterButton()
        registerHandlerForDropButton()
        registerHandlerForDefuseButton()
    }

    private fun registerHandlerForDefuseButton() {
        mDefuseButton = findViewById(defuseButton) as FloatingActionButton

        mDefuseButton!!.setOnClickListener {
            if (mLastSelected != null) {
                mGeoFire!!.removeLocation(markersToKeys[mLastSelected!!])
                mDefuseButton!!.visibility = View.INVISIBLE
            }
        }
    }

    private fun registerHandlerForDropButton() {
        val dropButton = findViewById(R.id.dropButton) as FloatingActionButton
        dropButton.setOnClickListener {
            val landmineRef = mLandminesRef!!.push()
            landmineRef.setValue(Landmine(mFirebaseUser!!.uid, mFirebaseUser!!.displayName, System.currentTimeMillis())).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
                    val landminePosition = GeoLocation(loc.latitude, loc.longitude)

                    mGeoFire!!.setLocation(landmineRef.key, landminePosition) { s, databaseError ->
                        if (databaseError != null) {
                            landmineRef.removeValue()
                            Log.e(TAG, "err saving location: " + databaseError.toString())
                        }
                    }
                }
            }
        }
    }

    private fun registerHandlerForCenterButton() {
        val centerButton = findViewById(R.id.centerButton) as FloatingActionButton
        centerButton.setOnClickListener { goToCurrentPosition() }
    }

    private fun prepareFirebase() {
        mLandminesRef = FirebaseDatabase.getInstance().getReference(LANDMINES_NODE)
        val ref = FirebaseDatabase.getInstance().getReference(GEOFIRE_NODE)
        mGeoFire = GeoFire(ref)
    }

    private fun syncGeoQuery(loc: Location) {
        val myPosition = GeoLocation(loc.latitude, loc.longitude)
        if (mGeoQuery == null) {
            mGeoQuery = mGeoFire!!.queryAtLocation(myPosition, CACHE_RADIUS.toDouble())
            mGeoQuery!!.addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onKeyEntered(key: String, geoLocation: GeoLocation) {
                    val marker = mMap!!.addMarker(MarkerOptions().position(LatLng(geoLocation.latitude, geoLocation.longitude)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)).alpha(0.8f))

                    keysToMarkers.put(key, marker)
                    markersToKeys.put(marker, key)
                }

                override fun onKeyExited(key: String) {
                    val marker = keysToMarkers.remove(key)
                    markersToKeys.remove(marker)
                    marker?.remove()
                    mLandminesRef!!.child(key).removeValue()
                }

                override fun onKeyMoved(s: String, geoLocation: GeoLocation) {

                }

                override fun onGeoQueryReady() {

                }

                override fun onGeoQueryError(databaseError: DatabaseError) {

                }
            })
        } else {
            mGeoQuery!!.center = myPosition
        }
    }

    private fun prepareGoogleApi() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add keysToMarkers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap!!.isMyLocationEnabled = true
        mMap!!.uiSettings.isMyLocationButtonEnabled = false
        mMap!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
        mMap!!.setMinZoomPreference(MIN_ZOOM_PREFERENCE)
        mMap!!.setMaxZoomPreference(MAX_ZOOM_PREFERENCE)

        mMap!!.setOnMarkerClickListener { marker ->
            if (mLastSelected != null) mLastSelected!!.alpha = 0.8f

            val selectedMarkerClicked = mLastSelected != null && marker.position == mLastSelected!!.position

            if (selectedMarkerClicked) {
                mLastSelected = null
                mDefuseButton!!.visibility = View.INVISIBLE
            } else {
                mLastSelected = marker
                marker.alpha = 1f
                mDefuseButton!!.visibility = View.VISIBLE
            }
            false
        }
        goToCurrentPosition()
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient!!.connect()
    }

    override fun onStop() {
        mGoogleApiClient!!.disconnect()
        super.onStop()
    }

    override fun onConnected(bundle: Bundle?) {
        goToCurrentPosition()
    }

    private fun goToCurrentPosition() {
        if (mMap != null && mGoogleApiClient!!.isConnected) {
            val loc = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient)
            if (loc != null) {
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), MIN_ZOOM_PREFERENCE.toFloat()))
                syncGeoQuery(loc)
            }
        }
    }

    override fun onConnectionSuspended(i: Int) {
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.errorCode)
    }

    companion object {
        val MIN_ZOOM_PREFERENCE = 18.0f
        val MAX_ZOOM_PREFERENCE = 21.0f
        val GEOFIRE_NODE = "geofire"
        val CACHE_RADIUS = 1.0f
        val LANDMINES_NODE = "LANDMINES"
        val TAG = "MINEZ_ACTIVITY"
    }
}
