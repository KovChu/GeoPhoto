package com.kuanyi.geophoto.detail

import android.app.Fragment
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.postponeEnterTransition
import android.transition.TransitionInflater
import android.view.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MarkerOptions
import com.kuanyi.geophoto.R
import com.kuanyi.geophoto.manager.DataManager
import com.kuanyi.geophoto.model.GsonPhoto
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_photo_detail.*

/**
 * The detail fragment that will display an item's image,
 * location, title, user and description
 * Created by kuanyi on 2017/3/17.
 */
class PhotoDetailFragment : Fragment(), OnMapReadyCallback {

    lateinit var photoItem : GsonPhoto
    var isFromMap = false

    companion object {
        val PHOTO_ITEM_KEY = "PHOTO_ITEM_KEY"
        val FROM_MAP_KEY = "FROM_MAP_KEY"
        fun newInstance(id : String, isFromMap : Boolean) : PhotoDetailFragment {
            val args = Bundle()
            args.putString(PHOTO_ITEM_KEY, id)
            args.putBoolean(FROM_MAP_KEY, isFromMap)
            val fragment = PhotoDetailFragment()
            fragment.arguments = args
            return fragment
        }
    }

    lateinit var mMapView : GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val parentView = inflater.inflate(R.layout.fragment_photo_detail, container, false)

        val item = DataManager.instance.getPhotoItemById(arguments.getString(PHOTO_ITEM_KEY))
        if(item == null) {
            //if the data is not present, pop the fragment
            fragmentManager.popBackStack()
        }else {
            photoItem = item
        }
        isFromMap = arguments.getBoolean(FROM_MAP_KEY)
        //only need this when calling from the ListFragment
        if(!isFromMap && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition(activity)
            sharedElementEnterTransition = TransitionInflater
                    .from(activity)
                    .inflateTransition(android.R.transition.move);
        }
        return parentView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        detailUserName.text = String.format(getString(R.string.author_name_format),
                photoItem.ownername)
        detailDescription.text = photoItem.desciption
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Picasso.with(activity)
                .load(photoItem.buildPhotoUrl("h"))
                .placeholder(R.color.black)
                .into(photoDetailImage)

        if (!isFromMap && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            photoDetailImage.transitionName = photoItem.title
            ActivityCompat.startPostponedEnterTransition(activity)
        }
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
            mMapView.moveCamera(CameraUpdateFactory.newLatLngZoom(photoItem.getLatLng(), 16f))
            //override onMarkerClickListener so click on marker will not have any action
            mMapView.addMarker(MarkerOptions()
                    .position(photoItem.getLatLng()))
            mMapView.setOnMarkerClickListener { true }
        }
    }

    override fun onDetach() {
        super.onDetach()
        //restore the title when detach
        activity.toolbar.title = getString(R.string.app_name)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        activity.toolbar.title = photoItem.title
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