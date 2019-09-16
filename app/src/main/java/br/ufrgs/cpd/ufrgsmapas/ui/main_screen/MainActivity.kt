package br.ufrgs.cpd.ufrgsmapas.ui.main_screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import br.ufrgs.cpd.ufrgsmapas.adapters.MainSearchAdapter
import br.ufrgs.cpd.ufrgsmapas.custom_views.CustomInfoWindow
import br.ufrgs.cpd.ufrgsmapas.db.DatabaseAdapter
import br.ufrgs.cpd.ufrgsmapas.models.Building
import br.ufrgs.cpd.ufrgsmapas.ui.building_detail_screen.BuildingDetailActivity
import br.ufrgs.cpd.ufrgsmapas.utils.LayoutUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.lapism.searchview.SearchView
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import android.graphics.Color
import android.speech.RecognizerIntent
import android.support.v7.app.AlertDialog
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.TextView
import br.ufrgs.cpd.ufrgsmapas.BuildConfig
import br.ufrgs.cpd.ufrgsmapas.R
import br.ufrgs.cpd.ufrgsmapas.models.Pin
import br.ufrgs.cpd.ufrgsmapas.utils.BuildingsFetcher
import com.lapism.searchview.SearchView.SPEECH_REQUEST_CODE
import kotlinx.android.synthetic.main.abc_search_view.view.*
import kotlinx.android.synthetic.main.fragment_intro_fragment3.view.*
import kotlinx.android.synthetic.main.item_search.view.*
import kotlinx.android.synthetic.main.search_view.view.*


class MainActivity : AppCompatActivity(), MainScreenContract.View, SearchView.OnQueryTextListener, GoogleMap.OnMarkerClickListener, SearchView.OnOpenCloseListener {

    lateinit var mPresenter : MainScreenPresenter
    lateinit var mAllBuildings : List<Building>
    lateinit var mGoogleMap : GoogleMap
    lateinit var fragment_map : SupportMapFragment
    lateinit var mDataBaseAdapter: DatabaseAdapter

    var shouldShowAllPins = true

    lateinit var mPinsList : ArrayList<Pin>

    private val mAdapter = MainSearchAdapter(::searchResultItemSelected)
    private val rsCenter = LatLng(-29.585033, -52.442913)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(br.ufrgs.cpd.ufrgsmapas.R.layout.activity_main)
        LayoutUtils.setStatusbarTranslucent(this)

        //lateinits
        mDataBaseAdapter = DatabaseAdapter(this)
        mPresenter = MainScreenPresenter(this, this, mDataBaseAdapter)
        mAllBuildings = mDataBaseAdapter.getAllBuildings()
        fragment_map = (supportFragmentManager.findFragmentById(R.id.map)) as SupportMapFragment

        // configure search view
        searchView.setArrowOnly(true)
        searchView.setOnQueryTextListener(this)
        searchView.setOnOpenCloseListener(this)
        searchView.adapter = mAdapter
        searchView.imageView_arrow_back.setOnClickListener {
            closeSearchView()
        }

        // do stuff that need to be done only on first run
        checkFirstRun()

