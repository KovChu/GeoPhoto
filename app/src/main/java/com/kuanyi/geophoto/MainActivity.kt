package com.kuanyi.geophoto

import android.app.FragmentTransaction
import android.content.Context
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.kuanyi.geophoto.detail.PhotoDetailFragment
import com.kuanyi.geophoto.list.ListFragment
import com.kuanyi.geophoto.manager.DataManager
import com.kuanyi.geophoto.map.MapFragment
import com.kuanyi.geophoto.model.GsonPhoto



class MainActivity : AppCompatActivity() {

    lateinit var mSwitchItem : MenuItem

    lateinit var mSearchMenuItem : MenuItem
    lateinit var mSearchView : SearchView

    var isDisplayMap = true

    companion object {
        val FRAGMENT_MAP = "FRAGMENT_MAP"
        val FRAGMENT_LIST = "FRAGMENT_LIST"
        val FRAGMENT_DETAIL = "FRAGMENT_DETAIL"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        fragmentManager.beginTransaction()
                .add(R.id.content, MapFragment(), FRAGMENT_MAP)
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
                Log.i("SearchView", "User has pressed search")
                sendRequest(searchText.text.toString(), false)
                //when search started, hide the keyboard
                (getSystemService(Context.INPUT_METHOD_SERVICE)
                        as InputMethodManager).hideSoftInputFromWindow(
                        v.windowToken, 0)
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
        val id = item.itemId

        when (id) {
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
            removeListFragment()
        }
        isDisplayMap = !isDisplayMap
    }

    fun collapseSearchView() {
        MenuItemCompat.collapseActionView(mSwitchItem)
    }
    /*
        Priority of handling back pressed
        1. if the search bar is showing, hide it
        2. if the back stack entry count is greater than 0, this mean PhotoDetailFragment is present, pop it
        3. if the list fragment is present, pop it
        4. system behavior
     */
    override fun onBackPressed() {
        if (!mSearchView.isIconified) {
            collapseSearchView()
            return
        }
        if(fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            return
        }else if(removeListFragment()) {
            switchMapListItem()
        }else {
            // execute default action
            super.onBackPressed()
        }
    }

    fun removeListFragment() : Boolean {
        val listFragment = fragmentManager.findFragmentByTag(FRAGMENT_LIST)
        if(listFragment != null) {
            fragmentManager.beginTransaction().remove(listFragment).commit()
            return true
        }
        return false
    }

    fun sendRequest(tag: String?, isFirst : Boolean) {
        DataManager.instance.sendRequest(tag, isFirst)
    }

    fun openListFragment() {
        fragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.content, ListFragment(), FRAGMENT_LIST)
                .commit()
    }

    fun openDetailFragment(item : GsonPhoto) {
        fragmentManager.beginTransaction()
                .add(R.id.content, PhotoDetailFragment(item), FRAGMENT_DETAIL)
                .addToBackStack(FRAGMENT_DETAIL)
                .commit()
    }
}
