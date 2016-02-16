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

import android.app.FragmentManager;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.model.LatLng;

import java.util.Map;

import br.ufrgs.ufrgsmapas.activities.MainActivity;
import br.ufrgs.ufrgsmapas.utils.DebugUtils;
import br.ufrgs.ufrgsmapas.utils.MeasureUtils;
import br.ufrgs.ufrgsmapas.utils.TrackerUtils;
import br.ufrgs.ufrgsmapas.vos.BuildingVo;
import br.ufrgs.ufrgsmapas.vos.MapPositionVo;

/**
 * Entry point of the Views
 * Created by alan on 18/09/15.
 */
public class MainView extends RelativeLayout implements SearchBoxView.OnMenuClick, SearchBoxView.SearchStates, SidebarView.SidebarViewEvents, BottomSheetView.BottomSheetButtonsClick, MainActivity.BackKeyPressed, MapView.OnMapClick {

    private static final String TAG = MainView.class.getSimpleName();

    private static final int MIN_SDK_TRANSPARENT_UI = 19;
    private static final int EXTRA_PADDING_TOP_DP = 8;

    private MainActivity mActivity;

    private BottomSheetView mBottomSheetView;
    private MapView mMapView;
    private SearchBoxView mSearchBoxView;
    private SidebarView mSideBarView;

    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void configureView(MainActivity mainActivity){
        this.mActivity = mainActivity;

        mSearchBoxView = new SearchBoxView(mActivity, this);
        mSearchBoxView.setTopMargin(MeasureUtils.getStatusBarHeightApiLevel(mActivity,
                MIN_SDK_TRANSPARENT_UI));

        mSideBarView = new SidebarView(mActivity);

        mMapView = new MapView(mActivity, this);
        setMapPadding();

        mBottomSheetView = new BottomSheetView(mActivity);
        // No more trasparent Navigation Bar
        //mBottomSheetView.setBottomMarginPx(
        //        MeasureUtils.getNavigationBarHeightApiLevel(mActivity, MIN_SDK_TRANSPARENT_UI));

        setListeners();

        drawAllBuildings();
    }

    private void setMapPadding() {
        int topPaddingPx = MeasureUtils.getStatusBarHeightApiLevel(mActivity, MIN_SDK_TRANSPARENT_UI)
                + mSearchBoxView.getHeight();
        if(DebugUtils.DEBUG) Log.d(TAG, "Top padding: "+topPaddingPx);
        // No more transparent Navigation Bar
        //int bottomPaddingPx = MeasureUtils.getNavigationBarHeightApiLevel(mActivity, MIN_SDK_TRANSPARENT_UI);

        mMapView.setDefaultTopPaddingDp(MeasureUtils.convertPxToDp(topPaddingPx, mActivity) + EXTRA_PADDING_TOP_DP);
        //mMapView.setDefaultBottomPaddingDp(MeasureUtils.convertPxToDp(bottomPaddingPx, mActivity));
    }

    private void setListeners() {
        // Searchbox menu button and states
        mSearchBoxView.registerOnMenuClick(this);
        mSearchBoxView.registerSearchStates(this);

        // Menu map modifiers (map, satellite)
        mSideBarView.registerSidebarViewEvents(this);

        // Bottom sheet buttons
        mBottomSheetView.registerBottomSheetButtonsClick(this);

        // Map click
        mMapView.registerOnMapClickList(this);

        // Back button
        mActivity.registerBackKeyPressed(this);
    }

    private void drawAllBuildings(){
        mMapView.drawBuildings(mActivity.getAllBuildings());
    }

    private void drawFavoriteBuildings() {
        mMapView.drawBuildings(mActivity.getFavoriteBuildings());
    }

    public FragmentManager getActivityFragmentManager(){
        return mActivity.getFragmentManager();
    }

    /**
     * Get the search results from model.
     * @param searchText Search query
     * @param mResultSizeLimit Maximum of results to be shown
     * @return A Map with {Name, Id} of a location.
     */
    public Map<String, String> searchLocationNameId(String searchText, int mResultSizeLimit) {
        return mActivity.searchLocationNameId(searchText, mResultSizeLimit);
    }

    /**
     * Called to make the view show a building found by user.
     * @param buildingId Id of the building to be shown.
     */
    public void newSearchResult(String buildingId) {
        //selectItem(buildingId, true);
        selectItem(buildingId, false);
    }

