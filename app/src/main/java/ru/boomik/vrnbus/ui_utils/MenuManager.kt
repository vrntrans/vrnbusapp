package ru.boomik.vrnbus.ui_utils

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import ru.boomik.vrnbus.R

class MenuManager(activity: Activity) {


    private lateinit var mRefreshMenuItem: MenuItem
    private lateinit var mSearchView: SearchView
    private lateinit var mRotationRefreshAnimation: Animation
    private var mActivity: Activity = activity

    private lateinit var mRefreshClickedCallback: () -> Unit
    private lateinit var mBusClickedCallback: () -> Unit
    private lateinit var mQuerySubmitCallback: (String) -> Unit

    fun createOptionsMenu(menu: Menu) {
        val menuInflater = mActivity.menuInflater
        menuInflater.inflate(R.menu.map_menu, menu)
        mRefreshMenuItem = menu.findItem(R.id.refresh)
        val inflater = mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val iv = inflater.inflate(R.layout.iv_refresh, null) as ImageView
        mRefreshMenuItem.actionView=iv

        mRotationRefreshAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.rotate_refresh)
        mRotationRefreshAnimation.repeatCount = Animation.INFINITE

        // Associate searchable configuration with the SearchView
        val searchManager = mActivity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        mSearchView = menu.findItem(R.id.search).actionView as SearchView
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(mActivity.componentName))
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                mQuerySubmitCallback(query)
                return false
            }
        })
    }

    fun subscribeRefresh(callback: () -> Unit) {
        mRefreshClickedCallback = callback
    }

    fun subscribeBus(callback: () -> Unit) {
        mBusClickedCallback = callback
    }


    fun subscribeQuerySubmit(callback: (String) -> Unit) {
        mQuerySubmitCallback = callback
    }



    fun startUpdate() {
        mRefreshMenuItem.actionView.startAnimation(mRotationRefreshAnimation)
    }

    fun stopUpdate() {
        mRefreshMenuItem.actionView?.clearAnimation()
    }

    fun optionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                mRefreshClickedCallback()
                true
            }
            R.id.select_bus -> {
                mBusClickedCallback()
                true
            }
            else -> false
        }
    }

}