package com.kuanyi.geophoto.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.kuanyi.geophoto.R


/**
 * marker manipulate class that will handle basic marker creation,
 * enlarge or reset marker
 * Created by kuanyi on 2017/3/16.
 */
class MarkerGenerator(val context : Context) {

    lateinit private var mContainer: ViewGroup
    lateinit private var mUserImage: ImageView

    fun createUserMarkerImage(image : Bitmap?) : Bitmap? {
        setupView()
        mUserImage.setImageBitmap(image)
        return makeIcon()
    }

    /**
     * Ensure views are ready. This allows us to lazily inflate the main layout.
     */
    private fun setupView() {
        mContainer = LayoutInflater.from(context).inflate(R.layout.map_marker, null) as ViewGroup
        mContainer.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)
        mUserImage = mContainer.findViewById(R.id.markerImage) as ImageView
    }

    /**
     * Creates an icon with the current content and style.
     *
     *
     * This method is useful if a custom view has previously been set, or if text content is not
     * applicable.
     */
    private fun makeIcon(): Bitmap? {
        try {
            val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            mContainer.measure(measureSpec, measureSpec)

            val measuredWidth = mContainer.measuredWidth
            val measuredHeight = mContainer.measuredHeight
            if (measuredHeight == 0 || measuredWidth == 0) {
                //when the width or height is 0, this mean there is something wrong with the container
                //do not proceed or crash will happen
                return null
            }
            mContainer.layout(0, 0, measuredWidth, measuredHeight)

            val r = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)

            r.eraseColor(Color.TRANSPARENT)

            val canvas = Canvas(r)
            mContainer.draw(canvas)
            return r
        } catch (e: IllegalStateException) {
            //java.lang.IllegalStateException: onMeasure() did not set the measured dimension by calling setMeasuredDimension()
            //will occur periodically when running container.measure.
            //use a try/catch to catch the exception and return null to force the IconManager to reset and draw again.
            e.printStackTrace()
            return null
        }
    }

}
