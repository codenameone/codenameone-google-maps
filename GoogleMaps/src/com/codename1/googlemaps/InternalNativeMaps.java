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

import com.codename1.system.NativeInterface;
import com.codename1.ui.PeerComponent;

/**
 * This is an internal implementation class
 *
 * @author Shai Almog
 * @deprecated used internally please use MapContainer
 */
public interface InternalNativeMaps extends NativeInterface {

    public void setMapType(int type);
    public int getMapType();
    public int getMaxZoom();
    public int getMinZoom();
    public void setMarkerSize(int width, int height);
    public long addMarker(byte[] icon, double lat, double lon, String text, String longText, boolean callback, float anchorU, float anchorV);
    public long beginPath();
    public void addToPath(long pathId, double lat, double lon);
    public long finishPath(long pathId);
    public void removeMapElement(long id);
    public void removeAllMarkers();
    public PeerComponent createNativeMap(int mapId);
    public double getLatitude();
    public double getLongitude();
    public void setPosition(double lat, double lon);
    public void setZoom(double lat, double lon, float zoom);
    public float getZoom();
    public void deinitialize();
    public void initialize();
    
    public void calcScreenPosition(double lat, double lon);
    public int getScreenX();
    public int getScreenY();

    public void calcLatLongPosition(int x, int y);
    public double getScreenLat();
    public double getScreenLon();
    
    public void setShowMyLocation(boolean show);
    public void setRotateGestureEnabled(boolean e);
}
