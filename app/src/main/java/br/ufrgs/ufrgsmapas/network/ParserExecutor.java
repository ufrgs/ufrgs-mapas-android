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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import java.util.concurrent.ExecutionException;

import br.ufrgs.ufrgsmapas.models.BuildingDatabaseHelper;
import br.ufrgs.ufrgsmapas.models.DatabaseBuilder;
import br.ufrgs.ufrgsmapas.utils.DebugUtils;
import br.ufrgs.ufrgsmapas.vos.BuildingVo;

/**
 * Execute parsers in new threads and store the results in the database
 * Created by alan on 31/07/15.
 */
public class ParserExecutor {

    private static final String TAG = ParserExecutor.class.getSimpleName();

    // Temp list to transfer information from the first parser to the second parser.=
    private SparseArray<BuildingVo> mTempBuilding;

    // Database fields
    private SQLiteDatabase mDatabase;
    private BuildingDatabaseHelper mDbHelper;

    public void executeParsers(Context context) {

        mTempBuilding = new SparseArray<>();

        LocationParserExecutor locationParserExecutor = new LocationParserExecutor();
        ExtraInfoParserExecutor extraInfoParserExecutor = new ExtraInfoParserExecutor();

        try {
            locationParserExecutor.execute().get();
            extraInfoParserExecutor.execute().get();
        } catch (InterruptedException e) {
            if(DebugUtils.DEBUG) Log.e(TAG, "Parsing error: " + e);
        } catch (ExecutionException e) {
            if(DebugUtils.DEBUG) Log.e(TAG, "Parsing error: " + e);
        }

        // Get DB instance
        mDbHelper = new BuildingDatabaseHelper(context);
        mDatabase = mDbHelper.getWritableDatabase();

        // Store in the database
        DatabaseBuilder dbBuilder = new DatabaseBuilder(context);
        dbBuilder.buildDatabase(mTempBuilding);
    }


    /**
     * Executes the position parser.
     */
    private class LocationParserExecutor extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            LocationParser.parseBuildings(mTempBuilding);
            return null;
        }
    }

    /**
     * Executes the extra information parser.
     */
    private class ExtraInfoParserExecutor extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            ExtraInfoParser.fillBuildings(mTempBuilding);
            return null;
        }
    }

}
