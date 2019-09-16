package br.ufrgs.cpd.ufrgsmapas.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Window
import android.view.WindowManager
import br.ufrgs.cpd.ufrgsmapas.R
import kotlinx.android.synthetic.main.activity_main.*



/**
 * Created by Theo on 26/07/17.
 */

object LayoutUtils {
    fun setStatusBarColor(activity: Activity, color: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.parseColor(color)
        }
    }

    fun setStatusBarColor(activity: Activity, r: Int, g: Int, b: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.rgb(r, g, b)
        }
    }

    fun setNavbarTranslucent(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            //TODO:Arrumar margem da root!
        }
    }

    fun setNavigationBarColor(activity: Activity, color: Int) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.navigationBarColor = color
        }
    }

    fun setStatusbarTranslucent(activity: Activity){
        if (Build.VERSION.SDK_INT >= 21) {

            // Set the status bar to dark-semi-transparentish
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            // Set paddingTop of toolbar to height of status bar.
            // Fixes statusbar covers toolbar issue
            activity.searchView?.setPadding(0, getStatusBarHeight(activity), 0, 0);
        }
    }

    //must be called before setContentView
    fun setFullScreen(activity: Activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE)
        activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun setupToolbar(activity: AppCompatActivity, toolbar: Toolbar, title: String?, isModal: Boolean) {

        activity.setSupportActionBar(toolbar)
        if (title != null) {
            activity.supportActionBar!!.title = title
            toolbar.setTitleTextColor(activity.resources.getColor(android.R.color.white))
        } else {
            activity.supportActionBar!!.setDisplayShowTitleEnabled(false)
        }

        if (isModal) {
            toolbar.navigationIcon = ContextCompat.getDrawable(activity, R.drawable.ic_arrow_back_white_24dp)
            toolbar.setNavigationOnClickListener { activity.onBackPressed() }
        }
    }

    // A method to find height of the status bar
    fun getStatusBarHeight(activity: Activity): Int {
        var result = 0
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = activity.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}