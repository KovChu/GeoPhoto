package com.kuanyi.geophoto.model

import com.google.android.gms.maps.model.LatLng

/**
 * The Gson data class that holds the data of the each photo
 * Created by kuanyi on 2017/3/15.
 */
class GsonPhoto(
        @com.google.gson.annotations.SerializedName("id")
        val id: String,
        @com.google.gson.annotations.SerializedName("secret")
        val secret: String,
        @com.google.gson.annotations.SerializedName("server")
        val server: String,
        @com.google.gson.annotations.SerializedName("farm")
        val farm: Int,
        @com.google.gson.annotations.SerializedName("title")
        val title: String,
        @com.google.gson.annotations.SerializedName("desciption")
        val desciption: String,
        @com.google.gson.annotations.SerializedName("ownername")
        val ownername: String,
        @com.google.gson.annotations.SerializedName("latitude")
        val latitude: Double,
        @com.google.gson.annotations.SerializedName("longitude")
        val longitude: String) {

            fun getLatLng() : LatLng {
            return LatLng(latitude, longitude.toDouble())
    }

    // https://farm{farm-id}.staticflickr.com/{server-id}/{id}_{secret}.jpg
    // use q for square(150x150) image and h for larger (1600) images

    fun buildPhotoUrl(imageSize : String) : String {
        return "https://farm$farm.staticflickr.com/$server/${id}_${secret}_${imageSize}.jpg"
//        return String.format(IMAGE_URL, farm, server, id, secret, if(isLargeSize) "h" else "q")
    }

}
