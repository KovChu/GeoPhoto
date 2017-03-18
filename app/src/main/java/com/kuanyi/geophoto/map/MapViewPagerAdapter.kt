package com.kuanyi.geophoto.map

import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.kuanyi.geophoto.R
import com.kuanyi.geophoto.component.onPhotoItemClicked
import com.kuanyi.geophoto.model.GsonPhoto
import com.squareup.picasso.Picasso

/**
 * The adapter for map's ViewPager
 * Created by kuanyi on 2017/3/16.
 */

class MapViewPagerAdapter(val callback: onPhotoItemClicked) : PagerAdapter() {

    var photoList = mutableListOf<GsonPhoto>()

    fun setData(photoList : ArrayList<GsonPhoto>) {
        this.photoList.addAll(photoList)
        notifyDataSetChanged()
    }

    fun clearData() {
        photoList.clear()
        notifyDataSetChanged()
    }

    fun getItemPosition(item : GsonPhoto): Int {
        return photoList.indices.firstOrNull { item.id == photoList[it].id }
                ?: 0
    }


    override fun getItemPosition(`object`: Any?): Int {
        //requires this to force View Pager to refresh the list
        return PagerAdapter.POSITION_NONE
    }
    /**
     * Determines whether a page View is associated with a specific key object
     * as returned by [.instantiateItem]. This method is
     * required for a PagerAdapter to function properly.

     * @param view Page View to check for association with `object`
     * *
     * @param object Object to check for association with `view`
     * *
     * @return true if `view` is associated with the key object `object`
     */
    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        return view === `object`
    }

    /**
     * Return the number of views available.
     */
    override fun getCount(): Int {
        return photoList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val context = container.context
        val itemView = LayoutInflater.from(context).inflate(R.layout.map_photo_list_item, container, false)
        val item = photoList[position]
        Picasso.with(context).load(item.buildPhotoUrl(false))
                .fit()
                .placeholder(R.color.black)
                .into(itemView.findViewById(R.id.listItemImage) as ImageView)
        (itemView.findViewById(R.id.listItemTitle) as TextView).text = item.title
        (itemView.findViewById(R.id.listItemUserName) as TextView).text =
                String.format(context.getString(R.string.author_name_format),
                        item.ownername)
        itemView.setOnClickListener {
            callback.onItemClicked(item)
        }
        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }
}
