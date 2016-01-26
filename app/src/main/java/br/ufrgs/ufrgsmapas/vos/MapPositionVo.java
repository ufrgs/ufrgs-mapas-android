package br.ufrgs.ufrgsmapas.vos;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Get from database only the required values
 * Created by alan on 18/08/15.
 */
public class MapPositionVo implements ClusterItem{
    public int id;
    public double latitude;
    public double longitude;

    public boolean selected = false;
    public boolean starred = false;

    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }
}
