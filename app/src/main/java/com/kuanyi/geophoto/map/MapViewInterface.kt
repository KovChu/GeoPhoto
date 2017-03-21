package com.kuanyi.geophoto.map

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.kuanyi.geophoto.model.GsonPhoto


/**
 * The interface from Map's Presenter to View
 * The Presenter will forward all view related action to View
 * Created by kuanyi on 2017/3/22.
 */
interface MapViewInterface {

    /**
     * add a single marker on map, and return it for future reference
     */
    fun addMarker(markerOptions: MarkerOptions) : Marker

    /**
     * clear all markers on map
     */
    fun clearMarkers()

    /**
     * move the map's camera to specific location
     * shouldAnimate will determine whether the movement is animate or not
     */
    fun moveCamera(location : LatLng, shouldAnimate : Boolean)

    /**
     * move the map's camera to display all photos on the map
     */
    fun zoomToDisplayPhotos(photoList : MutableSet<GsonPhoto>)

    /**
     * display the error message
     */
    fun displayErrorMessage(errorMessage : String)

}