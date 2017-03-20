package com.kuanyi.geophoto.map

import android.app.Fragment
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.kuanyi.geophoto.MainActivity
import com.kuanyi.geophoto.R
import com.kuanyi.geophoto.manager.DataManager
import com.kuanyi.geophoto.model.GsonPhoto
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_maps.*


/**
 * The Fragment that displays the photo's location on map
 * Created by kuanyi on 2017/3/15.
 */
class MapFragment : Fragment(), OnMapReadyCallback, DataManager.PhotoCallback {

    var mPhotoMarkerMap = HashMap<GsonPhoto, Marker>()

    val mTargetList = mutableListOf<Target>()

    companion object {
        val TUTORIAL_COMPLETE = "TUTORIAL_COMPLETE"
        lateinit var mMapView : GoogleMap
        val DEFAULT_LAT = 35.6775219
        val DEFAULT_LNG = 139.7524709
        val DEFAULT_RADIUS = 3
        val DEFAULT_ZOOM = 12.0f
        var currentLat : Double = 0.0
                //get the latitude that is currently displaying on the map
            get() = mMapView.cameraPosition.target.latitude
        var currentLng : Double = 0.0
                // get the longitude that is currently displaying on the map
            get() = mMapView.cameraPosition.target.longitude
        var radius : Int = 3 //default to 3 meters
                // calculate the radius of the current visible map region
                // http://stackoverflow.com/questions/29222864/get-radius-of-visible-map-in-android
            get() {
                val visibleRegion = mMapView.projection.visibleRegion

                val farRight = visibleRegion.farRight
                val farLeft = visibleRegion.farLeft
                val nearRight = visibleRegion.nearRight
                val nearLeft = visibleRegion.nearLeft

                val distanceWidth = FloatArray(2)
                Location.distanceBetween(
                        (farRight.latitude + nearRight.latitude) / 2,
                        (farRight.longitude + nearRight.longitude) / 2,
                        (farLeft.latitude + nearLeft.latitude) / 2,
                        (farLeft.longitude + nearLeft.longitude) / 2,
                        distanceWidth
                )


                val distanceHeight = FloatArray(2)
                Location.distanceBetween(
                        (farRight.latitude + nearRight.latitude) / 2,
                        (farRight.longitude + nearRight.longitude) / 2,
                        (farLeft.latitude + nearLeft.latitude) / 2,
                        (farLeft.longitude + nearLeft.longitude) / 2,
                        distanceHeight
                )

                //due to API might increase the radius, we want to use the smaller value
                //so make it more possible for the returning data to remain in the map
                var radius = if (distanceWidth[0] > distanceHeight[0])
                    distanceHeight[0].toInt() else distanceWidth[0].toInt()
                // distanceBetween returns value in meter,
                // so we need to divide it by 1000 to convert to km
                radius /= 1000
                //limit the radius to 20, as it might not find any result if the radius is too large
                if(radius > 20)
                    radius = 20
                else if (radius == 0)
                //if the radius is 0 (less than 1) set it to 1 as 0 radius might cause inappropriate result
                    radius = 1
                return radius
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val parentView = inflater.inflate(R.layout.fragment_maps, container, false)
        return parentView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mapView.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.getMapAsync(this)
        val sharePreference = activity.getSharedPreferences(MainActivity.SHARE_PREFERENCE_KEY, Context.MODE_PRIVATE)
        if(sharePreference.getBoolean(TUTORIAL_COMPLETE, false)) {
            onboardingLayout.visibility = View.GONE
        }else {
            onboardingLayout.setOnClickListener {
                //when the user click on the layout, mark onboarding as complete
                onboardingLayout.visibility = View.GONE
                sharePreference.edit().putBoolean(TUTORIAL_COMPLETE, true).apply()
            }
        }
    }

    override fun onMapReady(p0: GoogleMap?) {
        if(p0 != null) {
            mMapView = p0
            mMapView.uiSettings.isCompassEnabled = true
            mMapView.setOnMarkerClickListener { marker->
                //when the marker is clicked, center the map on it's location, and scroll the list to the item
//                mMapView.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
                val item = findPhotoByMarker(marker)
                if(item != null)
                    (activity as MainActivity).openDetailFragmentFromMap(item)
                true
            }
            mMapView.setOnCameraIdleListener {
                //Callback interface for when camera movement has ended.
                //store the data to DataManage for retrieval
                DataManager.instance.previousLatLng = mMapView.cameraPosition.target
                DataManager.instance.previousZoom = mMapView.cameraPosition.zoom
            }

            fab.setOnClickListener {
                zoomToDisplayAllMarker()
            }

            //initialize call to display data
            if(!DataManager.instance.hasRequestedData) {
                mMapView.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(DEFAULT_LAT, DEFAULT_LNG), DEFAULT_ZOOM))
                (activity as MainActivity).sendRequest("", true)
            }else {
                if(DataManager.instance.resultData != null) {
                    onRestoreData()
                }else {
                    (activity as MainActivity).sendRequest("", true)
                }
            }
        }
    }

    fun zoomToDisplayAllMarker() {
        if(!mPhotoMarkerMap.isEmpty()) {
            val builder = LatLngBounds.Builder()

            //add all marker location to the boundary
            for (photoItem in mPhotoMarkerMap.keys) {
                builder.include(photoItem.getLatLng())
            }
            val bounds = builder.build()
            val padding = (30 * resources.displayMetrics.density).toInt()
            mMapView.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                            bounds,
                            padding))
        }
    }

    private fun onRestoreData() {
        onPhotoReady(DataManager.instance.resultData!!.photo)
        mMapView.moveCamera(CameraUpdateFactory.newLatLngZoom(
                DataManager.instance.previousLatLng,
                DataManager.instance.previousZoom))
    }

    fun findPhotoByMarker(marker : Marker) : GsonPhoto? {
        for((key, value) in mPhotoMarkerMap) {
            if(marker == value) {
                return key
            }
        }
        return null
    }

    /**
     * callback when the data was successfully obtain from server
     * still need to handle the case that the list might be empty
     */
    override fun onPhotoReady(photos: ArrayList<GsonPhoto>) {
        mMapView.clear()
        mPhotoMarkerMap.clear()
        mTargetList.clear()
        if (photos.size > 0) {
            for (item: GsonPhoto in photos) {
                createMarker(item)
            }
        } else {
            displayErrorMessage(getString(R.string.error_empty))
        }
    }


    /**
     * callback when the request was not successful or error
     */
    override fun onDataError() {
        displayErrorMessage(getString(R.string.error_internet))
    }

    fun displayErrorMessage(message : String) {
        Snackbar.make(mapView, message, Snackbar.LENGTH_LONG).show()
    }

    // create an marker, add it to map and put it into the array list
    fun createMarker(item : GsonPhoto) {
        val generator = MarkerGenerator(activity)
        val target = MarkerImageTarget(item, generator)
        //this is needed since Picasso store target in weakference
        mTargetList.add(target)
        Picasso.with(activity).load(item.buildPhotoUrl("q"))
                .error(R.color.black)
                .into(target)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        DataManager.instance.addCallback(this)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        DataManager.instance.removeCallback(this)
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    inner class MarkerImageTarget(val item : GsonPhoto, val generator : MarkerGenerator) : Target {
        /**
         * Callback invoked right before your request is submitted.
         *
         *
         * **Note:** The passed [Drawable] may be `null` if none has been
         * specified via [RequestCreator.placeholder]
         * or [RequestCreator.placeholder].
         */
        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        }

        /**
         * Callback indicating the image could not be successfully loaded.
         *
         *
         * **Note:** The passed [Drawable] may be `null` if none has been
         * specified via [RequestCreator.error]
         * or [RequestCreator.error].
         */
        override fun onBitmapFailed(errorDrawable: Drawable?) {
            addMarker(BitmapFactory.decodeResource(activity.resources, R.color.black))
        }

        /**
         * Callback when an image has been successfully loaded.
         *
         *
         * **Note:** You must not recycle the bitmap.
         */
        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            addMarker(bitmap)
        }

        private fun addMarker(bitmap: Bitmap?) {
            mPhotoMarkerMap.put(item,
                    mMapView.addMarker(MarkerOptions()
                            .position(item.getLatLng())
                            .icon(BitmapDescriptorFactory.fromBitmap(generator.createUserMarkerImage(bitmap)))))
            mTargetList.remove(this)
        }
    }
}
