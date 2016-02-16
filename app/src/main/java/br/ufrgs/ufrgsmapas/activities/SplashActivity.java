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
package br.ufrgs.ufrgsmapas.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import io.fabric.sdk.android.Fabric;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import br.ufrgs.ufrgsmapas.R;
import br.ufrgs.ufrgsmapas.models.BuildingDatabaseHelper;

/**
 * Splash screen called before the MainActivity.
 * Any special processing required before the app
 * beginning can be made here.
 * @author Alan Wink
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Starts fabric
        Fabric.with(this, new Answers(), new Crashlytics());

        // We will check if the user has the database here. If it is not created, we will copy the
        // database from the assets folder to the database folder
        try {
            installDatabaseIfRequired();
        } catch (IOException e) {
            Log.e(TAG, "Unable to copy DB: " + e);
        }

        int milisecondsDelayed = 1000;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, milisecondsDelayed);

    }

    private void installDatabaseIfRequired() throws IOException {
        final String PACKAGE_NAME = getPackageName();
        final String DB_PATH = "/data/data/" + PACKAGE_NAME + "/databases/";
        final String DB_NAME = BuildingDatabaseHelper.DATABASE_NAME;
        final String DB_FULL_PATH = DB_PATH + DB_NAME;

        Log.d(TAG, "Checking if there is a DB");

        if(!databaseInstalled(DB_FULL_PATH)){
            copyDatabase(DB_NAME, DB_FULL_PATH);
        }
    }

    private boolean databaseInstalled(String DB_FULL_PATH) {
        SQLiteDatabase database = null;

        try{
            database = SQLiteDatabase.openDatabase(DB_FULL_PATH, null, SQLiteDatabase.OPEN_READONLY);
        }catch (SQLiteException e){
            // database does not exist yet
        }

        if(database != null){
            database.close();
        }
        Log.d(TAG, "Verified: " + (database != null));
        return database != null;
    }

    private void copyDatabase(String DB_NAME, String DB_FULL_PATH) throws IOException {
        Log.d(TAG, "Copy starting");

        // Create a empty database
        BuildingDatabaseHelper dbHelper = new BuildingDatabaseHelper(this);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        database.close();

        // Open local database
        InputStream inputDatabase = getAssets().open(DB_NAME);

        // Open empty database
        OutputStream outputDatabase = new FileOutputStream(DB_FULL_PATH);

        // Transfer bytes from input to output
        byte[] buffer = new byte[1024];
        int length;
        while((length = inputDatabase.read(buffer)) > 0){
            outputDatabase.write(buffer, 0, length);
        }
        Log.d(TAG, "Copy finish");
        // Close all
        outputDatabase.flush();
        outputDatabase.close();
        inputDatabase.close();

    }

}
