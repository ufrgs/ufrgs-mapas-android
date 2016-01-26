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

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.SearchView;

import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.ufrgs.ufrgsmapas.R;
import br.ufrgs.ufrgsmapas.activities.MainActivity;
import br.ufrgs.ufrgsmapas.libs.SearchBox;
import br.ufrgs.ufrgsmapas.utils.DebugUtils;

/**
 * Handles the SearchBox
 * Created by Alan Wink.
 */
public class SearchBoxView implements MainActivity.ActivityResult{

    private static final String TAG = SearchView.class.getSimpleName();

    private Map<String, String> mSearchResultsMap;

    private SearchBox mSearchBox;

    private MainView mView;

    private List<OnMenuClick> mMenuClickList;
    private List<SearchStates> mSearchStatesList;
    private MainActivity mActivity;
    private boolean mIsOpen = false;

    public SearchBoxView(MainActivity activity, MainView mainView) {

        mActivity = activity;
        mSearchBox = (SearchBox) mActivity.findViewById(R.id.searchbox);

        mView = mainView;
        mMenuClickList = new ArrayList<>();
        mSearchStatesList = new ArrayList<>();

        configureSearchBox();
    }

    private void configureSearchBox(){

        mSearchBox.setLogoText("Buscar no UFRGS Mapas");
        mSearchBox.enableVoiceRecognition(mActivity);
        mActivity.registerActivityResult(this);

        mSearchBox.setMenuListener(new SearchBox.MenuListener() {

            @Override
            public void onMenuClick() {
                //Hamburger has been clicked
                for(OnMenuClick c : mMenuClickList){
                    c.onSearchBoxMenuClick();
                }
            }

        });

        mSearchBox.setSearchListener(new SearchBox.SearchListener() {

            @Override
            public void onSearchOpened() {
                mIsOpen = true;
                //Use this to tint the screen
                if (DebugUtils.DEBUG) Log.d(TAG, "onSearchOpened");
                for (SearchStates s : mSearchStatesList) {
                    s.onSearchBoxOpened();
                }

            }

            @Override
            public void onSearchClosed() {
                mIsOpen = false;
                //Use this to un-tint the screen
                if (DebugUtils.DEBUG) Log.d(TAG, "onSearchClosed");

                for (SearchStates s : mSearchStatesList) {
                    s.onSearchBoxClosed();
                }
            }

            @Override
            public void onSearchTermChanged() {
                //React to the search term changing
                //Called after it has updated results
                if (DebugUtils.DEBUG) Log.d(TAG, "onSearchTermChanged");

                // Clear everything
                mSearchBox.clearSearchable();

                // Get search resutls
                mSearchResultsMap = mView.searchLocationNameId(mSearchBox.getSearchText(),
                        mSearchBox.mResultSizeLimit);

                for (Map.Entry<String, String> entry : mSearchResultsMap.entrySet()) {
                    if (mSearchBox.getSearchables().toString().contains(entry.getValue())) {

                    } else {
                        mSearchBox.addSearchable(new SearchResult(entry.getKey(), null));
                    }

                }
                mSearchBox.updateResults();

            }

            @Override
            public void onSearch(String searchTerm) {
                String buildingId = mSearchResultsMap.get(searchTerm);

                if (DebugUtils.DEBUG) Log.d(TAG, "onSearch " + searchTerm + " : " + buildingId);

                if (buildingId != null) { //TODO Call mainView to open the search result
                    mView.newSearchResult(buildingId);
                }
            }

            @Override
            public void onSearchCleared() {
                if (DebugUtils.DEBUG) Log.d(TAG, "onSearchCleared");
                for (SearchStates s : mSearchStatesList) {
                    s.onSearchBoxCleared();
                }
            }

        });
    }

    public void registerOnMenuClick(OnMenuClick c){
        mMenuClickList.add(c);
    }

    public void unregisterOnMenuClick(OnMenuClick c){
        mMenuClickList.remove(c);
    }

    public void registerSearchStates(SearchStates s){
        mSearchStatesList.add(s);
    }

    public void unregisterSearchStates(SearchStates s){
        mSearchStatesList.remove(s);
    }

    /**
     * Set the a margin related to the original search bar position
     */
    public void setTopMargin(int px){
        // I know, it seems weird to animate the bar to its position, but trying to set the margin
        // in runtime is so much painful.
        mSearchBox.animate().translationY(px).setDuration(0).start();
    }

    /**
     * Get the search bar height, in pixels
     * @return
     */
    public int getHeight(){
        int height = mSearchBox.getBarHeightPx();

        if(DebugUtils.DEBUG) Log.d(TAG, "Searchbox height: " + height);
        return height;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (/*isAdded() &&*/ requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == mActivity.RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ArrayList<String> firstResult = new ArrayList<>();
            firstResult.add(matches.get(0));
            mSearchBox.populateEditText(firstResult);
            openSearch();
        }
    }

    public boolean isOpen() {
        return mIsOpen;
    }

    public void openSearch(){
        if(!mIsOpen) {
            mSearchBox.toggleSearch();
        }
    }

    public void closeSearch() {
        if(mIsOpen) {
            mSearchBox.toggleSearch();
        }
    }

    public interface OnMenuClick{
        void onSearchBoxMenuClick();
    }

    public interface SearchStates{
        void onSearchBoxOpened();
        void onSearchBoxClosed();
        void onSearchBoxCleared();
    }

}
