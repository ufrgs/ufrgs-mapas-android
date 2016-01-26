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

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import br.ufrgs.ufrgsmapas.vos.BuildingVo;

/**
 * Populates the database with information from web (if required).
 * Created by alan on 18/08/15.
 */
public class DatabaseBuilder {

    private static final String TAG = DatabaseBuilder.class.getSimpleName();

    // Database fields
    private SQLiteDatabase mDatabase;
    private BuildingDatabaseHelper mDbHelper;

    public DatabaseBuilder(Context context){
        mDbHelper = new BuildingDatabaseHelper(context);
    }

    /**
     * Get all the buildings information and store in the database
     * @param buildingSparseArray SparseArray with all the building information
     */
    public void buildDatabase(SparseArray<BuildingVo> buildingSparseArray){

        // Open database
        mDatabase = mDbHelper.getWritableDatabase();

        // Clear table
        mDatabase.execSQL("delete from " + BuildingDatabaseHelper.TABLE_BUILDINGS);

        for(int i = 0; i < buildingSparseArray.size(); i++){
            insertOnBuildingsTable(mDatabase, buildingSparseArray.valueAt(i));
        }

        // Close database
        mDatabase.close();
    }

    /**
     * Insert a building object in the database
     * @param database Database object
     * @param b BuildingVo object
     */
    private void insertOnBuildingsTable(SQLiteDatabase database, BuildingVo b) {

        ContentValues values = new ContentValues();
        values.put(BuildingDatabaseHelper.COLUMN_ID, b.id);
        values.put(BuildingDatabaseHelper.COLUMN_LATITUDE, b.latitude);
        values.put(BuildingDatabaseHelper.COLUMN_LONGITUDE, b.longitude);
        values.put(BuildingDatabaseHelper.COLUMN_NAME, b.name);
        values.put(BuildingDatabaseHelper.COLUMN_UFRGS_BUILDING_CODE, b.ufrgsBuildingCode);
        values.put(BuildingDatabaseHelper.COLUMN_ADDRESS_NAME, b.buildingAddress);
        values.put(BuildingDatabaseHelper.COLUMN_ADDRESS_NUMBER, b.buildingAddressNumber);
        values.put(BuildingDatabaseHelper.COLUMN_ZIP_CODE, b.zipCode);
        values.put(BuildingDatabaseHelper.COLUMN_NEIGHBORHOOD, b.neighborhood);
        values.put(BuildingDatabaseHelper.COLUMN_CITY, b.city);
        values.put(BuildingDatabaseHelper.COLUMN_STATE, b.state);
        values.put(BuildingDatabaseHelper.COLUMN_CAMPUS, b.campusCode);
        values.put(BuildingDatabaseHelper.COLUMN_EXTERNAL_BUILDING, b.isExternalBuilding);
        values.put(BuildingDatabaseHelper.COLUMN_DESCRIPTION, b.description);
        values.put(BuildingDatabaseHelper.COLUMN_TELEPHONE, b.phone);
        values.put(BuildingDatabaseHelper.COLUMN_IS_HISTORICAL, b.isHistorical);
        values.put(BuildingDatabaseHelper.COLUMN_URL, b.locationUrl);

        database.insert(BuildingDatabaseHelper.TABLE_BUILDINGS, null, values);

    }
}
