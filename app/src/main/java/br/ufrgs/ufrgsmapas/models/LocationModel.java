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
package br.ufrgs.ufrgsmapas.models;

import android.util.SparseArray;

import br.ufrgs.ufrgsmapas.utils.MeasureUtils;
import br.ufrgs.ufrgsmapas.vos.BuildingVo;
import br.ufrgs.ufrgsmapas.vos.MapPositionVo;

/**
 * Handles operations to find the nearest building to a specific point.
 * Created by Alan Wink on 28/09/15.
 */
public class LocationModel {

    public static final int NEAR_CENTRO = 1;
    public static final int NEAR_SAUDE = 2;
    public static final int NEAR_ESEF = 3;
    public static final int NEAR_VALE = 4;
    public static final int NEAR_LITORAL = 6;
    public static final int NEAR_NOBODY = 0;

    private final BuildingModel mBuildingModel;

    public LocationModel(BuildingModel buildingModel) {
        this.mBuildingModel = buildingModel;
    }

    /**
     * Search in the building list the closest builing to a target position, if inside a radius.
     * @param targetLatitude Target latitude
     * @param targetLongitude Target longitude
     * @param radius Radius (in meters)
     * @return The closest campus to a user location (if inside radius)
     */
    public int closestBuilding(double targetLatitude, double targetLongitude, double radius){

        SparseArray<MapPositionVo> buildingList = mBuildingModel.getFullBuildingPositions();

        MapPositionVo closest = null;
        double minDistance = Float.MAX_VALUE;

        MapPositionVo candidate;
        double candidateDistance;

        for(int i = 0; i < buildingList.size(); i++){
            // Measure
            candidate = buildingList.valueAt(i);
            candidateDistance = MeasureUtils.coordinateDistance(targetLatitude, targetLongitude,
                    candidate.latitude, candidate.longitude);

            // Compare and change if it is closer
            if(candidateDistance < minDistance){
                minDistance = candidateDistance;
                closest = candidate;
            }

        }

        // Check if the result is inside radius and return the campus
        if (minDistance <= radius){
            return getCampus(closest);
        }
        else {
            return NEAR_NOBODY;
        }
    }

    /**
     * Get campus code from a MapPositionVo
     * @param target Building's MapPositionVo
     * @return Campus code
     */
    private int getCampus(MapPositionVo target){
        if(target == null){
            return NEAR_NOBODY;
        }

        BuildingVo building = mBuildingModel.getBuilding(target.id);
        return building.campusCode;
    }

}
