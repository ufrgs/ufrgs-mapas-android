package br.ufrgs.cpd.ufrgsmapas.ui.splash_screen

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import br.ufrgs.cpd.ufrgsmapas.BuildConfig
import br.ufrgs.cpd.ufrgsmapas.R
import br.ufrgs.cpd.ufrgsmapas.ui.intro_screen.IntroActivity
import br.ufrgs.cpd.ufrgsmapas.ui.main_screen.MainActivity
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.startActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val preferences = applicationContext.getSharedPreferences(BuildConfig.APPLICATION_ID, 0)

        // se abriu o app pela primeira vez
        if (preferences.getBoolean(resources.getString(R.string.onboarding_tag), true)) {

            // mostra o onboarding
            startActivity<IntroActivity>()
        } else {
            startActivity<MainActivity>()
        }

        finish()

    }
}
