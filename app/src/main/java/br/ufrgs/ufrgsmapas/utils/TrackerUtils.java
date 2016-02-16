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

package br.ufrgs.ufrgsmapas.utils;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.SearchEvent;

/**
 * @author Alan Wink
 */
public class TrackerUtils {
    public static void buildingStar(String ufrgsBuildingCode, boolean starred){
        if(starred) {
            Answers.getInstance().logCustom(
                    new CustomEvent("Starred building")
                            .putCustomAttribute("ufrgsBuildingCode", ufrgsBuildingCode)
            );
        }
    }

    public static void menuOpen(){
        Answers.getInstance().logCustom(
                new CustomEvent("Menu open")
        );
    }

    public static void externalNavigation(){
        Answers.getInstance().logCustom(
                new CustomEvent("Open external navigation")
        );
    }

    public static void searchQuery(String query){
        Answers.getInstance().logSearch(new SearchEvent()
                .putQuery(query));
    }

    public static void pinClick(int buildingId){
        Answers.getInstance().logCustom(
                new CustomEvent("Clicked building")
                        .putCustomAttribute("buildingId", buildingId)
        );
    }

    public static void satteliteSwitch(){
        Answers.getInstance().logCustom(
                new CustomEvent("Sattelite switch")
        );
    }

    public static void voiceAction(){
        Answers.getInstance().logCustom(
                new CustomEvent("Voice action")
        );
    }

    public static void normalMapSwitch() {
        Answers.getInstance().logCustom(
                new CustomEvent("Normal map switch")
        );
    }

    public static void favoritesMap() {
        Answers.getInstance().logCustom(
                new CustomEvent("Favorites map switch")
        );
    }

    public static void allBuildingsMap() {
        Answers.getInstance().logCustom(
                new CustomEvent("All buildings map switch")
        );
    }
}
