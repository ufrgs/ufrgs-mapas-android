package br.ufrgs.cpd.ufrgsmapas.ui.intro_screen


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import br.ufrgs.cpd.ufrgsmapas.R
import kotlinx.android.synthetic.main.fragment_intro_fragment2.view.*


class IntroFragment2 : Fragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_intro_fragment2, container, false)

        view.btnNext.setOnClickListener { (activity as IntroActivity).nextPage() }
        view.btnSkip.setOnClickListener { (activity as IntroActivity).closeTutorial() }

        return view
    }

    companion object {
        fun newInstance(): IntroFragment2 {
            val fragment = IntroFragment2()
            return fragment
        }
    }

}
