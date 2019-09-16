package br.ufrgs.cpd.ufrgsmapas.ui.building_detail_screen

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import br.ufrgs.cpd.ufrgsmapas.R
import br.ufrgs.cpd.ufrgsmapas.utils.LayoutUtils
import kotlinx.android.synthetic.main.activity_buiding_detail.*
import android.content.Intent
import android.net.Uri
import br.ufrgs.cpd.ufrgsmapas.models.Building


/**
 * Created by Theo on 04/08/17.
 */
class BuildingDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buiding_detail)
        LayoutUtils.setStatusbarTranslucent(this)

        val building = intent.getParcelableExtra<Building>("the_building")

        nomePredio.text = building.nomePredio.trim()
        numeroPredio.text = "Prédio ${building.codPredioUfrgs}"
        enderecoPredio.text = building.logadouro + " " + building.nrLoogadouro.toString() + "\n" + building.cidade + " - " + building.cep
        predioCampus.text = building.denominacaoCampus.toUpperCase().trim()

        bg.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.faster_fade_in, R.anim.faster_fade_out);
        }

        floatingActionButton.setOnClickListener {
            val geoUri = "http://maps.google.com/maps?q=loc:${building.latitude},${building.longitude} (${building.nomePredio})"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
            startActivity(intent)
        }

        println(building.codPredioUfrgs.toString() + " - " + building.cep)

    }
}