        // handle user location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 666)
        } else {
            val fusedLocation = LocationServices.getFusedLocationProviderClient(this)
            fusedLocation.lastLocation.addOnSuccessListener(this) { location ->
                if(location == null){
                    setupGoogleMapNoLocation(fragment_map)
                } else {
                    setupGoogleMapWithLocation(fragment_map, location)
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val query = data?.getStringExtra(SearchManager.QUERY)

        if (query != null) {
            searchView.setQuery(query, true)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupGoogleMapWithLocation(fragment_map: SupportMapFragment, location: Location) {
        val supportMap = fragment_map.getMapAsync { googleMap ->
            this.mGoogleMap = googleMap
            this.mGoogleMap.isMyLocationEnabled = true
            this.mGoogleMap.uiSettings.isMyLocationButtonEnabled = false
            this.mGoogleMap.setInfoWindowAdapter(CustomInfoWindow(this))
            this.mGoogleMap.setOnMarkerClickListener(this)
            mPresenter.getAllPins()
            moveTo(location, 5.5f)
        }
    }

    private fun setupGoogleMapNoLocation(fragment_map: SupportMapFragment) {
        val supportMap = fragment_map.getMapAsync { googleMap ->
            this.mGoogleMap = googleMap
            this.mGoogleMap.uiSettings.isMyLocationButtonEnabled = false
            this.mGoogleMap.setInfoWindowAdapter(CustomInfoWindow(this))
            this.mGoogleMap.setOnMarkerClickListener(this)
            mPresenter.getAllPins()

            // centraliza o mapa no RS
            val location = Location("")
            location.latitude = rsCenter.latitude
            location.longitude = rsCenter.longitude
            moveTo(location, 5.5f)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == 666 && grantResults.isNotEmpty()) {
            setupGoogleMapNoLocation(fragment_map)
        } else {
            val fusedLocation = LocationServices.getFusedLocationProviderClient(this)
            fusedLocation.lastLocation.addOnSuccessListener(this) { location ->
                setupGoogleMapWithLocation(fragment_map, location)
            }
        }
    }

    override fun onClose(): Boolean {
        closeSearchView()
        return false
    }

    override fun onOpen(): Boolean {
        shouldShowAllPins = true
        return false
    }

    private fun closeSearchView() {
        if (::mGoogleMap.isInitialized) {
            if (shouldShowAllPins && mPinsList.size == 1) {
                mPresenter.getAllPins()
                searchView.setQuery("", false)
            }
        }
        shouldShowAllPins = true
    }

    private fun moveTo(location: Location, zoom: Float) {
        val cam = CameraPosition.Builder().target(LatLng(location.latitude, location.longitude)).zoom(zoom).build()
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cam))
    }

    override fun searchResult(list: List<Building>) {
        mAdapter.setData(list)
    }

    override fun showPins(list: ArrayList<Pin>) {
        mPinsList = list

        if (list.size == 0) {
            alertFailedToFetchBuildings()
            return
        }

        mGoogleMap.clear()

        list.map {
            var pin : Int

            if(it.buildings.size == 1)
                pin = R.drawable.pin_predios
            else
                pin = R.drawable.pin_varios_predios

            MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(pin))
                    .position(LatLng(it.latitude, it.longitude))
                    .snippet(it.id.toString())
        }.forEach {
            mGoogleMap.addMarker(it)
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let { mPresenter.serchForBuilding(it) }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let { mPresenter.serchForBuilding(it) }
        return true
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        val id = marker?.snippet?.toInt()

        val cam = CameraPosition.Builder().target(marker?.position).zoom(17f).build()
        this.mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cam), 500, object : CancelableCallback {

            override fun onFinish() {

                val pin = getPinById(id!!)

                if (pin?.buildings?.size == 1) {
                    showBuilding(pin!!.buildings[0])
                } else {
                    askWhichBuildingToShow(pin!!)
                }
            }

            override fun onCancel() {
                Log.d("MainActivity", "Cam animation cancell")}
        })

        return true
    }

    fun getPinById(id : Int) : Pin? {
        mPinsList.forEach {
            if(it.id == id)
                return it
        }

        return null
    }

    private fun checkFirstRun() {

        val preferences = applicationContext.getSharedPreferences(BuildConfig.APPLICATION_ID, 0)

        if (preferences.getBoolean(resources.getString(R.string.privacy_policy_tag), true)) {
            showPrivacyPolicy()

            val editor = preferences.edit()
            editor.putBoolean(resources.getString(R.string.privacy_policy_tag), false)
            editor.commit()
        }

    }

    private fun showPrivacyPolicy() {
        val textView = layoutInflater.inflate(R.layout.privacy_policy_textview, null) as TextView
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.setText(R.string.privacy_policy_message)
        textView.setLinkTextColor(Color.parseColor("#0000FF"))

        val dialog = AlertDialog.Builder(this)
                .setPositiveButton(R.string.privacy_policy_response, null)
                .setCancelable(false)
                .setView(textView)
                .create()

        dialog.show()
    }

    private fun askWhichBuildingToShow(pin: Pin) {

        val buildings = pin.buildings
        val names: Array<String> = buildings.map {
            it.nomePredio
        }.toTypedArray()

        val dialog = AlertDialog.Builder(this)
                .setTitle("Escolha o prédio")
                .setItems(names) {dialog, which ->
                    val selected = pin.buildings[which]
                    showBuilding(selected)
                }

        dialog.show()
    }

    private fun alertFailedToFetchBuildings() {
        val dialog = AlertDialog.Builder(this)
                .setTitle("Erro")
                .setMessage("Não foi possível obter as localizações dos prédios. Cheque sua conexão com a internet.")
                .setPositiveButton("Ok", null)

        dialog.show()
    }

    private fun showBuilding(building: Building) {
        startActivity<BuildingDetailActivity>("the_building" to building)
        overridePendingTransition(R.anim.faster_fade_in, R.anim.faster_fade_out)
    }

    fun searchResultItemSelected(building: Building) {
        shouldShowAllPins = false

        val buildings = arrayListOf<Building>()
        buildings.add(building)

        val pin = Pin(building.latitude, building.longitude, buildings, 0)
        val pins = arrayListOf<Pin>()
        pins.add(pin)

        showPins(pins)

        val l = Location("")
        l.latitude = building.latitude
        l.longitude = building.longitude

        moveTo(l, 16f)
        searchView.clearFocus()
    }

}
