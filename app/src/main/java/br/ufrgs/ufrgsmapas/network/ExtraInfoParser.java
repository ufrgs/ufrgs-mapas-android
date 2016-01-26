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

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;

import java.io.IOException;

import br.ufrgs.ufrgsmapas.utils.DebugUtils;
import br.ufrgs.ufrgsmapas.vos.BuildingVo;

/**
 * Fetch extra information about the buildings.
 * Created by alan on 14/08/15.
 */
public class ExtraInfoParser {

    private static final String TAG = ExtraInfoParser.class.getSimpleName();

    private static final String BASE_URL =
            "http://www1.ufrgs.br/infraestrutura/geolocation/index.php/mapa/identificarPredio/id/";

    public static void fillBuildings(SparseArray<BuildingVo> mTempBuilding){

        if(mTempBuilding == null || mTempBuilding.size() == 0){
            if(DebugUtils.DEBUG) Log.e(TAG, "No list to fillBuildings() populate");
            return;
        }

        BuildingVo buildingVo;
        for(int i = 0; i < mTempBuilding.size(); i++){
            buildingVo = mTempBuilding.valueAt(i);
            fillBuildingInformation(buildingVo);
        }
    }

    /**
     * Get the information for a buildingVo
     * @param buildingVo The incomplete BuildingVo object. The extra information will be added to
     *                 this object.
     * @return The same BuildingVo object.
     */
    private static BuildingVo fillBuildingInformation(BuildingVo buildingVo){

        int buildingId = buildingVo.id;
        String URL = BASE_URL + buildingId;

        // Read JSON File
        Connection con = HttpConnection.connect(URL);
        con.method(Connection.Method.POST).ignoreContentType(true);
        Connection.Response resp;
        try {
            resp = con.execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String jsonDoc = resp.body();

        JSONObject jsonStartObject = (JSONObject) JSONValue.parse(jsonDoc);

        // Create a BuildingVo object
        buildingVo.name = (String) jsonStartObject.get("NomePredio");
        buildingVo.ufrgsBuildingCode = (String) jsonStartObject.get("CodPredioUFRGS");
        buildingVo.buildingAddress = (String) jsonStartObject.get("Logradouro");
        buildingVo.buildingAddressNumber = (String) jsonStartObject.get("NrLogradouro");
        buildingVo.zipCode = (String) jsonStartObject.get("CEP");
        buildingVo.neighborhood = (String) jsonStartObject.get("Bairro");
        buildingVo.city = (String) jsonStartObject.get("Cidade");
        buildingVo.state = (String) jsonStartObject.get("UF");
        buildingVo.campusCode = Integer.valueOf((String) jsonStartObject.get("Campus"));
        buildingVo.isExternalBuilding = (String) jsonStartObject.get("IndicadorPredioExterno");
        buildingVo.description = (String) jsonStartObject.get("Descricao");
        buildingVo.phone = (String) jsonStartObject.get("TelefonePortaria");
        buildingVo.isHistorical = (String) jsonStartObject.get("IndicadorPredioHistorico");
        buildingVo.locationUrl = (String) jsonStartObject.get("URLLocalizacao");

        return buildingVo;

    }

}
