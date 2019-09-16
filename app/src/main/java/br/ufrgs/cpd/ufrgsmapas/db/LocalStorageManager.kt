package br.ufrgs.cpd.ufrgsmapas.db

import io.realm.Realm
import io.realm.RealmConfiguration
import android.content.Context
import br.ufrgs.cpd.ufrgsmapas.models.Building
import io.realm.Realm.setDefaultConfiguration
import java.util.*


class LocalStorageManager() {

    private lateinit var mContext: Context

    constructor(context: Context) : this() {
        mContext = context
    }

    fun getAllBuildings(): ArrayList<Building> {
        val list = ArrayList<Building>()
        var realm: Realm? = null

        try {
            realm = getRealm()
            val results = realm
                    .where(Building::class.java)
                    .findAll()
            list.addAll(realm.copyFromRealm(results))
        } finally {
            if (realm != null) {
                realm.close()
            }
        }

        return list
    }

    fun getBuildingsByNameOrNumber(query: String): ArrayList<Building> {

        if (query == "") {
            return getAllBuildings()
        }

        val q = query.toLowerCase()

        val filtered = getAllBuildings().filter {
            it.nomePredio.toLowerCase().contains(q) || it.codPredioUfrgs.toString().contains(q)
        }

        return ArrayList(filtered)
    }

    fun isEmpty(): Boolean {
        return getAllBuildings().isEmpty()
    }

    fun save(buildings: ArrayList<Building>) {
        val realm = getRealm()

        realm.beginTransaction()
        realm.deleteAll()
        realm.commitTransaction()

        for (b in buildings) {
            save(b)
        }
        realm.close()
    }

    private fun save(b: Building) {
        val realm = getRealm()
        realm.beginTransaction()
        var building = realm.createObject(Building::class.java)
        building.codPredio = b.codPredio
        building.nomePredio = b.nomePredio
        building.codPredioUfrgs = b.codPredioUfrgs
        building.campus = b.campus
        building.denominacaoCampus = b.denominacaoCampus
        building.latitude = b.latitude
        building.longitude = b.longitude
        building.logadouro = b.logadouro
        building.nrLoogadouro = b.nrLoogadouro
        building.cidade = b.cidade
        building.cep = b.cep
        building.telefone = b.telefone
        building.email = b.email
        building.isUnique = b.isUnique
        realm.commitTransaction()
    }

    private fun getRealm(): Realm {
        val realmConfig = RealmConfiguration.Builder(mContext).deleteRealmIfMigrationNeeded().build()
        setDefaultConfiguration(realmConfig)

        return Realm.getDefaultInstance()
    }

}