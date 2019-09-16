package br.ufrgs.cpd.ufrgsmapas.ui.intro_screen

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import br.ufrgs.cpd.ufrgsmapas.R
import br.ufrgs.cpd.ufrgsmapas.adapters.IntroViewPagerAdapter
import br.ufrgs.cpd.ufrgsmapas.ui.main_screen.MainActivity
import kotlinx.android.synthetic.main.activity_intro.*
import org.jetbrains.anko.startActivity
import android.R.id.edit
import android.content.Context
import android.content.SharedPreferences
import android.content.Context.MODE_PRIVATE
import org.jetbrains.anko.defaultSharedPreferences
import android.R.id.edit
import br.ufrgs.cpd.ufrgsmapas.BuildConfig


class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        viewpager.adapter = IntroViewPagerAdapter(supportFragmentManager)
        indicator.setViewPager(viewpager)
    }

    fun nextPage() {
        viewpager.arrowScroll(View.FOCUS_RIGHT)
    }

    fun closeTutorial(){
        val preferences = applicationContext.getSharedPreferences(BuildConfig.APPLICATION_ID, 0)

        // marca que já mostrou o onboarding
        val editor = preferences.edit()
        editor.putBoolean(resources.getString(R.string.onboarding_tag), false)
        editor.commit()

        finish()
        startActivity<MainActivity>()
    }

}
