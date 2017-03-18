package com.kuanyi.geophoto.map

import android.app.Fragment
import android.location.Location
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.kuanyi.geophoto.MainActivity
import com.kuanyi.geophoto.R
import com.kuanyi.geophoto.component.MarkerGenerator
import com.kuanyi.geophoto.component.onPhotoItemClicked
import com.kuanyi.geophoto.manager.DataManager
import com.kuanyi.geophoto.model.GsonPhoto
import kotlinx.android.synthetic.main.fragment_maps.*
import java.util.*


/**
 * Created by kuanyi on 2017/3/15.
 */
class MapFragment : Fragment(), OnMapReadyCallback,
        DataManager.PhotoCallback, ViewPager.OnPageChangeListener, onPhotoItemClicked {

    private var mPhotoAdapter = MapViewPagerAdapter(this)

    private var mPhotoMarkerMap = HashMap<GsonPhoto, Marker>()

    private var mLastSelectedMarker : Marker? = null


    companion object {
        private lateinit var mMapView : GoogleMap
        val DEFAULT_LAT = 35.6775219
        val DEFAULT_LNG = 139.7524709
        val DEFAULT_RADIUS = 3
        val DEFAULT_ZOOM = 14.0f
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

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retain this fragment across configuration changes.
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val parentView = inflater.inflate(R.layout.fragment_maps, container, false)
        return parentView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        DataManager.instance.addCallback(this)

        mapPhotoList.adapter = mPhotoAdapter
        mapPhotoList.addOnPageChangeListener(this)
    }

    override fun onMapReady(p0: GoogleMap?) {
        if(p0 != null) {
            mMapView = p0
            mMapView.uiSettings.isCompassEnabled = true
            mMapView.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(DEFAULT_LAT, DEFAULT_LNG), DEFAULT_ZOOM))
            mMapView.setOnMarkerClickListener { marker->
                //when the marker is clicked, center the map on it's location, and scroll the list to the item
                mMapView.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
                val item = findPhotoByMarker(marker)
                if(item != null)
                    mapPhotoList.setCurrentItem(mPhotoAdapter.getItemPosition(item), true)
                true
            }
            //initialize call to display data
            (activity as MainActivity).sendRequest("", true)
        }
    }

    fun findPhotoByMarker(marker : Marker) : GsonPhoto? {
        for(entry : MutableMap.MutableEntry<GsonPhoto, Marker> in mPhotoMarkerMap) {
            if(marker == entry.value) {
                return entry.key
            }
        }
        return null
    }
    /**
     * the item in the adapter has been clicked
     */
    override fun onItemClicked(photoItem: GsonPhoto) {
        (activity as MainActivity).openDetailFragment(photoItem)
    }


    /**
     * callback when the data was successfully obtain from server
     * still need to handle the case that the list might be empty
     */
    override fun onPhotoReady(photos: ArrayList<GsonPhoto>) {
        //reset the data first
        mLastSelectedMarker = null
        mMapView.clear()
        mPhotoMarkerMap.clear()
        mPhotoAdapter.clearData()

        mPhotoAdapter.setData(photos)
        if(photos.size > 0) {
            errorTextView.visibility = View.GONE
            for(item : GsonPhoto in photos) {
                createMarker(item)
            }
            zoomToDisplayAllMarker()
            //move to the first item
            mapPhotoList.setCurrentItem(0, true)
        }else {
            displayErrorMessage(getString(R.string.error_empty))
        }

    }

    fun zoomToDisplayAllMarker() {

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

    fun displayMarker(item : GsonPhoto) {
        val marker = mPhotoMarkerMap[item]
        if(mLastSelectedMarker != null) {
            MarkerGenerator.resetMarker(mLastSelectedMarker)
        }
        MarkerGenerator.enlargeMarker(marker, activity)
        mLastSelectedMarker = marker
        //center to the marker
//        mMapView.animateCamera(CameraUpdateFactory.newLatLngZoom(item.getLatLng(), 16f))
    }

    /**
     * callback when the request was not successful or error
     */
    override fun onDataError() {
        displayErrorMessage(getString(R.string.error_internet))
    }

    fun displayErrorMessage(message : String) {
        errorTextView.visibility = View.VISIBLE
        errorTextView.text = message
    }


    /**
     * Called when the scroll state changes. Useful for discovering when the user
     * begins dragging, when the pager is automatically settling to the current page,
     * or when it is fully stopped/idle.

     * @param state The new scroll state.
     * *
     * @see ViewPager.SCROLL_STATE_IDLE

     * @see ViewPager.SCROLL_STATE_DRAGGING

     * @see ViewPager.SCROLL_STATE_SETTLING
     */
    override fun onPageScrollStateChanged(state: Int) {
    }

    /**
     * This method will be invoked when the current page is scrolled, either as part
     * of a programmatically initiated smooth scroll or a user initiated touch scroll.

     * @param position Position index of the first page currently being displayed.
     * *                 Page position+1 will be visible if positionOffset is nonzero.
     * *
     * @param positionOffset Value from [0, 1) indicating the offset from the page at position.
     * *
     * @param positionOffsetPixels Value in pixels indicating the offset from position.
     */
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    /**
     * This method will be invoked when a new page becomes selected. Animation is not
     * necessarily complete.

     * @param position Position index of the new selected page.
     */
    override fun onPageSelected(position: Int) {
        displayMarker(mPhotoAdapter.photoList[position])
    }

    // create an marker, add it to map and put it into the array list
    fun createMarker(item : GsonPhoto) {
        mPhotoMarkerMap.put(item,
                mMapView.addMarker(MarkerGenerator.createMarker(item)))
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
