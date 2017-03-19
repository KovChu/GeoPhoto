package com.kuanyi.geophoto.manager

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.kuanyi.geophoto.map.MapFragment
import com.kuanyi.geophoto.model.GsonPhoto
import com.kuanyi.geophoto.model.GsonSearchResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * a singleton manager class that holds data of the data return from the server
 * it will also perform the action of calling HttpManager to obtain data
 * Created by kuanyi on 2017/3/16.
 */
class DataManager private constructor() {

    private object Holder { val INSTANCE = DataManager() }

    private val mPhotoReadyCallback = mutableListOf<PhotoCallback>()

    var hasRequestedData = false

    //store the map data that user was viewing
    var previousLatLng : LatLng? = null
    var previousZoom : Float = 0.0f

    companion object {
        val instance: DataManager by lazy { Holder.INSTANCE }
        var resultData : GsonSearchResult? = null
    }

    fun sendRequest(tag: String?, isFirst : Boolean) {
        hasRequestedData = true
        Log.i("DataManager", "getting data for tag = " + tag +
                " lat = " + MapFragment.currentLat +
                ", lng = " + MapFragment.currentLng +
                ", radius = " + MapFragment.radius)
        HttpManager.instance
                    .requestPhoto(tag,
                            if(isFirst) MapFragment.DEFAULT_LAT else MapFragment.currentLat,
                            if(isFirst) MapFragment.DEFAULT_LNG else MapFragment.currentLng,
                            if(isFirst) MapFragment.DEFAULT_RADIUS else MapFragment.radius)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe (
                            { retrievedData ->
                                if(retrievedData != null) {
                                    Log.i("DataManager", "received data with size = " + retrievedData.photo.size)
                                    resultData = retrievedData
                                    //provide data back to fragment via callback interface
                                    for (callback : PhotoCallback in mPhotoReadyCallback) {
                                        callback.onPhotoReady(retrievedData.photo)
                                    }
                                }else {
                                    onRetrieveError()
                                }

                            },
                            { e ->
                                onRetrieveError()
                            }
                    )
    }

    fun onRetrieveError() {
        for (callback : PhotoCallback in mPhotoReadyCallback) {
            callback.onDataError()
        }
    }

    fun addCallback(callback : PhotoCallback) {
        mPhotoReadyCallback.add(callback)
    }


    interface PhotoCallback {
        /**
         * callback when the data was successfully obtain from server
         * still need to handle the case that the list might be empty
         */
        fun onPhotoReady(photos : ArrayList<GsonPhoto>)

        /**
         * callback when the request was not successful or error
         */
        fun onDataError()
    }

    fun removeCallback(callback : PhotoCallback) {
        mPhotoReadyCallback.remove(callback)
    }

    fun resetData() {
        hasRequestedData = false
        resultData = null
        mPhotoReadyCallback.clear()
    }
}
