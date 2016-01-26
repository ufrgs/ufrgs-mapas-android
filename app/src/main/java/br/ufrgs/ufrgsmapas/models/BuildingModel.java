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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import br.ufrgs.ufrgsmapas.R;
import br.ufrgs.ufrgsmapas.network.ParserExecutor;
import br.ufrgs.ufrgsmapas.vos.BuildingVo;
import br.ufrgs.ufrgsmapas.vos.MapPositionVo;

/**
 * Interface to get the buildings list from storage or web.
 * Created by Alan Wink on 03/08/15.
 */
public class BuildingModel {

    private static final String TAG = BuildingModel.class.getSimpleName();

    private BuildingDatabaseHelper mDbHelper;
    private SQLiteDatabase mDatabase;

    /**
     * Open the database and populate it in case the buildings table is empty.
     * @param context Application context
     */
    public BuildingModel(Context context){

        // Get DB instance
        mDbHelper = new BuildingDatabaseHelper(context);
        mDatabase = mDbHelper.getWritableDatabase();

        // Execute parsers if the buildings table is empty
        if(isBuildingTableEmpty()) {
            ParserExecutor parserExecutor = new ParserExecutor();
            parserExecutor.executeParsers(context);
        }
    }

    /**
     * Creates a SparseArray of MapPositionVo, to crate Markers in the map if minimal information
     * possible.
     * @return A SparseArray of MapPositionVo with all the buildings.
     */
    public SparseArray<MapPositionVo> getFullBuildingPositions(){
        SparseArray<MapPositionVo> result = new SparseArray<>();
        MapPositionVo idPos;

        String[] queryColumns = {BuildingDatabaseHelper.COLUMN_ID,
                                    BuildingDatabaseHelper.COLUMN_LATITUDE,
                                    BuildingDatabaseHelper.COLUMN_LONGITUDE,
                                    BuildingDatabaseHelper.COLUMN_IS_STARRED};

        Cursor cursor = mDatabase.query(BuildingDatabaseHelper.TABLE_BUILDINGS,
                                        queryColumns, null, null, null, null, null);

        // Get id, latitude and longitude for all buildings
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            idPos = new MapPositionVo();
            idPos.id = cursor.getInt(0);
            idPos.latitude = cursor.getDouble(1);
            idPos.longitude = cursor.getDouble(2);
            idPos.starred = cursor.getInt(3) == 1;
            result.append(idPos.id, idPos);
            cursor.moveToNext();
        }
        cursor.close();

