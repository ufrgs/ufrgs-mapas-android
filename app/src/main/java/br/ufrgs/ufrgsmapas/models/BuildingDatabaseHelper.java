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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import br.ufrgs.ufrgsmapas.utils.DebugUtils;

/**
 * Database used to store building information.
 * Created by alan on 18/08/15.
 */
public class BuildingDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = BuildingDatabaseHelper.class.getSimpleName();

    /******************************* DB Description ***********************************************/
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "buildings.db";

    /********************************* Table Names ************************************************/
    public static final String TABLE_BUILDINGS = "buildings";

    /******************************** Column Names ************************************************/
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_UFRGS_BUILDING_CODE = "ufrgsBuildingCode";
    public static final String COLUMN_ADDRESS_NAME = "addressName";
    public static final String COLUMN_ADDRESS_NUMBER = "addressNumber";
    public static final String COLUMN_ZIP_CODE = "zipCode";
    public static final String COLUMN_NEIGHBORHOOD = "neighborhood";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_STATE = "state";
    public static final String COLUMN_CAMPUS = "campus";
    public static final String COLUMN_EXTERNAL_BUILDING = "externalBuilding";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_TELEPHONE = "telephone";
    public static final String COLUMN_IS_HISTORICAL = "isHistorical";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_IS_STARRED = "starred";

    /***************************** Pre-built queries **********************************************/
    private static final String DATABASE_CREATE = "create table " + TABLE_BUILDINGS + "(" +
                                                    COLUMN_ID + " integer primary key, " +
                                                    COLUMN_LATITUDE + " real, " +
                                                    COLUMN_LONGITUDE + " real, " +
                                                    COLUMN_NAME + " text, " +
                                                    COLUMN_UFRGS_BUILDING_CODE + " text, " +
                                                    COLUMN_ADDRESS_NAME + " text, " +
                                                    COLUMN_ADDRESS_NUMBER + " text, " +
                                                    COLUMN_ZIP_CODE + " text, " +
                                                    COLUMN_NEIGHBORHOOD + " text, " +
                                                    COLUMN_CITY + " text, " +
                                                    COLUMN_STATE + " text, " +
                                                    COLUMN_CAMPUS + " text, " +
                                                    COLUMN_EXTERNAL_BUILDING + " text, " +
                                                    COLUMN_DESCRIPTION + " text, " +
                                                    COLUMN_TELEPHONE + " text, " +
                                                    COLUMN_IS_HISTORICAL + " text, " +
                                                    COLUMN_URL + " text " +
                                                    COLUMN_IS_STARRED + " integer " +
                                                    "); ";



    public BuildingDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion == 1){

            if(DebugUtils.DEBUG) Log.d(TAG, "Database from v1 to v2");

            // From v1 to v2: created a new column for starred, default FALSE
            db.execSQL("ALTER TABLE " + TABLE_BUILDINGS + " ADD COLUMN " + COLUMN_IS_STARRED + " integer DEFAULT 0; ");
        }
    }
}
