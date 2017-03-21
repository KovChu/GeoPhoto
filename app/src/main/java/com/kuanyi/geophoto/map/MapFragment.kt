package com.kuanyi.geophoto.map

import android.app.Fragment
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.kuanyi.geophoto.MainActivity
import com.kuanyi.geophoto.R
import com.kuanyi.geophoto.manager.DataManager
import com.kuanyi.geophoto.model.GsonPhoto
import kotlinx.android.synthetic.main.fragment_maps.*


/**
 * The Fragment that displays the photo's location on map
 * Created by kuanyi on 2017/3/15.
 */
class MapFragment : Fragment(), OnMapReadyCallback,
        MapViewInterface {

    lateinit var mMapPresenter : MapPresenter

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
        mMapPresenter = MapPresenterImp(this, activity as MainActivity)
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

                mMapPresenter.onMarkerClicked(marker)
                true
            }
            mMapView.setOnCameraIdleListener {
                //Callback interface for when camera movement has ended.
                //store the data to DataManage for retrieval
                DataManager.instance.previousLatLng = mMapView.cameraPosition.target
                DataManager.instance.previousZoom = mMapView.cameraPosition.zoom
            }

            fab.setOnClickListener {
                mMapPresenter.onFabClicked()
            }
            mMapPresenter.onMapReady()
        }
    }

    /**
     * add a single marker on map, and return it for future reference
     */
    override fun addMarker(markerOptions: MarkerOptions): Marker {
        return mMapView.addMarker(markerOptions)
    }

    /**
     * clear all markers on map
     */
    override fun clearMarkers() {
        mMapView.clear()
    }

    /**
     * move the map's camera to specific location
     * shouldAnimate will determine whether the movement is animate or not
     */
    override fun moveCamera(location: LatLng, shouldAnimate: Boolean) {
        if(shouldAnimate) {
            mMapView.animateCamera(CameraUpdateFactory.newLatLng(location))
        }else {
            mMapView.moveCamera(CameraUpdateFactory.newLatLng(location))
        }
    }

    /**
     * move the map's camera to display all photos on the map
     */
    override fun zoomToDisplayPhotos(photoList: MutableSet<GsonPhoto>) {
        if(!photoList.isEmpty()) {
            val builder = LatLngBounds.Builder()

            //add all marker location to the boundary
            for (photoItem in photoList) {
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

    /**
     * display the error message
     */
    override fun displayErrorMessage(errorMessage: String) {
        Snackbar.make(mapView, errorMessage, Snackbar.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        mMapPresenter.detach()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
