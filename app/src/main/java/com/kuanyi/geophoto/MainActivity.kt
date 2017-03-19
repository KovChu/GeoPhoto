package com.kuanyi.geophoto

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import com.kuanyi.geophoto.detail.PhotoDetailFragment
import com.kuanyi.geophoto.list.ListFragment
import com.kuanyi.geophoto.manager.DataManager
import com.kuanyi.geophoto.map.MapFragment
import com.kuanyi.geophoto.model.GsonPhoto



/**
 * The Activity handles the fragment transitions and action bar activities
 * Created by kuanyi on 2017/3/15.
 */
class MainActivity : AppCompatActivity() {

    lateinit var mSwitchItem : MenuItem

    lateinit var mSearchMenuItem : MenuItem
    lateinit var mSearchView : SearchView

    var isDisplayMap = true

    var mapFragment = MapFragment()

    companion object {
        val FRAGMENT_MAP = "FRAGMENT_MAP"
        val FRAGMENT_LIST = "FRAGMENT_LIST"
        val FRAGMENT_DETAIL = "FRAGMENT_DETAIL"
        val SHARE_PREFERENCE_KEY = "GEOPHOTO"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("ActivityLifecycle", "onCreate")
        setContentView(R.layout.activity_main)
        //need to reset when the Activity has created
        //this is needed when the app was closed and re-opened
        DataManager.instance.resetData()
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        fragmentManager.beginTransaction()
                .add(R.id.content, mapFragment, FRAGMENT_MAP)
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        mSwitchItem = menu!!.findItem(R.id.action_switch)
        mSearchMenuItem = menu.findItem(R.id.action_search)
        mSearchView = MenuItemCompat.getActionView(mSearchMenuItem) as SearchView
        mSearchView.queryHint = getString(R.string.action_search_hint)
        // searchView's setOnQueryTextListener does not support empty string search
        // as alternate solution, we need to use the EditorActionListener for
        // the EditText within the SearchView
        val searchText = mSearchView.findViewById(R.id.search_src_text) as EditText
        searchText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                sendRequest(searchText.text.toString(), false)
                //when search started, hide the keyboard
                (getSystemService(Context.INPUT_METHOD_SERVICE)
                        as InputMethodManager).hideSoftInputFromWindow(
                        v.windowToken, 0)
                collapseSearchView()
                true
            }else {
                false
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_switch -> {
                switchMapListItem()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun switchMapListItem() {
        if(isDisplayMap) {
            mSwitchItem.setIcon(android.R.drawable.ic_menu_mapmode)
            mSwitchItem.setTitle(R.string.action_display_map)
            openListFragment()
        }else {
            mSwitchItem.setIcon(R.drawable.ic_list)
            mSwitchItem.setTitle(R.string.action_display_list)
            openMapFragment()
        }
        isDisplayMap = !isDisplayMap
    }

    fun collapseSearchView() {
        mSearchMenuItem.collapseActionView()
    }
    /*
        Priority of handling back pressed
        1. if the search bar is showing, hide it
        2. if the back stack entry count is greater than 0,
           this mean PhotoDetailFragment is present, pop it
        3. system behavior
     */
    override fun onBackPressed() {
        if (!mSearchView.isIconified) {
            collapseSearchView()
            return
        }
        if(fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            return
        }else {
            // execute default action
            super.onBackPressed()
        }
    }

    fun sendRequest(tag: String?, isFirst : Boolean) {
        DataManager.instance.sendRequest(tag, isFirst)
    }

    fun openListFragment() {
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out)
                .replace(R.id.content, ListFragment(), FRAGMENT_LIST)
                .commit()
    }

    fun openMapFragment() {
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out,
                        R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out)
                .replace(R.id.content, mapFragment, FRAGMENT_LIST)
                .commit()
    }

    fun openDetailFragmentFromMap(item : GsonPhoto) {
        fragmentManager.beginTransaction().setCustomAnimations(R.animator.slide_up_in,
                R.animator.slide_down_out, R.animator.slide_up_in,
                R.animator.slide_down_out)
                .add(R.id.content, PhotoDetailFragment(item, true), FRAGMENT_DETAIL)
                .addToBackStack(FRAGMENT_DETAIL)
                .commit()
    }

    fun openDetailFragmentFromList(item :  GsonPhoto, sharedImageView : ImageView) {
        val transaction = fragmentManager.beginTransaction()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            transaction.addSharedElement(sharedImageView, ViewCompat.getTransitionName(sharedImageView))
        }
        //share element transition only works when the transaction is replace
        transaction.replace(R.id.content, PhotoDetailFragment(item, false), FRAGMENT_DETAIL)
                .addToBackStack(FRAGMENT_DETAIL)
                .commit()
    }
}
