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

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import br.ufrgs.ufrgsmapas.R;
import br.ufrgs.ufrgsmapas.utils.DebugUtils;
import br.ufrgs.ufrgsmapas.utils.MeasureUtils;
import br.ufrgs.ufrgsmapas.vos.BuildingVo;

/**
 * Handles the bottom sheet
 * Created by Alan Wink.
 */
public class BottomSheetView {

    private static final String TAG = BottomSheetView.class.getSimpleName();

    private int mClosedPosition = 0;
    private static final int ANIMATION_DURATION = 500;
    private int mOriginalPosition = 0;

    private boolean mOpen = true;
    private boolean mExtraOpen = false;

    private List<BottomSheetButtonsClick> mButtonsClickList;
    private BottomSheetActions mBottomSheetActions = null;

    private Context mContext;

    private View mBottomSheetCard;

    private ImageView mBuildingImage;
    private TextView mBuildingName;
    private TextView mBuildingAddress;
    private ImageButton mLocationButton;
    private ImageButton mStarButton;
    private ImageButton mExtraInfoButton;
    private View mFrontInfoLayout;

    private static final int TRANSLATION_EXTRA_DISTANCE_DP = 100;

    private BuildingVo mOpenBuilding;

    public BottomSheetView(Context context) {
        this.mContext = context;

        mButtonsClickList = new ArrayList<>();

        configureViews();

        mClosedPosition = getHeightPx();
        closeImmediately();
    }

