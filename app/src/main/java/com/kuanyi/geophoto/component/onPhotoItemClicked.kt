package com.kuanyi.geophoto.component

import com.kuanyi.geophoto.model.GsonPhoto

/**
 * The callback interface for the adapter
 * Created by kuanyi on 2017/3/16.
 */
interface onPhotoItemClicked {
    /**
     * the item in the adapter has been clicked
     */
    fun onItemClicked(photoItem : GsonPhoto)
}