package com.kuanyi.geophoto.model

/**
 * The Gson data class that holds the data return from the server
 * Created by kuanyi on 2017/3/15.
 */
class GsonSearchResult (
        @com.google.gson.annotations.SerializedName("page")
        val page: Int,
        @com.google.gson.annotations.SerializedName("pages")
        val pages: Int,
        @com.google.gson.annotations.SerializedName("perPage")
        val perPage: Int,
        @com.google.gson.annotations.SerializedName("total")
        val total: String) {
    val photo: ArrayList<GsonPhoto> = ArrayList()
}
