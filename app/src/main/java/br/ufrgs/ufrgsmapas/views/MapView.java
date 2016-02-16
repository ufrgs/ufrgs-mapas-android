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
package br.ufrgs.ufrgsmapas.views;

import android.location.Location;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import br.ufrgs.ufrgsmapas.R;
import br.ufrgs.ufrgsmapas.activities.MainActivity;
import br.ufrgs.ufrgsmapas.libs.NonHierarchicalDistanceBasedAlgorithm;
import br.ufrgs.ufrgsmapas.utils.DebugUtils;
import br.ufrgs.ufrgsmapas.utils.MeasureUtils;
import br.ufrgs.ufrgsmapas.utils.TrackerUtils;
import br.ufrgs.ufrgsmapas.vos.BuildingVo;
import br.ufrgs.ufrgsmapas.vos.MapPositionVo;


/**
 * Handles map operations. Responsible for populate the map and handle user interactions.
 * Created by alan on 30/07/15.
 */
public class MapView implements ClusterManager.OnClusterItemClickListener<MapPositionVo>, ClusterManager.OnClusterClickListener<MapPositionVo> {

    private static final String TAG = MapView.class.getSimpleName();

    private int mDefaultPaddingLeftDp = 0;
    private int mDefaultPaddingTopDp = 0;
    private int mDefaultPaddingRightDp = 0;
    private int mDefaultPaddingBottomDp = 0;

    private int mPaddingLeftDp = mDefaultPaddingLeftDp;
    private int mPaddingTopDp = mDefaultPaddingTopDp;
    private int mPaddingRightDp = mDefaultPaddingRightDp;
    private int mPaddingBottomDp = mDefaultPaddingBottomDp;

    private static final LatLng mDefaultStartPosition = new LatLng(-30.033775, -51.219224);
    private static final int DEFAULT_ZOOM = 17;

    private ClusterManager<MapPositionVo> mClusterManager;
    private GoogleMap mGoogleMap;

    private MainActivity mActivity;
    private MainView mView;

    private List<MapPositionVo> mMapPositionVoList;
    private BuildingVo mSelectedBuildingVo;

    private List<OnMapClick> mOnMapClickList;

    public MapView(MainActivity activity, MainView view) {
        this.mActivity = activity;
        mView = view;
        mMapPositionVoList = new ArrayList<>();

        mOnMapClickList = new ArrayList<>();

        setUpMapIfNeeded();
        configureMapUi();
        configureClusterManager();

        flyToLocation();
    }

    public void registerOnMapClickList(OnMapClick o){
        mOnMapClickList.add(o);
    }

    public void unregisterOnMapClickList(OnMapClick o){
        mOnMapClickList.remove(o);
    }

    /**
     * Draw a list of buildings into the map.
     * @param buildingList List of MapPositionVo to be drawn.
     */
    public void drawBuildings(SparseArray<MapPositionVo> buildingList) {
        if(DebugUtils.DEBUG) Log.d(TAG, "drawing a list of buildings with size: " + buildingList.size());

        mClusterManager.clearItems();
        mMapPositionVoList.clear();

        for(int i = 0; i < buildingList.size(); i++){
            mClusterManager.addItem(buildingList.valueAt(i));
            mMapPositionVoList.add(buildingList.valueAt(i));
        }
        mClusterManager.cluster();
    }

    private void configureClusterManager() {
        mClusterManager = new ClusterManager<>(mActivity, mGoogleMap);
        mClusterManager.setRenderer(new BuildingClusterRenderer(mActivity, mGoogleMap, mClusterManager));
        mClusterManager.setAlgorithm(new PreCachingAlgorithmDecorator<>(new NonHierarchicalDistanceBasedAlgorithm<MapPositionVo>()));

        mGoogleMap.setOnCameraChangeListener(mClusterManager);
        mGoogleMap.setOnMarkerClickListener(mClusterManager);
        mGoogleMap.setOnInfoWindowClickListener(mClusterManager);

        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterClickListener(this);
    }

