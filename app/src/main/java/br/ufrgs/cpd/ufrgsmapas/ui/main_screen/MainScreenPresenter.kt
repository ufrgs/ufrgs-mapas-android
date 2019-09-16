package br.ufrgs.cpd.ufrgsmapas.ui.main_screen

import android.content.Context
import br.ufrgs.cpd.ufrgsmapas.db.DatabaseAdapter
import br.ufrgs.cpd.ufrgsmapas.models.Building
import br.ufrgs.cpd.ufrgsmapas.models.Pin

/**
 * Created by Theo on 31/07/17.
 */
class MainScreenPresenter(val context : Context, val view : MainScreenContract.View, val dba : DatabaseAdapter) : MainScreenContract.Presenter {

    override fun getAllPins() {
        view.showPins(this.makeAllPins())
    }

    override fun serchForBuilding(query: String) {
        view.searchResult(dba.searchForBuilding(query))
    }

    fun makeAllPins() : ArrayList<Pin> {

        val allBuildings = dba.getAllBuildings()
        val pins = arrayListOf<Pin>()

        for(i in 0..(allBuildings.size -1)) {

            var pinToAddBuilding: Pin? = null

            for(j in 0..(pins.size - 1)) {
                if (pins[j].latitude == allBuildings[i].latitude && pins[j].longitude == allBuildings[i].longitude) {
                    pinToAddBuilding = pins[j]
                    break
                }
            }

            // se já existe um pin com aquelas coordenadas, adiciona o prédio no array do pin
            if (pinToAddBuilding != null) {
                pinToAddBuilding.buildings.add(allBuildings[i])
            }
            // senão, cria novo pin e adiciona o prédio no array dele
            else {
                val b: Building = allBuildings[i]
                var newPin = Pin(b.latitude, b.longitude, arrayListOf<Building>(b), i)

                pins.add(newPin)
            }
        }

        return pins
    }
}