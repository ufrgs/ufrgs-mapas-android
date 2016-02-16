/*
 * Copyright 2016 Universidade Federal do Rio Grande do Sul
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.ufrgs.ufrgsmapas.activities;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.ufrgs.ufrgsmapas.R;
import br.ufrgs.ufrgsmapas.models.BuildingModel;
import br.ufrgs.ufrgsmapas.models.LocationModel;
import br.ufrgs.ufrgsmapas.utils.DebugUtils;
import br.ufrgs.ufrgsmapas.utils.TrackerUtils;
import br.ufrgs.ufrgsmapas.views.MainView;
import br.ufrgs.ufrgsmapas.vos.BuildingVo;
import br.ufrgs.ufrgsmapas.vos.MapPositionVo;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

/**
 * App's main activity. Acts as a controller
 * @author Alan Wink
 */
public class MainActivity extends AppCompatActivity{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double BUILDING_PROXIMITY_RADIUS = 250; // Meters

    private List<ActivityLifecycleEvents> mActivityLifecycleEventsList;
    private List<BackKeyPressed> mBackKeyPressedList;
    private List<ValidUserLocation> mValidUserLocationList;
    private List<ActivityResult> mActivityResultList;

    private MainView mView;
    private BuildingModel mBuildingModel;
    private LocationModel mLocationModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mView = (MainView) View.inflate(this, R.layout.activity_main, null);
        setContentView(mView);

        mBuildingModel = new BuildingModel(this);
        mLocationModel = new LocationModel(mBuildingModel);

        // Create lists for events
        mActivityLifecycleEventsList = new ArrayList<>();
        mBackKeyPressedList = new ArrayList<>();
        mValidUserLocationList = new ArrayList<>();
        mActivityResultList = new ArrayList<>();

        mView.configureView(this);
        configureUserLocationListener();
    }

    @Override
    protected void onStart() {
        // Call all registered interfaces
        for(ActivityLifecycleEvents a : mActivityLifecycleEventsList){
            a.onActivityStart();
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        // Call all registered interfaces
        for(ActivityLifecycleEvents a : mActivityLifecycleEventsList){
            a.onActivityResume();
        }
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // Call all registered interfaces
        for(ActivityLifecycleEvents a : mActivityLifecycleEventsList){
            a.onActivityWindowFocusChanged(hasFocus);
        }
        super.onWindowFocusChanged(hasFocus);
    }


    @Override
    protected void onPause() {
        // Call all registered interfaces
        for(ActivityLifecycleEvents a : mActivityLifecycleEventsList){
            a.onActivityPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Call all registered interfaces
        for(ActivityLifecycleEvents a : mActivityLifecycleEventsList){
            a.onActivityDestroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Call all registered interfaces
        for(ActivityLifecycleEvents a : mActivityLifecycleEventsList){
            a.onActivitySaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        // Call all registered interfaces
        for(ActivityLifecycleEvents a : mActivityLifecycleEventsList){
            a.onActivityLowMemory();
        }
        super.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        boolean handled = false;

        // Call all registered interfaces
        for(BackKeyPressed b : mBackKeyPressedList){
            // Call onBackKeyPressed and check if it was handled.
            handled = handled | b.onBackKeyPressed();
        }

        if(DebugUtils.DEBUG) Log.d(TAG, "BackKeyPressed handled: " + handled);
        // Not handled, super
        if(!handled) {
            super.onBackPressed();
        }
    }

    public void registerActivityLifecycleEvents(ActivityLifecycleEvents a){
        mActivityLifecycleEventsList.add(a);
    }

    public void unregisterActivityLifecycleEvents(ActivityLifecycleEvents a){
        mActivityLifecycleEventsList.remove(a);
    }

    public void registerBackKeyPressed(BackKeyPressed b){
        mBackKeyPressedList.add(b);
    }

    public void unregisterBackKeyPressed(BackKeyPressed b){
        mBackKeyPressedList.remove(b);
    }

    public void registerValidUserLocation(ValidUserLocation v){
        mValidUserLocationList.add(v);
    }

    public void unregisterValidUserLocation(ValidUserLocation v){
        mValidUserLocationList.remove(v);
    }

    public void registerActivityResult(ActivityResult a){
        mActivityResultList.add(a);
    }

    public void unregisterActivityResult(ActivityResult a){
        mActivityResultList.remove(a);
    }

    private void configureUserLocationListener(){
        SmartLocation.with(this)
                .location()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {

                        updateClosestCampus(location);
                        
                        for (ValidUserLocation v : mValidUserLocationList) {
                            v.onValidUserLocation(location);
                        }
                    }
                });

    }

    public SparseArray<MapPositionVo> getAllBuildings() {
        return mBuildingModel.getFullBuildingPositions();
    }

    public SparseArray<MapPositionVo> getFavoriteBuildings() {
        return mBuildingModel.getStarredBuildingPositions();
    }

    public Map<String, String> searchLocationNameId(String searchText, int resultSizeLimit) {
        return mBuildingModel.searchBuildingName(searchText, resultSizeLimit);
    }

    public BuildingVo getBuilding(int id) {
        return mBuildingModel.getBuilding(id);
    }

    public void setStarred(BuildingVo buildingVo, boolean starred) {
        mBuildingModel.setStarred(buildingVo.id, starred);
        TrackerUtils.buildingStar(buildingVo.ufrgsBuildingCode, starred);
        buildingVo.isStarred = starred;
    }

    /**
     * Opens a external Map App to handle an intent
     * @param buildingVo Building with the coordinates to be handled
     */
    public void openDirections(BuildingVo buildingVo) {
        TrackerUtils.externalNavigation();
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr=" + buildingVo.latitude
                        + "," +buildingVo.longitude));
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        for(ActivityResult a: mActivityResultList){
            a.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Used to check the closest campus
     */
    public void updateClosestCampus(Location l) {
        int closestCampus = mLocationModel.closestBuilding(l.getLatitude(), l.getLongitude(), BUILDING_PROXIMITY_RADIUS);
        mView.updateInterfaceLocation(closestCampus);
    }

    public interface ActivityLifecycleEvents{
        void onActivityStart();
        void onActivityResume();
        void onActivityWindowFocusChanged(boolean hasFocus);
        void onActivityPause();
        void onActivityDestroy();
        void onActivitySaveInstanceState(Bundle outState);
        void onActivityLowMemory();
    }

    public interface BackKeyPressed{
        boolean onBackKeyPressed();
    }

    public interface ValidUserLocation{
        void onValidUserLocation(Location l);
    }

    public interface ActivityResult{
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

}
