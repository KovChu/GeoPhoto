package com.kuanyi.geophoto.manager

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kuanyi.geophoto.model.GsonSearchResult
import io.reactivex.Observable
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * * The class holds the reference to the OkHttpClient and take cares all the HTTP calls.
 * Created by kuanyi on 2017/3/15.
 */
class HttpManager private constructor(){

    private object Holder { val INSTANCE = HttpManager() }

    companion object {
        val instance: HttpManager by lazy { Holder.INSTANCE }
    }

    private val API_URL : String = "https://api.flickr.com/services/rest/?method=flickr.photos.search"

    private val API_KEY : String = "bd51bf0d4026eb36132d913f1644a973"

    private var mHttpClient: OkHttpClient

    private val DEFAULT_CONNECTION_TIMEOUT: Long = 30

    private val DEFAULT_WRITE_TIMEOUT: Long = 60

    private val DEFAULT_READ_TIMEOUT: Long = 30

    init {
        mHttpClient = OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS).build()
    }

    fun requestPhoto(tags : String?, lat: Double, lon: Double, radius: Int) : Observable<GsonSearchResult> {
        val formBody = buildDefaultRequestBody()
                .add("lat", lat.toString())
                .add("lon", lon.toString())
                .add("radius", radius.toString())
        if(tags != null && !tags.isEmpty()) {
            formBody.add("tag", tags)
        }

        val request : Request = Request.Builder()
                .url(API_URL)
                .post(formBody.build())
                .build()
        return Observable.create {
            subscriber ->

            val response = mHttpClient.newCall(request).execute()
            val photoResult : GsonSearchResult
            try {
                if (response.isSuccessful) {
                    //request successful
                    val type = object : TypeToken<GsonSearchResult>() {}.type
                    val jsonObject: JSONObject = JSONObject(response.body().string())
                    photoResult = Gson().fromJson<GsonSearchResult>(jsonObject.getString("photos"), type)
                    subscriber.onNext(photoResult)
                    subscriber.onComplete()
                }
            }catch (e : Exception) {
                subscriber.onError(e)
            }

        }


    }

    private fun buildDefaultRequestBody() : FormBody.Builder {
        return FormBody.Builder()
                .add("api_key", API_KEY)
                .add("format", "json")
                .add("has_geo", true.toString())
                .add("extras", "description,geo,owner_name")
                //defaults to 30 data per request
                .add("per_page", 30.toString())
                .add("nojsoncallback", 1.toString())
    }

}
