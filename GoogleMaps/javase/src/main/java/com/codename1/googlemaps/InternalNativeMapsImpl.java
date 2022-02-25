/*
 * Copyright (c) 2014, Codename One LTD. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codename1.googlemaps;

public class InternalNativeMapsImpl implements com.codename1.googlemaps.InternalNativeMaps{

    public InternalNativeMapsImpl() {
        /*
        javascript.googlemaps.key=YOUR_JAVASCRIPT_API_KEY
        android.xapplication=<meta-data android:name="com.google.android.maps.v2.API_KEY" android:value="YOUR_ANDROID_API_KEY"/>
        ios.afterFinishLaunching=[GMSServices provideAPIKey:@"YOUR_IOS_API_KEY"];
        android.min_sdk_version=19
         */

        System.setProperty("codename1.arg.{{#googlemaps#javascript.googlemaps.key}}.label", "Javascript API Key");
        System.setProperty("codename1.arg.{{#googlemaps#javascript.googlemaps.key}}.description", "Please enter your Javascript API key.");
        System.setProperty("codename1.arg.{{#googlemaps#javascript.googlemaps.key}}.link", "https://developers.google.com/maps/documentation/javascript/get-api-key Get Key");

        System.setProperty("codename1.arg.{{#googlemaps#android.xapplication}}.label", "android.xapplication");
        System.setProperty("codename1.arg.{{#googlemaps#android.xapplication}}.description", "Your android.xapplication build hint must inject your Android API key");
        System.setProperty("codename1.arg.{{#googlemaps#android.xapplication}}.hint", "<meta-data android:name=\"com.google.android.maps.v2.API_KEY\" android:value=\"YOUR_ANDROID_API_KEY\"/>");
        System.setProperty("codename1.arg.{{#googlemaps#android.xapplication}}.link", "https://developers.google.com/maps/documentation/android-sdk/get-api-key Get Key");

        System.setProperty("codename1.arg.{{#googlemaps#ios.afterFinishLaunching}}.label", "ios.afterFinishLaunching");
        System.setProperty("codename1.arg.{{#googlemaps#ios.afterFinishLaunching}}.description", "Your ios.afterFinishLaunching hint must inject your IOS API Key");
        System.setProperty("codename1.arg.{{#googlemaps#ios.afterFinishLaunching}}.hint", "[GMSServices provideAPIKey:@\"YOUR_IOS_API_KEY\"];");
        System.setProperty("codename1.arg.{{#googlemaps#ios.afterFinishLaunching}}.link", "https://developers.google.com/maps/documentation/ios-sdk/get-api-key Get Key");

        System.setProperty("codename1.arg.{{#googlemaps#android.min_sdk_version}}.label", "Android Minimum SDK Version");
        System.setProperty("codename1.arg.{{#googlemaps#android.min_sdk_version}}.description", "Your Android Minimum SDK Version must be at least 19");
        System.setProperty("codename1.arg.{{#googlemaps#android.min_sdk_version}}.hint", "19");

        System.setProperty("codename1.arg.{{@googlemaps}}.label", "Google Maps");
        System.setProperty("codename1.arg.{{@googlemaps}}.description", "The following build hints are required for the Google Maps cn1lib to operate correctly.");





    }

    public void initialize() {
    }

    public double getLatitude() {
        return 0;
    }

    public void setMapType(int param) {
    }

    public long finishPath(long param) {
        return 0;
    }

    public int getMaxZoom() {
        return 0;
    }

    public double getLongitude() {
        return 0;
    }

    public void removeAllMarkers() {
    }

    public void addToPath(long param, double param1, double param2) {
    }

    public long addMarker(byte[] param, double param1, double param2, String param3, String param4, boolean param5, float param6, float param7) {
        return 0;
    }

    public int getMapType() {
        return 0;
    }

    public void removeMapElement(long param) {
    }

    public com.codename1.ui.PeerComponent createNativeMap(int param) {
        return null;
    }

    public int getMinZoom() {
        return 0;
    }

    public long beginPath() {
        return 0;
    }

    public void setPosition(double param, double param1) {
    }

    public float getZoom() {
        return 0;
    }

    public void setZoom(double param, double param1, float param2) {
    }
    
    public void setMarkerSize(int w, int h) {
        
    }

    public void deinitialize() {
    }

    public boolean isSupported() {
        return false;
    }

    public void calcScreenPosition(final double lat, final double lon) {
    }
    
    public int getScreenX() {
        return 0;
    }
    
    public int getScreenY() {
        return 0;
    }

    public void calcLatLongPosition(final int x, final int y) {
    }
    
    public double getScreenLat() {
        return 0;
    }
    
    public double getScreenLon() {
        return 0;
    }

    public void setShowMyLocation(boolean show) {
    }

    public void setRotateGestureEnabled(boolean e) {
    }
    
    public void setPathStrokeColor(int color) {
        
    }
    public int getPathStrokeColor() {
        return 0;
    }
    public void setPathStrokeWidth(int width) {
        
    }
    public int getPathStrokeWidth() {
        return 1;
    }
}
