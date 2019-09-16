package br.ufrgs.cpd.ufrgsmapas.models

import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * Created by Theo on 31/07/17.
 */
@Parcelize
open class Building(
        var codPredio : Int = 0,
        var nomePredio : String = "",
        var codPredioUfrgs : Int = 0,
        var campus : Int = 0,
        var denominacaoCampus : String = "",
        var latitude : Double = 0.0,
        var longitude : Double = 0.0,
        var logadouro : String = "",
        var nrLoogadouro : Int = 0,
        var cidade : String = "",
        var cep : String = "",
        var telefone : String = "",
        var email : String = "",
        var isUnique : Boolean = false
    ): RealmObject(), Parcelable {

}