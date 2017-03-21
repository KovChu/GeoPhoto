package com.kuanyi.geophoto.map

import com.google.android.gms.maps.model.Marker

/**
 * The interface from Map's View to Presenter
 * View should forward all actions to presenter to handle
 * Created by kuanyi on 2017/3/22.
 */
interface MapPresenter {

    /**
     *  a call when map view is ready to perform necessary actions
     */
    fun onMapReady()

    /**
     * triggered when user clicked on a marker on map
     */
    fun onMarkerClicked(marker : Marker)

    /**
     * triggered when user clicked on the FAB
     */
    fun onFabClicked()

    /**
     * triggered when the map is no longer visible
     */
    fun detach()
}