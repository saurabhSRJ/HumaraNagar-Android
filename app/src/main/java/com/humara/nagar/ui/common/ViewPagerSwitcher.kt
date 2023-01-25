package com.humara.nagar.ui.common

import android.os.Handler
import android.os.Looper
import androidx.viewpager2.widget.ViewPager2

/**
 * Circular auto-scrolling ViewPager switcher
 */
class ViewPagerSwitcher(private val mViewPager: ViewPager2) {

    fun pageSwitcher(seconds: Int) {
        if (mViewPager.tag == null) switchViewPager(seconds)
    }

    private fun switchViewPager(seconds: Int) {
        mViewPager.tag = "TimeAdded"
        Handler(Looper.getMainLooper()).postDelayed({
            var currentPosition = mViewPager.currentItem
            if (currentPosition == mViewPager.adapter!!.itemCount - 1) {
                mViewPager.currentItem = 0
            } else {
                currentPosition++
                mViewPager.currentItem = currentPosition
            }
            switchViewPager(seconds)
        }, (seconds * 1000).toLong())
    }
}