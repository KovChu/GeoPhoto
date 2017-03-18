package com.kuanyi.geophoto.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.support.v4.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.kuanyi.geophoto.R
import com.kuanyi.geophoto.model.GsonPhoto



/**
 * marker manipulate class that will handle basic marker creation,
 * enlarge or reset marker
 * Created by kuanyi on 2017/3/16.
 */
class MarkerGenerator {

    companion object {

        @JvmStatic fun createMarker(item : GsonPhoto) : MarkerOptions {
            return MarkerOptions().position(item.getLatLng())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker))
                    .title(item.id)
                    .zIndex(1f)
        }

        @JvmStatic fun enlargeMarker(marker: Marker?, context: Context) {
            val d = ContextCompat.getDrawable(context, R.drawable.ic_map_marker_selected)

            d.level = 1234
            val b = (d.current as BitmapDrawable).bitmap
            val scaleBitmap = Bitmap.createScaledBitmap(
                    b, (b.width * 1.3).toInt(), (b.height * 1.3).toInt(), false)
            marker?.setIcon(BitmapDescriptorFactory.fromBitmap(scaleBitmap))
            //bring the marker to front to be visible
            marker?.zIndex = 10f
        }

        @JvmStatic fun resetMarker(marker : Marker?) {
            marker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker))
        }

    }
}