        return result;
    }

    /**
     * Check if the buildings table is empty.
     * @return The buildings table is empty.
     */
    public boolean isBuildingTableEmpty() {
        String query = "SELECT count(*) FROM " + BuildingDatabaseHelper.TABLE_BUILDINGS;
        Cursor cursor = mDatabase.rawQuery(query, null);
        cursor.moveToFirst();
        int rowNum = cursor.getInt(0);
        cursor.close();
        return rowNum == 0;
    }

    /**
     * Search for searchbox
     * @param query User query
     * @param maxResults Maximum number of results to be displayed on the searchbox
     * @return A Map<BuildingVo.name, BuildingVo.id> to be used by the searchbox.
     */
    public Map<String, String> searchBuildingName(String query, int maxResults){

        String sqlQuery = "SELECT " + BuildingDatabaseHelper.COLUMN_UFRGS_BUILDING_CODE + "||' - '||"
                                    + BuildingDatabaseHelper.COLUMN_NAME + ", "
                                    + BuildingDatabaseHelper.COLUMN_ID +
                           " FROM " + BuildingDatabaseHelper.TABLE_BUILDINGS +
                    " WHERE upper(" + BuildingDatabaseHelper.COLUMN_UFRGS_BUILDING_CODE + "||' - '||"
                                    + BuildingDatabaseHelper.COLUMN_NAME +
                  ") like upper('%" + query + "%')" +
                          " limit " + maxResults + ";";

        Map<String, String> result = new HashMap<>();

        Cursor cursor = mDatabase.rawQuery(sqlQuery, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            result.put(cursor.getString(0), cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();

        return result;
    }

    /**
     * Get a BuildingVo object from the database.
     * @param id BuildingVo id
     * @return BuildingVo object
     */
    public BuildingVo getBuilding(int id){
        String sqlQuery = "SELECT * " +
                             " FROM " + BuildingDatabaseHelper.TABLE_BUILDINGS +
                            " WHERE " + BuildingDatabaseHelper.COLUMN_ID +
                               " == " + id;

        Cursor cursor = mDatabase.rawQuery(sqlQuery, null);
        cursor.moveToFirst();

        // BuildingVo(int id, double latitude, double longitude)
        BuildingVo b = new BuildingVo(cursor.getInt(0),
                                    cursor.getDouble(1),
                                    cursor.getDouble(2));

        b.name = cursor.getString(3);

        b.image = getPlaceHolderImage();
        b.isABuildingImage = false;

        b.ufrgsBuildingCode = cursor.getString(4);
        b.buildingAddress = cursor.getString(5);
        b.buildingAddressNumber = cursor.getString(6);
        b.zipCode = cursor.getString(7);
        b.neighborhood = cursor.getString(8);
        b.city = cursor.getString(9);
        b.state = cursor.getString(10);
        b.campusCode = cursor.getInt(11);
        b.isExternalBuilding = cursor.getString(12);
        b.description = cursor.getString(13);
        b.phone = cursor.getString(14);
        b.isHistorical = cursor.getString(15);
        b.locationUrl = cursor.getString(16);
        if(cursor.getInt(17) == 1){
            b.isStarred = true;
        }

        return b;
    }

    private int getPlaceHolderImage() {
        int max = 5;
        int min = 1;
        Random rn = new Random();
        int choice = rn.nextInt(max - min + 1) + min;
        switch (choice){
            case 1: return R.drawable.placeholder_blue;
            case 2: return R.drawable.placeholder_purple;
            case 3: return R.drawable.placeholder_orange;
            case 4: return R.drawable.placeholder_teal;
            case 5: return R.drawable.placeholder_red;
            default: return R.drawable.placeholder_brown;
        }
    }

    /**
     * Set the starred attribute of a building
     * @param id BuildingVo ID
     * @param starred Value to be written
     */
    public void setStarred(int id, boolean starred){
        String starredVal = "0";
        if(starred){
            starredVal = "1";
        }

        String sqlQuery = "UPDATE " + BuildingDatabaseHelper.TABLE_BUILDINGS +
                " SET " + BuildingDatabaseHelper.COLUMN_IS_STARRED +
                " = " + starredVal +
                " WHERE " + BuildingDatabaseHelper.COLUMN_ID + " = " + id + ";";

        mDatabase.execSQL(sqlQuery);

    }

    public boolean isStarred(int id){
        String query = "SELECT " + BuildingDatabaseHelper.COLUMN_IS_STARRED +
                " FROM " + BuildingDatabaseHelper.TABLE_BUILDINGS +
                " WHERE " + BuildingDatabaseHelper.COLUMN_ID + " = " + id + ";";

        Cursor cursor = mDatabase.rawQuery(query, null);
        cursor.moveToFirst();

        return cursor.getInt(0) == 1;
    }

    //TODO: Group this with getFullBuildingPositions()
    public SparseArray<MapPositionVo> getStarredBuildingPositions() {
        SparseArray<MapPositionVo> result = new SparseArray<>();
        MapPositionVo idPos;

        String query = "SELECT " + BuildingDatabaseHelper.COLUMN_ID +
                ", " + BuildingDatabaseHelper.COLUMN_LATITUDE +
                ", " + BuildingDatabaseHelper.COLUMN_LONGITUDE +
                ", " + BuildingDatabaseHelper.COLUMN_IS_STARRED +
                " FROM " + BuildingDatabaseHelper.TABLE_BUILDINGS +
                " WHERE " + BuildingDatabaseHelper.COLUMN_IS_STARRED +
                " =1; ";

        Cursor cursor = mDatabase.rawQuery(query, null);

        // Get id, latitude and longitude for all buildings
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            idPos = new MapPositionVo();
            idPos.id = cursor.getInt(0);
            idPos.latitude = cursor.getDouble(1);
            idPos.longitude = cursor.getDouble(2);
            idPos.starred = cursor.getInt(3) == 1;
            result.append(idPos.id, idPos);
            cursor.moveToNext();
        }
        cursor.close();

        return result;
    }
}
