package br.ufrgs.cpd.ufrgsmapas.utils

import android.content.Context
import android.os.AsyncTask
import br.ufrgs.cpd.ufrgsmapas.models.Building
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class BuildingsFetcher {

    val THE_URL = "https://www1.ufrgs.br/ws/siteufrgs/getpredios"
    var mContext: Context

    constructor(context: Context) {
        mContext = context
    }

    fun getBuildings(completion: (ArrayList<Building>?) -> Unit) {
        try {

            MyAsyncTask {
                if (NetworkUtils.isConnectedToNetwork(mContext)) {
                    val apiResponse = URL(THE_URL).readText()
                    val buildings = parseJson(apiResponse)
                    completion(buildings)
                } else {
                    completion(null)
                }

            }.execute()

        } catch (e: Exception) {
            completion(null)
        }

    }

    fun parseJson(data: String) : ArrayList<Building> {

        var buildings = arrayListOf<Building>()

        try {
            val array = JSONArray(data)

            for (i in 0..(array .length() - 1)) {

                val obj: JSONObject = array .getJSONObject(i)

                try {
                    val lat = obj.getDouble("Latitude")
                    val long = obj.getDouble("Longitude")

                    val b = Building(
                            obj.getInt("CodPredio"),
                            obj.getString("NomePredio"),
                            obj.getInt("CodPredioUFRGS"),
                            obj.getInt("Campus"),
                            obj.getString("DenominacaoCampus"),
                            lat, long,
                            obj.getString("Logradouro"),
                            obj.getInt("NrLogradouro"),
                            obj.getString("Cidade"),
                            obj.getString("CEP"),
                            obj.getString("Telefone"),
                            obj.getString("EMail"),
                            false
                    )

                    buildings.add(b)
                } catch (e: Exception) {}
            }

        } catch (e: Exception) {}

        return buildings

    }

}

class MyAsyncTask(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }
}
