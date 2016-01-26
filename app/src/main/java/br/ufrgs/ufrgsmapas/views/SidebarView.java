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

import android.app.Activity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.List;

import br.ufrgs.ufrgsmapas.R;
import br.ufrgs.ufrgsmapas.models.LocationModel;

/**
 * All the configuration required by the NavigationView
 * Created by alan on 13/08/15.
 */
public class SidebarView {

    private static final String TAG = SidebarView.class.getSimpleName();

    private Activity mActivity;
    private Drawer mDrawer;

    private static final int MENU_WIDTH_DP = 300;

    private List<SidebarViewEvents> mSidebarViewEventsList;

    private static final int ALL_BUILDINGS = 0;
    private static final int FAV_BUILDINGS = 1;
    private static final int NORMAL_MAP = 2;
    private static final int SATELLITE_MAP = 3;

    public SidebarView(Activity activity) {

        mSidebarViewEventsList = new ArrayList<>();
        mActivity = activity;
        buildSidebar(activity);
    }

    private void buildSidebar(Activity activity) {
        mDrawer = new DrawerBuilder()
                .withActivity(activity)
                .withDrawerWidthDp(MENU_WIDTH_DP)
                .withHasStableIds(true)
                .withFullscreen(true)
                .withTranslucentNavigationBarProgrammatically(false)
                .withHeader(R.layout.drawer_header)
                .withSelectedItem(ALL_BUILDINGS)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withIdentifier(ALL_BUILDINGS)
                                .withName("Todos os prédios")
                                .withIcon(R.drawable.ic_map_black_48dp)
                                .withIconTintingEnabled(true),
                        new PrimaryDrawerItem()
                                .withIdentifier(FAV_BUILDINGS)
                                .withName("Favoritos")
                                .withIcon(R.drawable.ic_star_black_48dp)
                                .withSetSelected(false)
                                .withIconTintingEnabled(true),
                        new DividerDrawerItem(),
                        new SwitchDrawerItem()
                                .withIdentifier(SATELLITE_MAP)
                                .withName("Satélite")
                                .withIcon(R.drawable.ic_satellite_black_48dp)
                                .withChecked(false)
                                .withSelectable(false)
                                .withIconTintingEnabled(true)
                                .withOnCheckedChangeListener(onCheckedChangeListener)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (drawerItem.getIdentifier()) {
                            case ALL_BUILDINGS:
                                clickedAllBuildings();
                                break;
                            case FAV_BUILDINGS:
                                clickedFavorites();
                                break;
                        }
                        mDrawer.closeDrawer();
                        return true;
                    }
                })
                .build();
    }

    /**
     * Check if side menu is open
     * @return Side menu is open
     */
    public boolean isSideMenuOpen(){
        return mDrawer.isDrawerOpen();
    }

    /**
     * Opens the drawer
     */
    public void openMenu() {
        mDrawer.openDrawer();
    }

    /**
     * Closes the drawer
     */
    public void closeMenu(){
        mDrawer.closeDrawer();
    }


    public void registerSidebarViewEvents(SidebarViewEvents s){
        mSidebarViewEventsList.add(s);
    }

    public void unregisterSidebarViewEvents(SidebarViewEvents s){
        mSidebarViewEventsList.remove(s);
    }

    /**
     * Update interface based on closest campus.
     * @param closestCampus
     */
    public void updateInterface(int closestCampus) {
        ImageView headerImage = (ImageView) mActivity.findViewById(R.id.drawer_header_image);
        TextView headerTitle = (TextView) mActivity.findViewById(R.id.drawer_header_text);

        switch (closestCampus){
            case LocationModel.NEAR_CENTRO:
                headerImage.setImageResource(R.drawable.centro);
                headerTitle.setText("UFRGS Centro");
                break;
            case LocationModel.NEAR_SAUDE:
                headerImage.setImageResource(R.drawable.saude);
                headerTitle.setText("UFRGS Saúde");
                break;
            case LocationModel.NEAR_ESEF:
                headerImage.setImageResource(R.drawable.olimpico);
                headerTitle.setText("UFRGS ESEF");
                break;
            case LocationModel.NEAR_VALE:
                headerImage.setImageResource(R.drawable.vale);
                headerTitle.setText("UFRGS Vale");
                break;
            case LocationModel.NEAR_LITORAL:
                headerImage.setImageResource(R.drawable.placeholder_purple);
                headerTitle.setText("UFRGS Litoral");
                break;
            case LocationModel.NEAR_NOBODY:
                headerImage.setImageResource(R.drawable.placeholder_brown);
                headerTitle.setText("UFRGS Mapas");
                break;
            default:
                break;

        }
    }

    private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
            if(drawerItem.getIdentifier() == SATELLITE_MAP){
                if(isChecked){
                    clickedSatteliteMap();
                } else{
                    clickedNormalMap();
                }
            }
        }
    };

    private void clickedSatteliteMap() {
        for(SidebarViewEvents s: mSidebarViewEventsList){
            s.sidebarClickSatelliteMap();
        }
    }

    private void clickedNormalMap() {
        for(SidebarViewEvents s: mSidebarViewEventsList){
            s.sidebarClickNormalMap();
        }
    }

    private void clickedFavorites() {
        for(SidebarViewEvents s: mSidebarViewEventsList){
            s.sidebarClickFavorites();
        }
    }

    private void clickedAllBuildings() {
        for (SidebarViewEvents s : mSidebarViewEventsList) {
            s.sidebarClickAllBuildings();
        }
    }


    public interface SidebarViewEvents{
        void sidebarClickAllBuildings();
        void sidebarClickFavorites();
        void sidebarClickNormalMap();
        void sidebarClickSatelliteMap();
    }
}
