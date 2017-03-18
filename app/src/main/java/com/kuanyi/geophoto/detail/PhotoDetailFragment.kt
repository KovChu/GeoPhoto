package com.kuanyi.geophoto.detail

import android.app.Fragment
import android.os.Bundle
import android.view.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MarkerOptions
import com.kuanyi.geophoto.R
import com.kuanyi.geophoto.model.GsonPhoto
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_photo_detail.*

/**
 * Created by kuanyi on 2017/3/17.
 */
class PhotoDetailFragment(photoItem : GsonPhoto) : Fragment(), OnMapReadyCallback {

    private val mPhotoItem = photoItem

    lateinit var mMapView : GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val parentView = inflater.inflate(R.layout.fragment_photo_detail, container, false)
        return parentView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        Picasso.with(activity)
                .load(mPhotoItem.buildPhotoUrl("h"))
                .placeholder(R.color.black).fit()
                .into(photoDetailImage)
        detailUserName.text = String.format(getString(R.string.author_name_format),
                mPhotoItem.ownername)
        detailDescription.text = mPhotoItem.desciption
        activity.toolbar.title = mPhotoItem.title
    }


    //clear the item menu
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }


    override fun onMapReady(p0: GoogleMap?) {
        if(p0 != null) {
            mMapView = p0
            mMapView.uiSettings.isCompassEnabled = false
            mMapView.moveCamera(CameraUpdateFactory.newLatLngZoom(mPhotoItem.getLatLng(), 16f))
            //override onMarkerClickListener so click on marker will not have any action
            mMapView.addMarker(MarkerOptions()
                    .position(mPhotoItem.getLatLng()))
            mMapView.setOnMarkerClickListener { true }
        }
    }

    override fun onDetach() {
        super.onDetach()
        //restore the title
        activity.toolbar.title = getString(R.string.app_name)
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