package com.kuanyi.geophoto.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.kuanyi.geophoto.MainActivity
import com.kuanyi.geophoto.R
import com.kuanyi.geophoto.manager.DataManager
import com.kuanyi.geophoto.model.GsonPhoto
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

/**
 * The presenter for the MapFragment
 * Created by kuanyi on 2017/3/22.
 */
class MapPresenterImp(val mapViewInterface: MapViewInterface, val activity: MainActivity) : MapPresenter, DataManager.PhotoCallback {

    val mMapModel = MapModel()

    val mMarkerGenerator = MarkerGenerator(activity)

    /**
     *  a call when map view is ready to perform necessary actions
     */
    override fun onMapReady() {
        //initialize call to display data
        if(!DataManager.instance.hasRequestedData) {
            MapFragment.mMapView.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(MapFragment.DEFAULT_LAT, MapFragment.DEFAULT_LNG), MapFragment.DEFAULT_ZOOM))
            sendInitialRequest()
        }else {
            if(DataManager.instance.resultData != null) {
                onRestoreData()
            }else {
                sendInitialRequest()
            }
        }
        DataManager.instance.addCallback(this)
    }

    private fun sendInitialRequest() {
        activity.sendRequest("", true)
    }

    private fun onRestoreData() {
        onPhotoReady(DataManager.instance.resultData!!.photo)
        MapFragment.mMapView.moveCamera(CameraUpdateFactory.newLatLngZoom(
                DataManager.instance.previousLatLng,
                DataManager.instance.previousZoom))
    }


    /**
     * triggered when user clicked on a marker on map
     */
    override fun onMarkerClicked(marker: Marker) {
        val item = mMapModel.findPhotoByMarker(marker)
        if(item != null)
            activity.openDetailFragmentFromMap(item)
    }

    /**
     * triggered when user clicked on the FAB
     */
    override fun onFabClicked() {
        mapViewInterface.zoomToDisplayPhotos(mMapModel.mPhotoMarkerMap.keys)
    }

    /**
     * triggered when the map is no longer visible
     */
    override fun detach() {
        mMapModel.clearData()
        DataManager.instance.removeCallback(this)
    }

    /**
     * callback when the data was successfully obtain from server
     * still need to handle the case that the list might be empty
     */
    override fun onPhotoReady(photos: ArrayList<GsonPhoto>) {
        mapViewInterface.clearMarkers()
        mMapModel.clearData()
        if (photos.size > 0) {
            for (item: GsonPhoto in photos) {
                setupMarker(item)
            }
        } else {
            mapViewInterface.displayErrorMessage(activity.getString(R.string.error_empty))
        }
    }

    fun setupMarker(item : GsonPhoto) {
        val target = MarkerImageTarget(item)
        //this is needed since Picasso store target in WeakReference, so we need to force the target
        //to be strong reference to ensure it persist
        mMapModel.mTargetList.add(target)
        Picasso.with(activity).load(item.buildPhotoUrl("q"))
                .error(R.color.black)
                .into(target)
    }

    /**
     * callback when the request was not successful or error
     */
    override fun onDataError() {
        mapViewInterface.displayErrorMessage(activity.getString(R.string.error_internet))
    }

    inner class MarkerImageTarget(val item : GsonPhoto) : Target {
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
            mMapModel.mPhotoMarkerMap.put(item,
                    mapViewInterface.addMarker(MarkerOptions()
                            .position(item.getLatLng())
                            .icon(BitmapDescriptorFactory
                                    .fromBitmap(mMarkerGenerator.createUserMarkerImage(bitmap)))))
            mMapModel.mTargetList.remove(this)
        }
    }
}