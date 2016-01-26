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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.ui.SquareTextView;

import br.ufrgs.ufrgsmapas.R;
import br.ufrgs.ufrgsmapas.utils.MeasureUtils;
import br.ufrgs.ufrgsmapas.vos.MapPositionVo;

/**
 * Customize how clusters and markers are rendered on the map.
 * Created by Alan Wink on 21/09/15.
 */
public class BuildingClusterRenderer extends DefaultClusterRenderer<MapPositionVo>{

    private static final String TAG = BuildingClusterRenderer.class.getSimpleName();
    private static final int ICON_SIZE_DP = 40;

    private Context mContext;

    private GoogleMap mMap;
    private final float mDensity;
    private IconGenerator mIconGenerator;
    private ClusterManager<MapPositionVo> mClusterManager;
    private ShapeDrawable mColoredCircleBackground;
    private SparseArray<BitmapDescriptor> mIcons;

    private static BitmapDescriptor sMarkerUfrgsBlue = null;
    private static BitmapDescriptor sMarkerUfrgsRed = null;
    private static BitmapDescriptor sMarkerStarBlue = null;
    private static BitmapDescriptor sMarkerStarRed = null;

    private static final int MAX_CLUSTER_SIZE = 150;
    private static final int MIN_CLUSTER_SIZE = 5;

    public BuildingClusterRenderer(Context context, GoogleMap map, ClusterManager<MapPositionVo> clusterManager) {
        super(context, map, clusterManager);

        mContext = context;

        createMarkerBitmaps(mContext);

        this.mMap = map;
        this.mDensity = context.getResources().getDisplayMetrics().density;
        this.mIconGenerator = new IconGenerator(context);
        this.mIconGenerator.setContentView(this.makeSquareTextView(context));
        this.mIconGenerator.setTextAppearance(com.google.maps.android.R.style.ClusterIcon_TextAppearance);
        this.mIconGenerator.setBackground(this.makeClusterBackground());
        this.mIcons = new SparseArray<>();
        this.mClusterManager = clusterManager;

    }

    /**
     * Override settings for markers (not clusters).
     * @param item Item to be drawn
     * @param markerOptions Marker Options for the item
     */
    @Override
    protected void onBeforeClusterItemRendered(MapPositionVo item, MarkerOptions markerOptions) {
        markerOptions.title(Integer.toString(item.id));
        markerOptions.icon(getBuildingIcon(mContext, item));
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<MapPositionVo> cluster, MarkerOptions markerOptions) {
        int bucket = this.getBucket(cluster);
        BitmapDescriptor descriptor = this.mIcons.get(bucket);
        if(descriptor == null) {
            this.mColoredCircleBackground.getPaint().setColor(this.getColor(bucket));
            descriptor = BitmapDescriptorFactory.fromBitmap(this.mIconGenerator.makeIcon(this.getClusterText(bucket)));
            this.mIcons.put(bucket, descriptor);
        }

        markerOptions.icon(descriptor);
    }

    private int getColor(int clusterSize) {
        return ContextCompat.getColor(mContext, R.color.blue900);
    }


    private SquareTextView makeSquareTextView(Context context) {
        SquareTextView squareTextView = new SquareTextView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-2, -2);
        squareTextView.setLayoutParams(layoutParams);
        squareTextView.setId(com.google.maps.android.R.id.text);
        int twelveDpi = (int)(12.0F * this.mDensity);
        squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi);
        return squareTextView;
    }

    private LayerDrawable makeClusterBackground() {
        this.mColoredCircleBackground = new ShapeDrawable(new OvalShape());
        ShapeDrawable outline = new ShapeDrawable(new OvalShape());
        outline.getPaint().setColor(-2130706433);
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline, this.mColoredCircleBackground});
        int strokeWidth = (int)(this.mDensity * 3.0F);
        background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        return background;
    }

    /**
     * Check all possibilities of icon draws.
     * @param mapPositionVo MapPositionVo of the building
     * @return A BitmapDescriptor with the icon to draw
     */
    public static BitmapDescriptor getBuildingIcon(Context context, MapPositionVo mapPositionVo) {

        if(mapPositionVo.selected){
            if(mapPositionVo.starred){
                return sMarkerStarRed;
            }
            else{
                return sMarkerUfrgsRed;
            }
        }
        else{
            if(mapPositionVo.starred){
                return sMarkerStarBlue;
            }
            else{
                return sMarkerUfrgsBlue;
            }
        }

    }

    private static void createMarkerBitmaps(Context context){
        sMarkerStarBlue = scaleDown(context, R.drawable.marker_star_blue);
        sMarkerStarRed = scaleDown(context, R.drawable.marker_star_red);
        sMarkerUfrgsBlue = scaleDown(context, R.drawable.marker_ufrgs_blue);
        sMarkerUfrgsRed = scaleDown(context, R.drawable.marker_ufrgs_red);
    }

    private static BitmapDescriptor scaleDown(Context context, int res){

        BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(context, res);
        Bitmap bitmap = bitmapDrawable.getBitmap();
        int iconSizePx = MeasureUtils.convertDpToPixel(ICON_SIZE_DP, context);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, iconSizePx, iconSizePx, true);

        return BitmapDescriptorFactory.fromBitmap(scaledBitmap);
    }

}
