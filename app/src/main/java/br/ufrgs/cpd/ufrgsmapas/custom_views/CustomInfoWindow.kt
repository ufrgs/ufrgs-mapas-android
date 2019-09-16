package br.ufrgs.cpd.ufrgsmapas.custom_views

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import android.widget.TextView
import br.ufrgs.cpd.ufrgsmapas.R


/**
 * Created by Theo on 04/08/17.
 */
class CustomInfoWindow(val context: Context) : GoogleMap.InfoWindowAdapter {

    private var myContentsView: View = (context as AppCompatActivity).layoutInflater.inflate(R.layout.info_window, null)


    override fun getInfoContents(marker: Marker): View {

        val tvTitle = myContentsView.findViewById(R.id.title) as TextView
        tvTitle.text = marker.title
        val tvSnippet = myContentsView.findViewById(R.id.snippet) as TextView
        tvSnippet.text = marker.snippet

        return myContentsView
    }

    override fun getInfoWindow(marker: Marker): View? {
        // TODO Auto-generated method stub
        return null
    }




}