    /**
     * When the user clicks the hamburger menu button on the search bar.
     */
    @Override
    public void onSearchBoxMenuClick() {
        mSideBarView.openMenu();
    }

    /**
     * When user wants all buildings
     */
    @Override
    public void sidebarClickAllBuildings() {
        drawAllBuildings();
    }

    /**
     * When user clicks on favorite buildings option
     */
    @Override
    public void sidebarClickFavorites() {
        drawFavoriteBuildings();
    }

    /**
     * Select normal map
     */
    @Override
    public void sidebarClickNormalMap() {
        mMapView.setNormalMap();
    }

    /**
     * Select satellite map
     */
    @Override
    public void sidebarClickSatelliteMap() {
        mMapView.setSatelliteMap();
    }

    /**
     * Open the building with a MapPositionVo
     * @param mapPositionVo Building to be opened
     */
    public void selectItem(MapPositionVo mapPositionVo) {
        BuildingVo building = mActivity.getBuilding(mapPositionVo.id);
        selectBuilding(building, false);
    }

    /**
     * Open the building with the building id in string format
     * @param buildingCode Building (id) to be opened
     */
    private void selectItem(String buildingCode, boolean showExtraInfo) {
        int id = Integer.valueOf(buildingCode);
        BuildingVo building = mActivity.getBuilding(id);
        selectBuilding(building, showExtraInfo);
    }

    /**
     * Open a building. Changes the UI elements to show information about the building.
     * @param building Building info
     * @param showExtraInfo If the extra info about the building must be show directly
     */
    public void selectBuilding(BuildingVo building, boolean showExtraInfo){
        if(DebugUtils.DEBUG) Log.d(TAG, "selectBuilding("+ building.ufrgsBuildingCode +")");

        // Mark building as selected
        mMapView.selectBuilding(building);
        // Open bottom sheet
        mBottomSheetView.open(building, showExtraInfo, new BottomSheetView.BottomSheetActions() {
            @Override
            public void onBottomSheetOpened() {
            }

            @Override
            public void onBottomSheetClosed() {
                // If user closes the bottom sheet, unselect it.
                unselectBuilding(true);
            }
        });

    }

    @Override
    public void OnBottomSheetLocationButtonClick(BuildingVo buildingVo) {
        mActivity.openDirections(buildingVo);
    }

    /**
     * Set a building as favorite, based on user button click
     * @param buildingVo Building to set star
     */
    @Override
    public void OnBottomSheetStarButtonClick(BuildingVo buildingVo) {
        if(DebugUtils.DEBUG) Log.d(TAG, "MainView received star click of " + buildingVo.id);
        if(buildingVo.isStarred) {
            mActivity.setStarred(buildingVo, false);
            mBottomSheetView.unsetStarButton();
        }
        else{
            mActivity.setStarred(buildingVo, true);
            mBottomSheetView.setStarButton();
        }
        mMapView.updateSelectedBuilding(buildingVo);
    }

    /**
     * Closes views (in a order)
     */
    @Override
    public boolean onBackKeyPressed() {
        // Check searchbox
        if(mSearchBoxView.isOpen()){
            mSearchBoxView.closeSearch();
            return true;
        }

        // Check sidebar: Menu is open
        if(mSideBarView.isSideMenuOpen()){
            mSideBarView.closeMenu();
            return true;
        }

        // Check bottomsheet: A building is selected
        if(mBottomSheetView.isOpen()){
            unselectBuilding(false);
            return true;
        }

        return false;
    }

    private void unselectBuilding(boolean immediately){
        if(immediately){
            mBottomSheetView.closeImmediately();
        }
        else{
            mBottomSheetView.close();
        }
        mMapView.unselectBuilding();
        mMapView.resetPadding();
    }

    /**
     * User clicked on the search: close building
     */
    @Override
    public void onSearchBoxOpened() {
        unselectBuilding(false);
    }

    @Override
    public void onSearchBoxClosed() {

    }

    @Override
    public void onSearchBoxCleared() {

    }

    /**
     * When user clicks on map
     * @param pos
     */
    @Override
    public void onMapClick(LatLng pos) {
        onBackKeyPressed();
    }

    /**
     * Changes the interface based on proximity of a campus.
     * @param closestCampus a LocationModel code with the closest campus.
     */
    public void updateInterfaceLocation(int closestCampus) {
        mSideBarView.updateInterface(closestCampus);
    }
}
