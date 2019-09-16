package br.ufrgs.cpd.ufrgsmapas.db

import android.content.Context
import br.ufrgs.cpd.ufrgsmapas.models.Building
import br.ufrgs.cpd.ufrgsmapas.utils.BuildingsFetcher

/**
 * Created by Theo on 31/07/17.
 */
class DatabaseAdapter(val mContext : Context) {

    private var localStorageManager: LocalStorageManager = LocalStorageManager(mContext)

    init {
        if (localStorageManager.isEmpty()) {
            populateDB()
        }
    }

    fun searchForBuilding(query : String) : ArrayList<Building> {
        return localStorageManager.getBuildingsByNameOrNumber(query)
    }

    fun getAllBuildings() : ArrayList<Building> {
        return localStorageManager.getAllBuildings()
    }

    private fun populateDB() {
        val buildings = BuildingsFetcher(mContext).getBuildings {
            if (it == null) {
                localStorageManager.save(arrayListOf<Building>())
            } else {
                localStorageManager.save(it)
            }
        }
    }

}