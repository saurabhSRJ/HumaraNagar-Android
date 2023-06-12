package com.humara.nagar.ui.common

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

abstract class EndlessRecyclerViewScrollListener : RecyclerView.OnScrollListener {
    private var visibleThreshold = 20
    private var currentPage = 0
    private var previousTotalItemCount = 0
    private var loading = true
    private val startingPageIndex = 0
    var mLayoutManager: RecyclerView.LayoutManager

    constructor(layoutManager: RecyclerView.LayoutManager, visibleThreshold: Int) {
        mLayoutManager = layoutManager
        this.visibleThreshold = visibleThreshold
    }

    constructor(layoutManager: LinearLayoutManager, visibleThreshold: Int) {
        mLayoutManager = layoutManager
        this.visibleThreshold = visibleThreshold
    }

    constructor(layoutManager: GridLayoutManager, visibleThreshold: Int) {
        mLayoutManager = layoutManager
        this.visibleThreshold = visibleThreshold * layoutManager.spanCount
    }

    constructor(layoutManager: StaggeredGridLayoutManager, visibleThreshold: Int) {
        mLayoutManager = layoutManager
        this.visibleThreshold = visibleThreshold * layoutManager.spanCount
    }

    fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in lastVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i]
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }
        return maxSize
    }

    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        var lastVisibleItemPosition = 0
        val totalItemCount = mLayoutManager.itemCount
        when (mLayoutManager) {
            is StaggeredGridLayoutManager -> {
                val lastVisibleItemPositions = (mLayoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(null as IntArray?)
                lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions)
            }
            is GridLayoutManager -> {
                lastVisibleItemPosition = (mLayoutManager as GridLayoutManager).findLastVisibleItemPosition()
            }
            is LinearLayoutManager -> {
                lastVisibleItemPosition = (mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            }
        }
        if (totalItemCount < previousTotalItemCount) {
            currentPage = startingPageIndex
            previousTotalItemCount = totalItemCount
            if (totalItemCount == 0) {
                loading = true
            }
        }
        if (loading && totalItemCount > previousTotalItemCount) {
            loading = false
            previousTotalItemCount = totalItemCount
        }
        if (!loading && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
            ++currentPage
            onLoadMore(currentPage, totalItemCount, view)
            loading = true
        }
    }

    fun resetState() {
        currentPage = startingPageIndex
        previousTotalItemCount = 0
        loading = true
    }

    abstract fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?)
}