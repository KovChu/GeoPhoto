package com.kuanyi.geophoto.list

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.kuanyi.geophoto.MainActivity
import com.kuanyi.geophoto.R
import com.kuanyi.geophoto.manager.DataManager
import com.kuanyi.geophoto.model.GsonPhoto
import kotlinx.android.synthetic.main.fragment_list.*



/**
 * Created by kuanyi on 2017/3/17.
 */
class ListFragment : Fragment(), DataManager.PhotoCallback, ListRecyclerViewAdapter.onPhotoItemClickListener {

    private val mRecyclerAdapter = ListRecyclerViewAdapter(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val parentView = inflater.inflate(R.layout.fragment_list, container, false)
        DataManager.instance.addCallback(this)
        return parentView
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val column = calculateColumnCount()
        listRecyclerView.layoutManager = GridLayoutManager(activity, column)
        listRecyclerView.addItemDecoration(GridSpacingItemDecorator(column, dpToPx(8), true))
        listRecyclerView.itemAnimator = DefaultItemAnimator()
        listRecyclerView.adapter = mRecyclerAdapter
        if(DataManager.resultData != null) {
            onPhotoReady(DataManager.resultData!!.photo)
        }
    }

    private fun calculateColumnCount(): Int {

        Log.i("Screen", "width = " + resources.displayMetrics.widthPixels
                + ", height = " + resources.displayMetrics.heightPixels)
        return (resources.displayMetrics.widthPixels / (180 * resources.displayMetrics.density)).toInt()
    }

    override fun onDetach() {
        super.onDetach()
        DataManager.instance.removeCallback(this)
    }

    /**
     * Converting dp to pixel
     */
    private fun dpToPx(dp: Int): Int {
        val r = resources
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics))
    }


    /**
     * callback when the data was successfully obtain from server
     * still need to handle the case that the list might be empty
     */
    override fun onPhotoReady(photos: ArrayList<GsonPhoto>) {
        mRecyclerAdapter.clearData()
        mRecyclerAdapter.setData(photos)
        if(photos.size > 0) {
            errorTextView.visibility = View.GONE
            listRecyclerView.smoothScrollToPosition(0)
        }else {
            displayErrorMessage(getString(R.string.error_empty))
        }
    }

    /**
     * callback when the request was not successful or error
     */
    override fun onDataError() {
        displayErrorMessage(getString(R.string.error_internet))
    }

    fun displayErrorMessage(message : String) {
        errorTextView.visibility = View.VISIBLE
        errorTextView.text = message
    }

    /**
     * the item in the adapter has been clicked
     */
    override fun onItemClicked(item: GsonPhoto, imageView : ImageView) {
        (activity as MainActivity).openDetailFragmentFromList(item, imageView)
    }


}