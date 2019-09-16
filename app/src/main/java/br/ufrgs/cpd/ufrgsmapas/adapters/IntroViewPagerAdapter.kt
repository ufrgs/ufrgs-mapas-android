package br.ufrgs.cpd.ufrgsmapas.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import br.ufrgs.cpd.ufrgsmapas.ui.intro_screen.IntroFragment1
import br.ufrgs.cpd.ufrgsmapas.ui.intro_screen.IntroFragment2
import br.ufrgs.cpd.ufrgsmapas.ui.intro_screen.IntroFragment3

/**
 * Created by Theo on 26/07/17.
 */
class IntroViewPagerAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        when(position) {
            0 -> return IntroFragment1.newInstance()
            1 -> return IntroFragment2.newInstance()
            2 -> return IntroFragment3.newInstance()
        }

        return IntroFragment1.newInstance()
    }

    override fun getCount(): Int = 3
}