    /**
     * Load objects and set listeners
     */
    private void configureViews() {
        // View
        mBottomSheetCard = ((Activity) mContext).findViewById(R.id.card_bottom);

        //Image
        mBuildingImage = (ImageView) mBottomSheetCard.findViewById(R.id.building_image);

        // Text
        mBuildingName = (TextView) mBottomSheetCard.findViewById(R.id.building_card_name);
        mBuildingAddress = (TextView) mBottomSheetCard.findViewById(R.id.building_card_address);

        // Buttons and listeners
        mExtraInfoButton = (ImageButton) mBottomSheetCard.findViewById(R.id.show_extra_info_button);
        mExtraInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                extraInfo();
            }
        });

        // Swipe left/right to dismiss
        mBottomSheetCard.setOnTouchListener(new SwipeDismissUpDownTouchListener(mBottomSheetCard,
                null,
                new SwipeDismissUpDownTouchListener.DismissUpDownCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        if(mBottomSheetActions != null){
                            mBottomSheetActions.onBottomSheetClosed();
                        }
                    }

                    @Override
                    public void onSwipeUp(View view) {
                        openExtraInfo();
                    }

                    @Override
                    public void onSwipeDown(View view) {
                        closeExtraInfo();
                    }
                }));

        mLocationButton = (ImageButton) mBottomSheetCard.findViewById(R.id.locationButton);
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(BottomSheetButtonsClick b: mButtonsClickList){
                    b.OnBottomSheetLocationButtonClick(mOpenBuilding);
                }
            }
        });

        mStarButton = (ImageButton) mBottomSheetCard.findViewById(R.id.starButton);
        mStarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (BottomSheetButtonsClick b : mButtonsClickList) {
                    b.OnBottomSheetStarButtonClick(mOpenBuilding);
                }
            }
        });

        // Views for animation
        mFrontInfoLayout = mBottomSheetCard.findViewById(R.id.card_front_info); // Has the image, title and title bar
    }

    /**
     * Close the sheet without animation.
     */
    public void closeImmediately(){
        mBottomSheetCard.animate()
                .translationY(mClosedPosition)
                .setDuration(0)
                .setListener(null)
                .start();
        mOpen = false;
        closeExtraInfo();
    }

    /**
     * Close sheet animating it.
     */
    public void close(){
        mBottomSheetCard.animate()
                .translationY(mClosedPosition)
                .setDuration(ANIMATION_DURATION)
                .setListener(null)
                .start();
        mOpen = false;
        closeExtraInfo();
    }

    /**
     * Open sheet with building information animating it.
     */
    public void open(BuildingVo buildingVo, boolean withExtraInfoOpened, final BottomSheetActions op){
        String buildingTitle = buildingVo.name;
        if(buildingVo.ufrgsBuildingCode != null) buildingTitle = buildingVo.ufrgsBuildingCode + " - " + buildingTitle;
        mBuildingName.setText(buildingTitle);

        Picasso.with(mContext).load(buildingVo.image).noFade().noPlaceholder().into(mBuildingImage);
        /* If it is not a building image start opened */
        if(!buildingVo.isABuildingImage && !mExtraOpen){
            extraInfo();
        }

        String extraInfo = formatExtraInfoString(buildingVo);
        mBottomSheetActions = op;

        mBuildingAddress.setText(extraInfo);

        if(withExtraInfoOpened && !mExtraOpen){
            extraInfo();
        }

        // Set favorite star button
        if(buildingVo.isStarred){
            setStarButton();
        }
        else{
            unsetStarButton();
        }

        mBottomSheetCard.animate()
                .translationY(mOriginalPosition)
                .setDuration(ANIMATION_DURATION)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mBottomSheetActions.onBottomSheetOpened();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                })
                .start();

        mOpenBuilding = buildingVo;
        mOpen = true;
    }

    /**
     * Formats a string to show extra info
     * @param buildingVo Building info
     * @return Formatted string
     */
    private String formatExtraInfoString(BuildingVo buildingVo) {
        String extraInfo = "";

        if(buildingVo.buildingAddress != null) extraInfo += buildingVo.buildingAddress;
        if(buildingVo.buildingAddressNumber != null) extraInfo += ", " + buildingVo.buildingAddressNumber;
        if(buildingVo.neighborhood != null) extraInfo += "\n" + buildingVo.neighborhood;
        if(buildingVo.city != null) extraInfo += " - " + buildingVo.city;
        if(buildingVo.state != null) extraInfo += " - " + buildingVo.state;
        if(buildingVo.zipCode != null) extraInfo += "\n" + buildingVo.zipCode;
        //if(buildingVo.phone != null) extraInfo += "\nFone: " + buildingVo.phone;

        return extraInfo;
    }

    /**
     * Set star button
     */
    public void setStarButton() {
        mStarButton.setImageResource(R.drawable.ic_star_black_48dp);
    }

    /**
     * Unset star button
     */
    public void unsetStarButton(){
        mStarButton.setImageResource(R.drawable.ic_star_border_black_48dp);
    }

    /**
     * Get the state of the sheet.
     * @return If the sheet is open.
     */
    public boolean isOpen(){
        return mOpen;
    }

    public void registerBottomSheetButtonsClick(BottomSheetButtonsClick b){
        mButtonsClickList.add(b);
    }

    public void unregisterBottomSheetButtonsClick(BottomSheetButtonsClick b){
        mButtonsClickList.remove(b);
    }

    /**
     * Set a margin for the bottom of the screen
     * @param px Value in px
     */
    public void setBottomMarginPx(int px) {
        // The bottom sheet needs to go up (-y)
        mOriginalPosition = -px;
    }

    /**
     * Get the bottom sheet height
     * @return Height in px
     */
    public int getHeightPx(){
        mBottomSheetCard.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int height = mBottomSheetCard.getMeasuredHeight();


        if(DebugUtils.DEBUG) Log.d(TAG, "BottomSheet Height: " + height);
        return height;
    }

    /**
     * Animate card to show or hide extra info about a building
     */
    private void extraInfo(){
        if(mExtraOpen){
            closeExtraInfo();
        }
        else {
            openExtraInfo();
        }
    }

    private void openExtraInfo() {
        //Open
        if(!mExtraOpen) {
            int translationPx = MeasureUtils.convertDpToPixel(TRANSLATION_EXTRA_DISTANCE_DP, mContext);
            mFrontInfoLayout.animate().translationY(-translationPx).start();
            //mExtraInfoButton.setImageResource(R.drawable.ic_remove_black_48dp);
            mExtraOpen = true;
        }
    }

    private void closeExtraInfo() {
        // Close
        if(mExtraOpen) {
            mFrontInfoLayout.animate().translationY(0).start();
            //mExtraInfoButton.setImageResource(R.drawable.ic_add_black_48dp);
            mExtraOpen = false;
        }
    }

    /**
     * Interface to register handlers to button clicks
     */
    public interface BottomSheetButtonsClick{
        void OnBottomSheetLocationButtonClick(BuildingVo buildingVo);
        void OnBottomSheetStarButtonClick(BuildingVo buildingVo);
    }

    public interface BottomSheetActions {
        void onBottomSheetOpened();
        void onBottomSheetClosed();
    }


}
