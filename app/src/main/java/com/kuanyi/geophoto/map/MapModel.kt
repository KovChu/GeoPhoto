package com.kuanyi.geophoto.map

import com.google.android.gms.maps.model.Marker
import com.kuanyi.geophoto.model.GsonPhoto
import com.squareup.picasso.Target

/**
 * The model class holding the data related to the Map View
 * Created by kuanyi on 2017/3/22.
 */

class MapModel {

    val mPhotoMarkerMap = HashMap<GsonPhoto, Marker>()

    val mTargetList = mutableListOf<Target>()

    fun findPhotoByMarker(marker : Marker) : GsonPhoto? {
        for((key, value) in mPhotoMarkerMap) {
            if(marker == value) {
                return key
            }
        }
        return null
    }

    fun clearData() {
        mPhotoMarkerMap.clear()
        mTargetList.clear()
    }
}