    /**
     * Configurations related to map display
     */
    private void configureMapUi() {
        setMapPaddingDp(mDefaultPaddingLeftDp, mDefaultPaddingTopDp, mDefaultPaddingRightDp, mDefaultPaddingBottomDp);
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                for (OnMapClick o : mOnMapClickList) {
                    o.onMapClick(latLng);
                }
            }
        });
    }

    /**
     * Set map padding in DP
     * @param leftPaddingDp Left Padding
     * @param topPaddingDp Top Padding
     * @param rightPaddingDp Right side Padding
     * @param bottomPaddingDp Bottom side Padding
     */
    private void setMapPaddingDp(int leftPaddingDp, int topPaddingDp, int rightPaddingDp, int bottomPaddingDp) {
        setUpMapIfNeeded();
        mGoogleMap.setPadding(
                MeasureUtils.convertDpToPixel(leftPaddingDp, mActivity),
                MeasureUtils.convertDpToPixel(topPaddingDp, mActivity),
                MeasureUtils.convertDpToPixel(rightPaddingDp, mActivity),
                MeasureUtils.convertDpToPixel(bottomPaddingDp, mActivity)
        );
    }

    /**
     * Updates the map with default padding
     */
    public void resetPadding(){
        mPaddingLeftDp = mDefaultPaddingLeftDp;
        mPaddingTopDp = mDefaultPaddingTopDp;
        mPaddingRightDp = mDefaultPaddingRightDp;
        mPaddingBottomDp = mDefaultPaddingBottomDp;
        updatePadding();
    }

    /**
     * Updates map padding with the current values
     */
    private void updatePadding() {
        setMapPaddingDp(mPaddingLeftDp, mPaddingTopDp, mPaddingRightDp, mPaddingBottomDp);
    }

    /**
     * Get map reference and start it if needed.
     */
    private void setUpMapIfNeeded() {
        if (mGoogleMap != null) {
            return;
        }

        mGoogleMap = ((MapFragment) mView.getActivityFragmentManager()
                .findFragmentById(R.id.map))
                .getMap();
    }

    /**
     * Move camera to default position and register to move to user location.
     */
    private void flyToLocation(){
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultStartPosition, DEFAULT_ZOOM));
        mActivity.registerValidUserLocation(new MainActivity.ValidUserLocation() {
            @Override
            public void onValidUserLocation(Location l) {
                // Move camera to user location and unregister.
                LatLng userPos = new LatLng(l.getLatitude(), l.getLongitude());
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(userPos));
                mActivity.unregisterValidUserLocation(this);
            }
        });
    }


    /**
     * Set map to normal
     */
    public void setNormalMap() {
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    /**
     * Set map to satellite (using hybrid)
     */
    public void setSatelliteMap() {
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    /**
     * When the user clicks on a marker
     * @param mapPositionVo Item that was clicked
     * @return If the click was handled (in this case, always true)
     */
    @Override
    public boolean onClusterItemClick(MapPositionVo mapPositionVo) {
        mView.selectItem(mapPositionVo);
        TrackerUtils.pinClick(mapPositionVo.id);
        return true;
    }

    /**
     * Mark building as selected
     * @param building Building to be selected
     */
    public void selectBuilding(BuildingVo building) {
        // Unselect last building
        unselectBuilding();

        // Mark building as selected
        MapPositionVo mapPosVo = findMapPositionVo(building);
        mapPosVo.selected = true;
        mSelectedBuildingVo = building;

        // Redraw
        reloadMarker(mapPosVo);

        // Move camera
        animateCamera(mapPosVo);
    }

    /**
     * Removes the selection on the last building
     */
    public void unselectBuilding(){
        if(mSelectedBuildingVo != null) {
            MapPositionVo selected = findMapPositionVo(mSelectedBuildingVo);
            if (selected != null) {
                selected.selected = false;
                reloadMarker(selected);
                mSelectedBuildingVo = null;
            }
        }
    }

    /**
     * Move camera to a building
     * @param mapPosVo Target building
     */
    private void animateCamera(MapPositionVo mapPosVo) {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mapPosVo.getPosition(), DEFAULT_ZOOM));
    }

    /**
     * Find in the internal list the MapPositionVo related to the BuildingVo
     * @param building Building object
     * @return MapPositionVo object
     */
    private MapPositionVo findMapPositionVo(BuildingVo building) {
        for(MapPositionVo mapPos: mMapPositionVoList){
            if(mapPos.id == building.id) return mapPos;
        }
        return null;
    }

    /**
     * Workaround to update markers.
     */
    private void reloadMarker(MapPositionVo mapPositionVo){
        if(DebugUtils.DEBUG) Log.d(TAG, "reload: " + mapPositionVo.id + "\nselected: "+mapPositionVo.selected+"\nstarred: "+mapPositionVo.starred);
        MarkerManager.Collection markerCollection = mClusterManager.getMarkerCollection();
        Collection<Marker> markers = markerCollection.getMarkers();
        String strId = Integer.toString(mapPositionVo.id);
        for (Marker m : markers) {
            if (strId.equals(m.getTitle())) {
                m.setIcon(BuildingClusterRenderer.getBuildingIcon(mActivity, mapPositionVo));
                break;
            }
        }

    }

    /**
     * Update the marker related to the selected building.
     * @param buildingVo
     */
    public void updateSelectedBuilding(BuildingVo buildingVo) {
        if(mSelectedBuildingVo != null && mSelectedBuildingVo.id == buildingVo.id) {
            if(DebugUtils.DEBUG) Log.d(TAG, "updateSelectedBuilding: " + mSelectedBuildingVo.id);
            MapPositionVo selected = findMapPositionVo(mSelectedBuildingVo);

            // We need to change the MapPositionVo parameters with the new buildingVo parameters
            updateMapPositionVo(selected, buildingVo);

            reloadMarker(selected);
        }
    }

    /**
     * Updates a MapPositionVo with the BuildingVo information
     * @param mapPositionVo MapPositionVo to be changed
     * @param buildingVo BuildingVo with the new information
     */
    private void updateMapPositionVo(MapPositionVo mapPositionVo, BuildingVo buildingVo) {
        mapPositionVo.starred = buildingVo.isStarred;
    }

    /**
     * Sets the top padding of the map, in dp
     * @param dp Size in dp
     */
    public void setDefaultTopPaddingDp(int dp) {
        mDefaultPaddingTopDp = dp;
        resetPadding();
    }

    /**
     * Sets the top padding of the map, in dp
     * @param dp Size in dp
     */
    public void setDefaultBottomPaddingDp(int dp) {
        mDefaultPaddingBottomDp = dp;
        resetPadding();
    }

    /**
     * Adds an extra amount of bottom padding to the default padding
     * @param px Value in px
     */
    public void setExtraBottomPadding(int px) {
        mPaddingBottomDp = mDefaultPaddingBottomDp;
        if(DebugUtils.DEBUG) Log.d(TAG, "New bottom padding: \nFrom: " + mPaddingBottomDp);
        mPaddingBottomDp += MeasureUtils.convertPxToDp(px, mActivity);
        if(DebugUtils.DEBUG) Log.d(TAG, " To: " + mPaddingBottomDp);
        updatePadding();
    }

    /**
     * Click on cluster to zoom
     * @param cluster
     * @return
     */
    @Override
    public boolean onClusterClick(Cluster<MapPositionVo> cluster) {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), DEFAULT_ZOOM));
        return true;
    }

    public interface OnMapClick{
        void onMapClick(LatLng pos);
    }

}
