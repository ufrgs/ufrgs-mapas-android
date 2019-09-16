package br.ufrgs.cpd.ufrgsmapas.ui.main_screen

import br.ufrgs.cpd.ufrgsmapas.models.Building
import br.ufrgs.cpd.ufrgsmapas.models.Pin

/**
 * Created by Theo on 31/07/17.
 */
interface MainScreenContract {
    interface View {
        fun searchResult(list : List<Building>)
        fun showPins(list: ArrayList<Pin>)
    }

    interface Presenter {
        fun serchForBuilding(query : String)
        fun getAllPins()
    }
}