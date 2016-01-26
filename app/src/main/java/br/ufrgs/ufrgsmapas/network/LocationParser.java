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
package br.ufrgs.ufrgsmapas.network;

import android.util.Log;
import android.util.SparseArray;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;

import java.io.IOException;
import java.util.List;

import br.ufrgs.ufrgsmapas.utils.DebugUtils;
import br.ufrgs.ufrgsmapas.vos.BuildingVo;

/**
 * Parse BuildingVo() information (except name) from JSON file.
 * Created by alan on 31/07/15.
 */
public class LocationParser {

    private static final String TAG = LocationParser.class.getSimpleName();

    private static final String URL = "http://www1.ufrgs.br/infraestrutura/geolocation/index.php/mapa/predio";

    public static void parseBuildings(SparseArray<BuildingVo> mTempBuilding){

        // Read JSON File
        Connection con = HttpConnection.connect(URL);
        con.method(Connection.Method.POST).ignoreContentType(true);
        Connection.Response resp;
        try {
            resp = con.execute();
        } catch (IOException e) {
            if(DebugUtils.DEBUG) Log.e(TAG, "Error fetching positions: " + e);
            return;
        }
        String jsonDoc = resp.body();

        // Parse JSON to find each element
        JSONObject jsonStartObject = (JSONObject) JSONValue.parse(jsonDoc);

        // Get the array with objects representing each building
        JSONArray jsonBuildings = (JSONArray) jsonStartObject.get("features");

        int buildingsListSize = ((List) jsonBuildings).size();
        // Iterate through buildings
        for(int i = 0; i < buildingsListSize; i++){
            // Get buildingVo object
            JSONObject jsonBuilding = (JSONObject) jsonBuildings.get(i);

            // Get buildingVo id
            int id = ((Number) jsonBuilding.get("id")).intValue();
            // Get coordinates array
            JSONObject jsonGeometry = (JSONObject) jsonBuilding.get("geometry");
            JSONArray jsonCoordinates =
                    (JSONArray)
                            ( (JSONArray)
                                    ( (JSONArray) jsonGeometry.get("coordinates")
                                    ).get(0)
                            ).get(0);

            // Get latitudes and longitudes
            List coordList = jsonCoordinates;
            int coordListSize = coordList.size();
            double[] latitude = new double[coordListSize];
            double[] longitude = new double[coordListSize];
            for(int j = 0; j< coordListSize; j++){
                latitude[j] = (double) ((List) coordList.get(j)).get(1);
                longitude[j] = (double) ((List) coordList.get(j)).get(0);
            }

            // Create buildingVo object and add to list - store only the center of the position
            double[] center = centroid(latitude, longitude);
            BuildingVo buildingVo = new BuildingVo(id, center[0], center[1]);
            mTempBuilding.append(id, buildingVo);

        }

    }

    static private double[] centroid(double[] latitude, double[] longitude){
        double[] centroid = {0.0, 0.0};

        int numPoints = latitude.length - 1;// Last point is same of the first point
        for(int i = 0; i < numPoints; i++){
            centroid[0] += latitude[i];
            centroid[1] += longitude[i];
        }

        centroid[0] = centroid[0]/numPoints;
        centroid[1] = centroid[1]/numPoints;

        return centroid;

    }

}
